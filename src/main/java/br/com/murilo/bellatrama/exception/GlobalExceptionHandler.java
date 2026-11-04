package br.com.murilo.bellatrama.exception;

import br.com.murilo.bellatrama.dominio.foto.exception.FotoNaoEncontradaException;
import br.com.murilo.bellatrama.dominio.foto.exception.FotoPrincipalNaoPodeSerDesmarcadaException;
import br.com.murilo.bellatrama.dominio.produto.exception.ProdutoNaoEncontradoException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ProdutoNaoEncontradoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse produtoNaoEncontrado(ProdutoNaoEncontradoException exception) {
        return new ErrorResponse(HttpStatus.NOT_FOUND.value(), exception.getMessage(), null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse camposInvalidos(MethodArgumentNotValidException exception) {
        Map<String, String> erros = exception.getBindingResult().getFieldErrors()
                .stream().sorted(Comparator.comparing(this::obterMensagem)).collect(Collectors.toMap(
                        FieldError::getField,
                        this::obterMensagem, (primeira, segunda) -> primeira + " / " + segunda
                ));
        return new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Existem campos inválidos na requisição.", erros);
    }

    @ExceptionHandler(FotoNaoEncontradaException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse fotoNaoEncontrada(FotoNaoEncontradaException exception) {
        return new ErrorResponse(HttpStatus.NOT_FOUND.value(), exception.getMessage(), null);
    }

    @ExceptionHandler(FotoPrincipalNaoPodeSerDesmarcadaException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse fotoPrincipalNaoPodeSerDesmarcada(FotoPrincipalNaoPodeSerDesmarcadaException exception) {
        return new ErrorResponse(HttpStatus.BAD_REQUEST.value(), exception.getMessage(), null);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse argumentoComTipoInvalido(MethodArgumentTypeMismatchException exception) {
        return new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Um ou mais parâmetros da requisição possuem um tipo ou formato inválido.", null);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse corpoDaRequisicaoInvalido(HttpMessageNotReadableException exception) {
        log.warn("Corpo inválido: {}", exception.getMessage());
        return new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "O corpo da requisição está inválido ou em formato incompatível.", null);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public ErrorResponse metodoHttpNaoPermitido(HttpRequestMethodNotSupportedException exception) {
        return new ErrorResponse(HttpStatus.METHOD_NOT_ALLOWED.value(), "O método HTTP utilizado não é permitido para este recurso.", null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> erroInterno(Exception exception) {
        if (exception instanceof org.springframework.web.ErrorResponse errorResponse) {
            HttpStatusCode statusCode = errorResponse.getStatusCode();
            var status = statusCode.value();
            var mensagem = switch (status) {
                case 400 -> "Dados da requisição inválidos.";
                case 404 -> "Recurso não encontrado.";
                case 405 -> "Método HTTP não permitido.";
                case 415 -> "Tipo de conteúdo não suportado.";
                default -> "Erro ao processar a requisição.";
            };

            if (statusCode.is5xxServerError()) {
                log.error("Erro interno ao processar a requisição.", exception);
            }

            HttpHeaders headers = errorResponse.getHeaders();
            return ResponseEntity.status(status).headers(headers).body(new ErrorResponse(status, mensagem, null));
        }
        log.error("Erro interno ao processar a requisição.", exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse(500, "Ocorreu um erro interno ao processar sua solicitação.", null));
    }

    private String obterMensagem(FieldError fieldError) {
        return Objects.requireNonNullElse(
                fieldError.getDefaultMessage(),
                "Mensagem de validação não informada"
        );
    }

}
