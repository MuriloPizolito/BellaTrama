package br.com.murilo.bellatrama.exception;

import br.com.murilo.bellatrama.dominio.foto.exception.FotoNaoEncontradaException;
import br.com.murilo.bellatrama.dominio.produto.exception.ProdutoNaoEncontradoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ProdutoNaoEncontradoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse produtoNaoEncontrado(ProdutoNaoEncontradoException exception) {
        return new ErrorResponse(HttpStatus.NOT_FOUND.value(), exception.getMessage(), null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse camposInvalidos(MethodArgumentNotValidException e) {
        Map<String, String> erros = e.getBindingResult().getFieldErrors()
                .stream().collect(Collectors.toMap(
                        FieldError::getField,
                        FieldError::getDefaultMessage
                ));
        return new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Existem campos inválidos na requisição.", erros);
    }

    @ExceptionHandler(FotoNaoEncontradaException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse fotoNaoEncontrada(FotoNaoEncontradaException exception) {
        return new ErrorResponse(HttpStatus.NOT_FOUND.value(), exception.getMessage(), null);
    }

}
