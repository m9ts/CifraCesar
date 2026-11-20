package com.example.cifracesar.service;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 Dá uma nota para um texto decifrado, usada para escolher o deslocamento mais provável no brute force.
 A nota soma três sinais: palavras do português, siglas conhecidas (com tolerância a erros de digitação)
 e a distribuição de letras do texto comparada à do português.
 */
@Service
public class AnalisadorDeTexto {

    private static final Pattern PALAVRA = Pattern.compile("[A-Za-z]+");
    private static final Pattern SO_LETRAS = Pattern.compile("[a-z]{2,}");

    private static final String ARQUIVO_PALAVRAS = "/portugues/palavras.txt";
    private static final String ARQUIVO_SIGLAS = "/portugues/siglas.txt";

    private static final int TAMANHO_MINIMO_SIGLA = 3;
    private static final int TAMANHO_MINIMO_PARA_TOLERANCIA = 4;

    // frequência aproximada das letras no português (em %)
    private static final double[] FREQUENCIA_PORTUGUES = {
            14.63, 1.04, 3.88, 4.99, 12.57, 1.02, 1.30, 1.28, 6.18, 0.40, 0.02, 2.78, 4.74,
            5.05, 10.73, 2.52, 1.20, 6.53, 7.81, 4.34, 4.63, 1.67, 0.01, 0.21, 0.01, 0.47
    };

    private final Set<String> palavrasConhecidas;
    private final List<String> siglasPadrao;

    public AnalisadorDeTexto() {
        this.palavrasConhecidas = carregarPalavras();
        this.siglasPadrao = carregarSiglas();
    }

    // junta as siglas padrão com as siglas informadas na requisição (sem duplicatas e normalizadas)
    public List<String> siglasDisponiveis(List<String> siglasExtras) {
        Set<String> siglas = new LinkedHashSet<>(siglasPadrao);
        if (siglasExtras != null) {
            for (String sigla : siglasExtras) {
                if (sigla != null && !sigla.isBlank()) {
                    String normalizada = normalizarSigla(sigla);
                    if (normalizada.length() >= TAMANHO_MINIMO_SIGLA) {
                        siglas.add(normalizada);
                    }
                }
            }
        }
        return new ArrayList<>(siglas);
    }

    public Analise analisar(String texto, List<String> siglas, int tolerancia) {
        String semAcento = removerAcentos(texto);
        boolean temMinusculas = semAcento.chars().anyMatch(Character::isLowerCase);

        double pontuacao = 0;
        Set<String> siglasEncontradas = new LinkedHashSet<>();
        Set<String> palavrasReconhecidas = new LinkedHashSet<>();

        Matcher matcher = PALAVRA.matcher(semAcento);
        while (matcher.find()) {
            String token = matcher.group();
            String minusculo = token.toLowerCase(Locale.ROOT);

            // 1) Palavra do português (aceita plural simples)
            boolean palavraConhecida = palavrasConhecidas.contains(minusculo)
                    || (minusculo.length() > 3 && minusculo.endsWith("s")
                    && palavrasConhecidas.contains(minusculo.substring(0, minusculo.length() - 1)));
            if (palavraConhecida) {
                pontuacao += 1.0 + Math.min(minusculo.length(), 8) * 0.15;
                palavrasReconhecidas.add(minusculo);
            }

            // 2) Sigla conhecida, com tolerância para maiúsculas/minúsculas e pequenos erros de digitação
            if (token.length() >= TAMANHO_MINIMO_SIGLA) {
                String maiusculo = token.toUpperCase(Locale.ROOT);
                boolean tokenEmMaiusculas = token.equals(maiusculo);

                String siglaExata = null;
                for (String sigla : siglas) {
                    if (maiusculo.equals(sigla)) {
                        siglaExata = sigla;
                        break;
                    }
                }

                if (siglaExata != null) {
                    if (tokenEmMaiusculas) {
                        pontuacao += 8.0 + 4.0 * siglaExata.length();
                    } else {
                        // "ifsp" minúsculo ainda é reconhecido, só que com menos peso
                        pontuacao += 1.5 + 1.0 * siglaExata.length();
                    }
                    siglasEncontradas.add(siglaExata);
                } else if (tolerancia > 0) {
                    // Sem correspondência exata, tenta aproximar de alguma sigla catalogada
                    for (String sigla : siglas) {
                        if (sigla.length() < TAMANHO_MINIMO_PARA_TOLERANCIA
                                || token.length() < TAMANHO_MINIMO_PARA_TOLERANCIA
                                || !(tokenEmMaiusculas || sigla.length() >= 6)) {
                            continue;
                        }
                        int distancia = distanciaLevenshtein(sigla, maiusculo, tolerancia);
                        if (distancia > 0 && distancia <= tolerancia) {
                            double peso = tokenEmMaiusculas
                                    ? (6.0 - 1.5 * distancia) + 2.0 * sigla.length()
                                    : (2.0 - distancia) + 0.5 * sigla.length();
                            pontuacao += peso;
                            siglasEncontradas.add(sigla + " ~ " + maiusculo + " (" + distancia + " letra(s))");
                        }
                    }
                }

                // 3) Formato de sigla não catalogada: bloco em maiúsculas no meio de um texto com minúsculas
                if (siglaExata == null && temMinusculas && tokenEmMaiusculas && token.length() <= 6) {
                    pontuacao += 2.5;
                }
            }
        }

        pontuacao += bonusFrequenciaLetras(semAcento);

        return new Analise(arredondar(pontuacao), new ArrayList<>(siglasEncontradas),
                new ArrayList<>(palavrasReconhecidas));
    }

    /**
     Compara a distribuição de letras com a do português
     Serve como critério de desempate
     */
    private double bonusFrequenciaLetras(String texto) {
        int[] contagem = new int[26];
        long total = 0;
        for (int i = 0; i < texto.length(); i++) {
            char caractere = Character.toLowerCase(texto.charAt(i));
            if (caractere >= 'a' && caractere <= 'z') {
                contagem[caractere - 'a']++;
                total++;
            }
        }
        if (total < 8) {
            return 0;
        }

        double quiQuadrado = 0;
        for (int i = 0; i < 26; i++) {
            double esperado = FREQUENCIA_PORTUGUES[i] / 100.0 * total;
            if (esperado > 0) {
                double diferenca = contagem[i] - esperado;
                quiQuadrado += diferenca * diferenca / esperado;
            }
        }

        double porLetra = quiQuadrado / total;
        double bonus = 4.0 / (1.0 + porLetra);
        return bonus * Math.min(1.0, total / 40.0);
    }

    private int distanciaLevenshtein(String a, String b, int limite) {
        if (Math.abs(a.length() - b.length()) > limite) {
            return limite + 1;
        }

        int n = a.length();
        int m = b.length();
        int[][] distancias = new int[n + 1][m + 1];
        for (int i = 0; i <= n; i++) {
            distancias[i][0] = i;
        }
        for (int j = 0; j <= m; j++) {
            distancias[0][j] = j;
        }

        for (int i = 1; i <= n; i++) {
            int menorDaLinha = Integer.MAX_VALUE;
            for (int j = 1; j <= m; j++) {
                int custo = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                int valor = Math.min(Math.min(distancias[i - 1][j] + 1, distancias[i][j - 1] + 1),
                        distancias[i - 1][j - 1] + custo);

                // troca de duas letras vizinhas conta como um único erro (ex.: IFPS -> IFSP)
                if (i > 1 && j > 1 && a.charAt(i - 1) == b.charAt(j - 2) && a.charAt(i - 2) == b.charAt(j - 1)) {
                    valor = Math.min(valor, distancias[i - 2][j - 2] + 1);
                }

                distancias[i][j] = valor;
                menorDaLinha = Math.min(menorDaLinha, valor);
            }
            if (menorDaLinha > limite) {
                return limite + 1;
            }
        }
        return distancias[n][m];
    }

    private Set<String> carregarPalavras() {
        Set<String> palavras = new HashSet<>();
        for (String linha : lerRecurso(ARQUIVO_PALAVRAS)) {
            String palavra = removerAcentos(linha).trim().toLowerCase(Locale.ROOT);
            if (SO_LETRAS.matcher(palavra).matches()) {
                palavras.add(palavra);
            }
        }
        return palavras;
    }

    private List<String> carregarSiglas() {
        List<String> siglas = new ArrayList<>();
        for (String linha : lerRecurso(ARQUIVO_SIGLAS)) {
            String sigla = normalizarSigla(linha);
            if (sigla.length() >= TAMANHO_MINIMO_SIGLA) {
                siglas.add(sigla);
            }
        }
        return siglas;
    }

    private List<String> lerRecurso(String caminho) {
        InputStream stream = getClass().getResourceAsStream(caminho);
        if (stream == null) {
            throw new IllegalStateException("Recurso não encontrado no classpath: " + caminho);
        }

        List<String> linhas = new ArrayList<>();
        try (BufferedReader leitor = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String linha;
            while ((linha = leitor.readLine()) != null) {
                String limpa = linha.trim();
                if (!limpa.isEmpty() && !limpa.startsWith("#")) {
                    linhas.add(limpa);
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Falha ao ler o recurso " + caminho, e);
        }
        return linhas;
    }

    private String normalizarSigla(String sigla) {
        return removerAcentos(sigla).replaceAll("[^A-Za-z]", "").toUpperCase(Locale.ROOT);
    }

    private String removerAcentos(String texto) {
        return Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
    }

    private double arredondar(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }

    public record Analise(double pontuacao, List<String> siglasEncontradas, List<String> palavrasReconhecidas) {
    }
}
