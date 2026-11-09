package br.com.murilo.bellatrama.exception;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = GlobalExceptionHandlerTest.TestController.class)
@Import(GlobalExceptionHandlerTest.TestController.class)
class GlobalExceptionHandlerTest {

    @Autowired
    MockMvc mockMvc;

    private static final String MOTIVO_SENSIVEL = "Informação sensível que não deve aparecer";

    @RestController
    static class TestController {
        @PostMapping("/teste/validacao")
        public String testarValidacao(@RequestBody @Valid TestRequest testRequest) {
            return "Dados válidos";
        }

        @PostMapping("/teste/erro-unavailable")
        public String testeServiceUnavailable() {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, MOTIVO_SENSIVEL);
        }

        @GetMapping("/teste/erro-interno")
        public String testeErroInterno() {
            throw new RuntimeException(MOTIVO_SENSIVEL);
        }
    }

    record TestRequest(
            @NotBlank(message = "A descrição é obrigatória.")
            @Size(max = 500, message = "A descrição deve ter no máximo 500 caracteres.")
            String descricao
    ) {
    }

    @Test
    @DisplayName("Campo com mais de uma violação retorna todas as mensagens")
    void deveJuntarMensagensQuandoCampoViolaMaisDeUmaValidacao() throws Exception {
        String descricao = " ".repeat(501);
        String json = """
                {
                    "descricao": "%s"
                }
                """.formatted(descricao);

        mockMvc.perform(post("/teste/validacao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.mensagem").value("Existem campos inválidos na requisição."))
                .andExpect(jsonPath("$.erros.descricao").value("A descrição deve ter no máximo 500 caracteres. / A descrição é obrigatória."));
    }

    @Test
    @DisplayName("Não deve expor detalhes internos quando ocorre uma exceção genérica")
    void deveRetornarErroInternoSemExporDetalhesDaExcecao() throws Exception {

        mockMvc.perform(get("/teste/erro-interno"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.mensagem").value("Ocorreu um erro interno ao processar sua solicitação."))
                .andExpect(content().string(not(containsString(MOTIVO_SENSIVEL))));
    }

    @Test
    @DisplayName("Deve manter o status 5xx da exceção do Spring sem expor o motivo")
    void deveManterStatus5xxDaExcecaoDoSpringSemExporMotivo() throws Exception {

        mockMvc.perform(post("/teste/erro-unavailable"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503))
                .andExpect(jsonPath("$.mensagem").value("Erro ao processar a requisição."))
                .andExpect(content().string(not(containsString(MOTIVO_SENSIVEL))));
    }



}