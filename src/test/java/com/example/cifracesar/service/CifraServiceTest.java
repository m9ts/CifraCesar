package com.example.cifracesar.service;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CifraServiceTest {

    private final CifraService service = new CifraService(new AnalisadorDeTexto());

    @Test
    void cifraEDecifraMantendoAcentosEspacosEPontuacao() {
        String texto = "Ola, mundo! Acao e coracao.";

        String cifrado = service.cifrar(texto, 3);

        assertEquals("Rod, pxqgr! Dfdr h frudfdr.", cifrado);
        assertEquals(texto, service.decifrar(cifrado, 3));
    }

    @Test
    void forcaBrutaDescobreDeslocamentoDeFrasesEmPortugues() {
        String[] frases = {
                "A PROVA DE SEGURANCA DA INFORMACAO SERA APLICADA NO INSTITUTO FEDERAL DE SAO PAULO",
                "O IFSP FICA NA CIDADE DE SAO PAULO E OFERECE CURSOS DE TECNOLOGIA",
                "AMANHA EU VOU ESTUDAR PARA A PROVA DE MATEMATICA",
                "a mensagem secreta foi enviada para o professor hoje",
                "A senha do sistema precisa ter letras e numeros para ser segura",
                "O aluno esqueceu a senha e pediu ajuda para o professor"
        };
        int[] deslocamentos = {7, 12, 25, 4, 1, 19};

        for (int i = 0; i < frases.length; i++) {
            String cifrado = service.cifrar(frases[i], deslocamentos[i]);
            List<CifraService.Candidato> candidatos = service.forcaBruta(cifrado, List.of(), 1);

            assertEquals(deslocamentos[i], candidatos.get(0).deslocamento(),
                    "Frase: " + frases[i] + "\nMelhor candidato: " + candidatos.get(0)
                            + "\nSegundo: " + candidatos.get(1));
        }
    }

    @Test
    void reconheceSiglaIfspEmMaiusculas() {
        String frase = "O IFSP FICA EM SAO PAULO";
        String cifrado = service.cifrar(frase, 9);

        List<CifraService.Candidato> candidatos = service.forcaBruta(cifrado, List.of(), 1);

        assertEquals(9, candidatos.get(0).deslocamento(), "Melhor: " + candidatos.get(0));
        assertTrue(candidatos.get(0).siglasEncontradas().contains("IFSP"));
    }

    @Test
    void reconheceSiglaIfspEmMinusculas() {
        String frase = "o ifsp fica em sao paulo";
        String cifrado = service.cifrar(frase, 3);

        List<CifraService.Candidato> candidatos = service.forcaBruta(cifrado, List.of(), 1);

        assertEquals(3, candidatos.get(0).deslocamento(), "Melhor: " + candidatos.get(0));
        assertTrue(candidatos.get(0).siglasEncontradas().contains("IFSP"));
    }

    @Test
    void reconheceSiglaComErroDeDigitacao() {
        String frase = "O IFPS FICA EM CAMPINAS";
        String cifrado = service.cifrar(frase, 17);

        List<CifraService.Candidato> candidatos = service.forcaBruta(cifrado, List.of(), 1);

        assertEquals(17, candidatos.get(0).deslocamento(), "Melhor: " + candidatos.get(0));
        assertTrue(candidatos.get(0).siglasEncontradas().stream().anyMatch(s -> s.startsWith("IFSP ~ IFPS")));
    }

    @Test
    void dicionarioCarregadoDosRecursosReconhecePalavrasComuns() {
        AnalisadorDeTexto analisador = new AnalisadorDeTexto();

        AnalisadorDeTexto.Analise analise = analisador.analisar(
                "O medico receitou um remedio para a dor de cabeca", List.of(), 1);

        assertTrue(analise.palavrasReconhecidas().containsAll(List.of("medico", "remedio", "cabeca", "dor")));
    }

    @Test
    void forcaBrutaComVocabularioMaisAmplo() {
        String frase = "O MEDICO RECEITOU UM REMEDIO PARA A DOR DE CABECA";
        String cifrado = service.cifrar(frase, 21);

        List<CifraService.Candidato> candidatos = service.forcaBruta(cifrado, List.of(), 1);

        assertEquals(21, candidatos.get(0).deslocamento(), "Melhor: " + candidatos.get(0));
    }

    @Test
    void forcaBrutaReconheceTextosComAcentuacao() {
        String[] frases = {
                "O MÉDICO RECEITOU UM REMÉDIO PARA A DOR DE CABEÇA",
                "O IFSP FICA EM SÃO PAULO",
                "A AÇÃO DE PROTEÇÃO É MUITO IMPORTANTE",
                "o relatório do serviço público já está pronto",
                "INFORMAÇÃO, SEGURANÇA E CRIPTOGRAFIA"
        };
        int[] deslocamentos = {21, 9, 15, 4, 11};

        for (int i = 0; i < frases.length; i++) {
            String cifrado = service.cifrar(frases[i], deslocamentos[i]);
            List<CifraService.Candidato> candidatos = service.forcaBruta(cifrado, List.of(), 1);

            assertEquals(deslocamentos[i], candidatos.get(0).deslocamento(),
                    "Frase: " + frases[i] + "\nMelhor candidato: " + candidatos.get(0));
            // os acentos não são deslocados, então o texto devolvido mantém a grafia original
            assertEquals(frases[i], candidatos.get(0).textoClaro());
        }
    }

    @Test
    void acentosPermanecemNoCifradoMasNaoAtrapalhamOForcaBruta() {
        String frase = "CORAÇÃO E AÇÃO SÃO PALAVRAS COM TIL E CEDILHA";
        String cifrado = service.cifrar(frase, 13);

        assertTrue(cifrado.contains("Ç"), "o Ç deve permanecer intacto: " + cifrado);
        assertTrue(cifrado.contains("Ã"), "o Ã deve permanecer intacto: " + cifrado);
        assertEquals(frase, service.decifrar(cifrado, 13));

        List<CifraService.Candidato> candidatos = service.forcaBruta(cifrado, List.of(), 1);

        assertEquals(13, candidatos.get(0).deslocamento(), "Melhor: " + candidatos.get(0));
        assertEquals(frase, candidatos.get(0).textoClaro());
    }

    @Test
    void tilSoltoEPontuacaoNaoMudamAResposta() {
        String frase = "O IFSP FICA EM SÃO PAULO";
        String cifrado = service.cifrar(frase, 9) + " ~ ?!";

        List<CifraService.Candidato> candidatos = service.forcaBruta(cifrado, List.of(), 1);

        assertEquals(9, candidatos.get(0).deslocamento(), "Melhor: " + candidatos.get(0));
    }

    @Test
    void aceitaSiglaPersonalizada() {
        String frase = "A UNIVESP OFERECE CURSOS A DISTANCIA EM TODO O ESTADO";
        String cifrado = service.cifrar(frase, 11);

        List<CifraService.Candidato> candidatos = service.forcaBruta(cifrado, List.of("UNIVESP"), 1);

        assertEquals(11, candidatos.get(0).deslocamento(), "Melhor: " + candidatos.get(0));
        assertTrue(candidatos.get(0).siglasEncontradas().contains("UNIVESP"));
    }
}
