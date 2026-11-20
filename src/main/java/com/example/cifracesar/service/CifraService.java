package com.example.cifracesar.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class CifraService {

    private static final int TAMANHO_ALFABETO = 26;

    private final AnalisadorDeTexto analisador;

    public CifraService(AnalisadorDeTexto analisador) {
        this.analisador = analisador;
    }

    public String cifrar(String textoClaro, int deslocamento) {
        return deslocar(textoClaro, deslocamento);
    }

    public String decifrar(String textoCifrado, int deslocamento) {
        return deslocar(textoCifrado, -deslocamento);
    }

     // testa todas as 26 chaves possíveis e devolve os candidatos ordenados do mais provável para o menos provável.
    public List<Candidato> forcaBruta(String textoCifrado, List<String> siglasExtras, int tolerancia) {
        if (textoCifrado == null || textoCifrado.isBlank()) {
            throw new IllegalArgumentException("O texto cifrado é obrigatório!");
        }
        if (tolerancia < 0 || tolerancia > 2) {
            throw new IllegalArgumentException("A tolerância deve ser entre 0 e 2!");
        }

        List<String> siglas = analisador.siglasDisponiveis(siglasExtras);
        List<Candidato> candidatos = new ArrayList<>();

        for (int deslocamento = 0; deslocamento < TAMANHO_ALFABETO; deslocamento++) {
            String textoClaro = decifrar(textoCifrado, deslocamento);
            AnalisadorDeTexto.Analise analise = analisador.analisar(textoClaro, siglas, tolerancia);
            candidatos.add(new Candidato(
                    deslocamento,
                    textoClaro,
                    analise.pontuacao(),
                    analise.siglasEncontradas(),
                    analise.palavrasReconhecidas()
            ));
        }

        candidatos.sort(Comparator.comparingDouble(Candidato::pontuacao).reversed()
                .thenComparingInt(Candidato::deslocamento));
        return candidatos;
    }

    private String deslocar(String texto, int deslocamento) {
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException("O texto é obrigatório!");
        }
        int passo = Math.floorMod(deslocamento, TAMANHO_ALFABETO);
        StringBuilder resultado = new StringBuilder(texto.length());

        for (char caractere : texto.toCharArray()) {
            if (caractere >= 'A' && caractere <= 'Z') {
                resultado.append((char) ('A' + Math.floorMod(caractere - 'A' + passo, TAMANHO_ALFABETO)));
            } else if (caractere >= 'a' && caractere <= 'z') {
                resultado.append((char) ('a' + Math.floorMod(caractere - 'a' + passo, TAMANHO_ALFABETO)));
            } else {
                // números, acentos, espaços e pontuação permanecem intactos
                resultado.append(caractere);
            }
        }
        return resultado.toString();
    }

    public record Candidato(
            int deslocamento,
            String textoClaro,
            double pontuacao,
            List<String> siglasEncontradas,
            List<String> palavrasReconhecidas
    ) {
    }
}
