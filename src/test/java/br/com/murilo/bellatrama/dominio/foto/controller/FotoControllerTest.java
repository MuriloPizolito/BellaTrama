package br.com.murilo.bellatrama.dominio.foto.controller;

import br.com.murilo.bellatrama.dominio.foto.dto.FotoRequest;
import br.com.murilo.bellatrama.dominio.foto.dto.FotoResponse;
import br.com.murilo.bellatrama.dominio.foto.service.FotoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.endsWith;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FotoController.class)
class FotoControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    FotoService fotoService;

    private static final String URL_FOTO = "https://exemplo.com/foto-1.jpg";

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