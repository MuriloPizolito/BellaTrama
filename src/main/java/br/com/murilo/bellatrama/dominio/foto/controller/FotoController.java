package br.com.murilo.bellatrama.dominio.foto.controller;

import br.com.murilo.bellatrama.dominio.foto.dto.FotoRequest;
import br.com.murilo.bellatrama.dominio.foto.dto.FotoResponse;
import br.com.murilo.bellatrama.dominio.foto.service.FotoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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

    @GetMapping("/produto/{produtoId}/fotos")
    public ResponseEntity<List<FotoResponse>> listar(@PathVariable("produtoId") UUID produtoId) {
        List<FotoResponse> fotoResponseList = fotoService.listarFotos(produtoId);

        return ResponseEntity.ok(fotoResponseList);
    }

    @GetMapping("/produto/{produtoId}/fotos/{fotoId}")
    public ResponseEntity<FotoResponse> listarPorId(@PathVariable("produtoId") UUID produtoId, @PathVariable("fotoId") UUID fotoId) {
        FotoResponse fotoResponse = fotoService.buscarPorId(produtoId, fotoId);

        return ResponseEntity.ok(fotoResponse);
    }

}
