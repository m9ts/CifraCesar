package com.example.cifracesar.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class CifrarResponse {

    @Schema(maxLength = 255)
    private String textoCifrado;

    public String getTextoCifrado() {
        return textoCifrado;
    }

    public void setTextoCifrado(String textoCifrado) {
        this.textoCifrado = textoCifrado;
    }
}
