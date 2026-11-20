package com.example.cifracesar.controller;

import com.example.cifracesar.dto.CifrarRequest;
import com.example.cifracesar.dto.CifrarResponse;
import com.example.cifracesar.dto.DecifrarRequest;
import com.example.cifracesar.dto.DecifrarResponse;
import com.example.cifracesar.service.CifraService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CifraController {

    private final CifraService service;

    public CifraController(CifraService service) {
        this.service = service;
    }

    @PostMapping("/cifrar")
    public CifrarResponse cifrar(@Valid @RequestBody CifrarRequest request) {
        String cifrado = service.cifrar(request.getTextoClaro(),request.getDeslocamento()); //variavel vai receber o cifrar passando o texto claro e o deslocamento do request
        CifrarResponse response = new CifrarResponse(); // vai criar um objeto de resposta
        response.setTextoCifrado(cifrado); // vai guardar o texto cifrado nesse objeto
        return response; // retorna o objeto de resposta com o texto cifrado
        // chama o service com os campos do request
        // devolve o DTO de response
    }
    @PostMapping("/decifrar")
    public DecifrarResponse decifrar(@Valid @RequestBody DecifrarRequest request) {
        String decifrado = service.decifrar(request.getTextoCifrado(), request.getDeslocamento()); // variavel vai receber o decifrar passando o texto cifrado e a chave do request
        DecifrarResponse response = new DecifrarResponse(); // vai criar um objeto de resposta
        response.setTextoClaro(decifrado); // vai guaradar o texto claro (decifrado)
        return response; // vai retornar o objeto de resposta com o texto decifrado
    }
}
