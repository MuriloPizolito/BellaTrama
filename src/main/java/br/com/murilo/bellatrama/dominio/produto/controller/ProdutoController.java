package br.com.murilo.bellatrama.dominio.produto.controller;

import br.com.murilo.bellatrama.dominio.produto.dto.ProdutoRequest;
import br.com.murilo.bellatrama.dominio.produto.dto.ProdutoResponse;
import br.com.murilo.bellatrama.dominio.produto.service.ProdutoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/produto")
@RequiredArgsConstructor
public class ProdutoController {

    private final ProdutoService service;

    // mandar o codigo daqui, mas escrever sobre os commits no github, só dei o commit initial. Como devo continuar subindo os commits, com esses códigos já feitos

    @PostMapping
    public ResponseEntity<ProdutoResponse> cadastrar(@RequestBody @Valid ProdutoRequest produtoRequest) {
        ProdutoResponse produtoSalvo = service.cadastrar(produtoRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(produtoSalvo);
    }

    @GetMapping
    public ResponseEntity<List<ProdutoResponse>> listar(){
         List<ProdutoResponse> responses = service.listarProdutos();

        return ResponseEntity.status(HttpStatus.OK).body(responses);
    }

    @GetMapping("{id}")
    public ResponseEntity<ProdutoResponse> obterPorId(@PathVariable("id")UUID id){
        ProdutoResponse response = service.buscarPorId(id);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}
