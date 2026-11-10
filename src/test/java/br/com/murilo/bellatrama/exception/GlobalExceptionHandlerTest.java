package br.com.murilo.bellatrama.exception;

import br.com.murilo.bellatrama.dominio.foto.exception.FotoNaoEncontradaException;
import br.com.murilo.bellatrama.dominio.foto.exception.FotoPrincipalNaoPodeSerDesmarcadaException;
import br.com.murilo.bellatrama.dominio.produto.exception.ProdutoNaoEncontradoException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = GlobalExceptionHandlerTest.TestController.class)
@Import(GlobalExceptionHandlerTest.TestController.class)
@ExtendWith(OutputCaptureExtension.class)
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

        @PostMapping("/teste/erro-metodo-nao-permitido")
        public String testeMetodoNaoPermitido() {
            return "Método permitido";
        }

        @PostMapping("/teste/erro-uuid-invalido/{id}")
        public String testeUuidInvalido(@PathVariable UUID id) {
            return "sucesso, é UUID";
        }

        @GetMapping("/teste/erro-produto-nao-encontrado")
        public String testeProdutoNaoEncontrado() {
            throw new ProdutoNaoEncontradoException("Mensagem de teste");
        }

        @GetMapping("/teste/erro-foto-nao-encontrada")
        public String testeFotoNaoEncontrada() {
            throw new FotoNaoEncontradaException("Mensagem de teste");
        }

        @PutMapping("/teste/erro-desmarcar-foto-principal")
        public String testeDesmarcarFotoPrincipal() {
            throw new FotoPrincipalNaoPodeSerDesmarcadaException("Mensagem de teste");
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
    void deveManterStatus5xxDaExcecaoDoSpringSemExporMotivo(CapturedOutput output) throws Exception {

        mockMvc.perform(post("/teste/erro-unavailable"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503))
                .andExpect(jsonPath("$.mensagem").value("Erro ao processar a requisição."))
                .andExpect(content().string(not(containsString(MOTIVO_SENSIVEL))));
        Assertions.assertThat(output).contains("Erro interno ao processar a requisição.");
    }

    @Test
    @DisplayName("JSON malformado retorna bad request")
    void deveRetornarBadRequestQuandoJsonForMalformado(CapturedOutput output) throws Exception {
        String json = """
                {
                    "descricao: Teste descrição mal formatada,
                }
                """;

        mockMvc.perform(post("/teste/validacao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.mensagem").value("O corpo da requisição está inválido ou em formato incompatível."));
        Assertions.assertThat(output).contains("Corpo inválido");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("fornecerCenarios")
    void deveMapearExcecaoParaRespostaPadronizada(
            String descricao,
            HttpMethod httpMethod,
            String url,
            int statusEsperado,
            String mensagemEsperada,
            String contentType,
            String body
    ) throws Exception {

        MockHttpServletRequestBuilder request =
                MockMvcRequestBuilders.request(httpMethod, url);

        if (contentType != null) {
            request.contentType(contentType);
        }

        if (body != null) {
            request.content(body);
        }

        mockMvc.perform(request)
                .andExpect(status().is(statusEsperado))
                .andExpect(jsonPath("$.status").value(statusEsperado))
                .andExpect(jsonPath("$.mensagem").value(mensagemEsperada));
    }

    static Stream<Arguments> fornecerCenarios() {
        return Stream.of(

                Arguments.of(
                        "405 - Método HTTP não permitido",
                        HttpMethod.GET,
                        "/teste/erro-metodo-nao-permitido",
                        405,
                        "O método HTTP utilizado não é permitido para este recurso.",
                        null,
                        null
                ),

                Arguments.of(
                        "415 - Content-Type não suportado",
                        HttpMethod.POST,
                        "/teste/validacao",
                        415,
                        "Tipo de conteúdo não suportado.",
                        MediaType.TEXT_PLAIN_VALUE,
                        "Texto Simples"
                ),

                Arguments.of(
                        "400 - UUID inválido",
                        HttpMethod.POST,
                        "/teste/erro-uuid-invalido/1a",
                        400,
                        "Um ou mais parâmetros da requisição possuem um tipo ou formato inválido.",
                        null,
                        null
                ),

                Arguments.of(
                        "404 - Produto não encontrado",
                        HttpMethod.GET,
                        "/teste/erro-produto-nao-encontrado",
                        404,
                        "Mensagem de teste",
                        null,
                        null
                ),

                Arguments.of(
                        "404 - Foto não encontrada",
                        HttpMethod.GET,
                        "/teste/erro-foto-nao-encontrada",
                        404,
                        "Mensagem de teste",
                        null,
                        null
                ),

                Arguments.of(
                        "400 - Foto principal não pode ser desmarcada",
                        HttpMethod.PUT,
                        "/teste/erro-desmarcar-foto-principal",
                        400,
                        "Mensagem de teste",
                        null,
                        null
                ),

                Arguments.of(
                        "404 - Rota inexistente",
                        HttpMethod.POST,
                        "/teste/rota-que-nao-existe",
                        404,
                        "Recurso não encontrado.",
                        null,
                        null
                )
        );
    }

}