package br.com.murilo.bellatrama.exception;

import br.com.murilo.bellatrama.dominio.produto.exception.ProdutoNaoEncontradoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ProdutoNaoEncontradoException.class)
    public ResponseEntity<Void> produtoNaoEncontrado() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

}
