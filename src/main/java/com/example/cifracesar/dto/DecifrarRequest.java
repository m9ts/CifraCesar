package com.example.cifracesar.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class DecifrarRequest {

    @NotBlank(message = "O texto claro é obrigatório!")
    @Size(max = 255, message = "O texto claro deve ter no máximo 255 caracteres!")
    private String textoCifrado;

    private int deslocamento;

    public String getTextoCifrado() {
        return textoCifrado;
    }

    public void setTextoCifrado(String textoCifrado) {
        this.textoCifrado = textoCifrado;
    }

    public int getDeslocamento() {
        return deslocamento;
    }

    public void setDeslocamento(int deslocamento) {
        this.deslocamento = deslocamento;
    }
}