package com.example.cifracesar.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class DecifrarForcaBrutaRequest {

    @NotBlank(message = "O texto cifrado é obrigatório!")
    @Size(max = 255, message = "O texto cifrado deve ter no máximo 255 caracteres!")
    private String textoCifrado;

    public String getTextoCifrado() {
        return textoCifrado;
    }

    public void setTextoCifrado(String textoCifrado) {
        this.textoCifrado = textoCifrado;
    }
}
