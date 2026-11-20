package com.example.cifracesar.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class DecifrarRequest {

    @NotBlank(message = "O texto cifrado é obrigatório!")
    @Size(max = 255, message = "O texto cifrado deve ter no máximo 255 caracteres!")
    private String textoCifrado;

    @NotNull(message = "O deslocamento é obrigatório!")
    private Integer deslocamento;

    public String getTextoCifrado() {
        return textoCifrado;
    }

    public void setTextoCifrado(String textoCifrado) {
        this.textoCifrado = textoCifrado;
    }

    public Integer getDeslocamento() {
        return deslocamento;
    }

    public void setDeslocamento(Integer deslocamento) {
        this.deslocamento = deslocamento;
    }
}