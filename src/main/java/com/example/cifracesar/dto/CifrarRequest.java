package com.example.cifracesar.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CifrarRequest {

    @NotBlank(message = "O texto claro é obrigatório!")
    @Size(max = 255, message = "O texto claro deve ter no máximo 255 caracteres!")
    private String textoClaro;

    private int deslocamento;

    public String getTextoClaro() {
        return textoClaro;
    }

    public void setTextoClaro(String textoClaro) {
        this.textoClaro = textoClaro;
    }

    public int getDeslocamento() {
        return deslocamento;
    }

    public void setDeslocamento(int deslocamento) {
        this.deslocamento = deslocamento;
    }
}
