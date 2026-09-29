package br.com.murilo.bellatrama.dominio.foto.controller;

import br.com.murilo.bellatrama.dominio.foto.dto.FotoRequest;
import br.com.murilo.bellatrama.dominio.foto.dto.FotoResponse;
import br.com.murilo.bellatrama.dominio.foto.model.FotoEntity;
import br.com.murilo.bellatrama.dominio.foto.service.FotoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
//@RequestMapping
@RequiredArgsConstructor
public class FotoController {

    private final FotoService fotoService;

    @PostMapping("/produto/{produtoId}/fotos")
    public ResponseEntity<FotoResponse> cadastrar(@PathVariable("produtoId") UUID produtoId, @RequestBody @Valid FotoRequest fotoRequest) {
        FotoResponse fotoSalva = fotoService.cadastrar(produtoId, fotoRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(fotoSalva);
    }

}
