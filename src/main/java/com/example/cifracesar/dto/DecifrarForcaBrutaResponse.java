package com.example.cifracesar.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class DecifrarForcaBrutaResponse {

    @Schema(maxLength = 255)
    private String textoClaro;

    public String getTextoClaro() {
        return textoClaro;
    }

    public void setTextoClaro(String textoClaro) {
        this.textoClaro = textoClaro;
    }
}
