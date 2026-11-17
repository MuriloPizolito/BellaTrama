package br.com.murilo.bellatrama.dominio.foto.controller;

import br.com.murilo.bellatrama.dominio.foto.dto.FotoRequest;
import br.com.murilo.bellatrama.dominio.foto.dto.FotoResponse;
import br.com.murilo.bellatrama.dominio.foto.exception.FotoNaoEncontradaException;
import br.com.murilo.bellatrama.dominio.foto.service.FotoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.endsWith;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FotoController.class)
class FotoControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    FotoService fotoService;

    private static final String URL_FOTO = "https://exemplo.com/foto-1.jpg";
    private static final String NOVA_URL_FOTO = "https://exemplo.com/foto-2.jpg";

    @Nested
    @DisplayName("Cadastrar")
    class Cadastrar {

        @Test
        @DisplayName("Deve cadastrar foto com sucesso e retornar código 201 e a foto salva")
        void deveCadastrarFotoComSucesso() throws Exception {
            FotoRequest fotoRequest = criarRequest();
            FotoResponse fotoResponse = criarResponse();
            UUID produtoId = UUID.randomUUID();
            String json = """
                    {
                        "url": "https://exemplo.com/foto-1.jpg",
                        "principal": false
                    }
                    """;

            when(fotoService.cadastrar(produtoId, fotoRequest)).thenReturn(fotoResponse);

            mockMvc.perform(post("/produtos/{produtoId}/fotos", produtoId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(fotoResponse.id().toString()))
                    .andExpect(jsonPath("$.url").value(fotoResponse.url()))
                    .andExpect(jsonPath("$.principal").value(fotoResponse.principal()))
                    .andExpect(header().string("Location", endsWith("/produtos/" + produtoId + "/fotos/" + fotoResponse.id())));

            verify(fotoService).cadastrar(produtoId, fotoRequest);
            verifyNoMoreInteractions(fotoService);
        }

        @Test
        @DisplayName("Deve rejeitar cadastro de foto com campos inválidos sem chamar o service")
        void deveRejeitarFotoComCamposInvalidosSemChamarOService() throws Exception {
            FotoResponse fotoResponse = criarResponse();
            UUID produtoId = UUID.randomUUID();
            String json = """
                    {
                        "url": ""
                    }
                    """;

            when(fotoService.cadastrar(any(), any())).thenReturn(fotoResponse); // any() - só existe para a quebra do @Valid ficar legível

            mockMvc.perform(post("/produtos/{produtoId}/fotos", produtoId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.mensagem").value("Existem campos inválidos na requisição."))
                    .andExpect(jsonPath("$.erros.principal").value("O campo principal é obrigatório."))
                    .andExpect(jsonPath("$.erros.url").value("A URL não pode ser nula ou em branco."))
                    .andExpect(jsonPath("$.erros", aMapWithSize(2)));

            verifyNoInteractions(fotoService);
        }

    }

    @Nested
    @DisplayName("Listar")
    class Listar {

        @Test
        @DisplayName("Deve listar todas as fotos cadastradas do produto")
        void deveListarTodosAsFotosCadastradasDoProduto() throws Exception {
            FotoResponse fotoResponse1 = new FotoResponse(UUID.randomUUID(), URL_FOTO, true);
            FotoResponse fotoResponse2 = new FotoResponse(UUID.randomUUID(), NOVA_URL_FOTO, false);
            UUID produtoId = UUID.randomUUID();

            List<FotoResponse> fotoResponses = List.of(fotoResponse1, fotoResponse2);

            when(fotoService.listarFotos(produtoId)).thenReturn(fotoResponses);

            mockMvc.perform(get("/produtos/{produtoId}/fotos", produtoId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].id").value(fotoResponse1.id().toString()))
                    .andExpect(jsonPath("$[1].id").value(fotoResponse2.id().toString()))
                    .andExpect(jsonPath("$[0].url").value(URL_FOTO))
                    .andExpect(jsonPath("$[1].url").value(NOVA_URL_FOTO))
                    .andExpect(jsonPath("$[0].principal").value(true))
                    .andExpect(jsonPath("$[1].principal").value(false));

            verify(fotoService).listarFotos(produtoId);
            verifyNoMoreInteractions(fotoService);
        }

        @Test
        @DisplayName("Deve retornar uma lista vazia de fotos do produto")
        void deveRetornarUmaListaVaziaDeFotosDoProduto() throws Exception {
            List<FotoResponse> fotoResponses = List.of();
            UUID produtoId = UUID.randomUUID();

            when(fotoService.listarFotos(produtoId)).thenReturn(fotoResponses);

            mockMvc.perform(get("/produtos/{produtoId}/fotos", produtoId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$").isEmpty());

            verify(fotoService).listarFotos(produtoId);
            verifyNoMoreInteractions(fotoService);
        }

    }

    @Nested
    @DisplayName("Buscar por id")
    class BuscarPorId {

        @Test
        @DisplayName("Deve retornar foto encontrada pelo id")
        void deveRetornarFotoEncontradaPeloId() throws Exception {
            FotoResponse fotoResponse = criarResponse();
            UUID produtoId = UUID.randomUUID();

            when(fotoService.buscarPorId(produtoId, fotoResponse.id())).thenReturn(fotoResponse);

            mockMvc.perform(get("/produtos/{produtoId}/fotos/{fotoId}", produtoId, fotoResponse.id()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(fotoResponse.id().toString()))
                    .andExpect(jsonPath("$.url").value(fotoResponse.url()))
                    .andExpect(jsonPath("$.principal").value(fotoResponse.principal()));

            verify(fotoService).buscarPorId(produtoId, fotoResponse.id());
            verifyNoMoreInteractions(fotoService);
        }

        @Test
        @DisplayName("Deve retornar 404 no formato padronizado quando a foto não existe")
        void deveRetornarFotoNaoEncontradaPeloId() throws Exception {
            UUID produtoId = UUID.randomUUID();
            UUID fotoIdInexistente = UUID.randomUUID();

            when(fotoService.buscarPorId(produtoId, fotoIdInexistente)).thenThrow(new FotoNaoEncontradaException("Foto não encontrada!"));

            mockMvc.perform(get("/produtos/{produtoId}/fotos/{fotoId}", produtoId, fotoIdInexistente))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.mensagem").value("Foto não encontrada!"));

            verify(fotoService).buscarPorId(produtoId, fotoIdInexistente);
            verifyNoMoreInteractions(fotoService);
        }

    }

    @Nested
    @DisplayName("Atualizar")
    class Atualizar {

        @Test
        @DisplayName("Deve atualizar foto pelo id")
        void deveAtualizarFotoPeloId() throws Exception {
            FotoRequest fotoRequest = new FotoRequest("https://exemplo.com/foto-1.jpg", false);
            String json = """
                    {
                        "url": "https://exemplo.com/foto-1.jpg",
                        "principal": false
                    }
                    """;
            UUID produtoId = UUID.randomUUID();
            UUID fotoId = UUID.randomUUID();

            mockMvc.perform(put("/produtos/{produtoId}/fotos/{fotoId}", produtoId, fotoId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isNoContent())
                    .andExpect(content().string(""));

            verify(fotoService).atualizar(produtoId, fotoId, fotoRequest);
            verifyNoMoreInteractions(fotoService);
        }

        @Test
        @DisplayName("Deve retornar 400 ao atualizar foto com dados inválidos")
        void deveRetornarBadRequestAoAtualizarFotoComDadosInvalidos() throws Exception {
            UUID produtoId = UUID.randomUUID();
            UUID fotoId = UUID.randomUUID();
            String json = """
                    {
                        "url": ""
                    }
                    """;

            mockMvc.perform(put("/produtos/{produtoId}/fotos/{fotoId}", produtoId, fotoId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.mensagem").value("Existem campos inválidos na requisição."))
                    .andExpect(jsonPath("$.erros.principal").value("O campo principal é obrigatório."))
                    .andExpect(jsonPath("$.erros.url").value("A URL não pode ser nula ou em branco."))
                    .andExpect(jsonPath("$.erros", aMapWithSize(2)));

            verifyNoInteractions(fotoService);
        }

    }

    @Nested
    @DisplayName("Deletar")
    class Deletar {

        @Test
        @DisplayName("Deve deletar foto pelo id")
        void deveDeletarFoto() throws Exception {
            UUID produtoId = UUID.randomUUID();
            UUID fotoId = UUID.randomUUID();

            mockMvc.perform(delete("/produtos/{produtoId}/fotos/{fotoId}", produtoId, fotoId))
                    .andExpect(status().isNoContent())
                    .andExpect(content().string(""));

            verify(fotoService).deletar(produtoId, fotoId);
            verifyNoMoreInteractions(fotoService);
        }

    }

    private FotoResponse criarResponse() {
        return new FotoResponse(
                UUID.randomUUID(),
                URL_FOTO,
                true
        );
    }

    private FotoRequest criarRequest() {
        return new FotoRequest(
                URL_FOTO,
                false
        );
    }

}