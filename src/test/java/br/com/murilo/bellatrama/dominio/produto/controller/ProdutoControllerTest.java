package br.com.murilo.bellatrama.dominio.produto.controller;

import br.com.murilo.bellatrama.dominio.produto.dto.ProdutoRequest;
import br.com.murilo.bellatrama.dominio.produto.dto.ProdutoResponse;
import br.com.murilo.bellatrama.dominio.produto.service.ProdutoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.hamcrest.Matchers.endsWith;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(ProdutoController.class)
class ProdutoControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    ProdutoService produtoService;

    @Nested
    @DisplayName("Cadastrar")
    class Cadastrar {

        @Test
        @DisplayName("Deve cadastrar produto com sucesso e retornar código 201 e o produto salvo")
        void deveCadastrarProdutoComSucesso() throws Exception {
            ProdutoRequest produtoRequest = criarRequest();
            String json = """
                    {
                        "nome": "Bolsa",
                        "preco": 299.90,
                        "estoque": 10,
                        "descricao": "Bolsa artesanal",
                        "cor": "Laranja",
                        "material": "Algodão"
                    }
                    """;
            ProdutoResponse produtoResponse = criarResponse();

            when(produtoService.cadastrar(produtoRequest)).thenReturn(produtoResponse);

            mockMvc.perform(post("/produtos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.ativo").doesNotExist())
                    .andExpect(jsonPath("$.id").value(produtoResponse.id().toString()))
                    .andExpect(jsonPath("$.nome").value(produtoRequest.nome()))
                    .andExpect(jsonPath("$.preco").value(produtoResponse.preco().doubleValue()))
                    .andExpect(jsonPath("$.estoque").value(produtoRequest.estoque()))
                    .andExpect(jsonPath("$.descricao").value(produtoRequest.descricao()))
                    .andExpect(jsonPath("$.cor").value(produtoRequest.cor()))
                    .andExpect(jsonPath("$.material").value(produtoRequest.material()))
                    .andExpect(header().string("Location", endsWith("/produtos/" + produtoResponse.id())));

            verify(produtoService).cadastrar(produtoRequest);
        }


    }

    private ProdutoResponse criarResponse() {
        return new ProdutoResponse(
                UUID.randomUUID(),
                "Bolsa",
                new BigDecimal("299.90"),
                10,
                "Bolsa artesanal",
                "Laranja",
                "Algodão"
        );
    }

    private ProdutoRequest criarRequest() {
        return new ProdutoRequest(
                "Bolsa",
                new BigDecimal("299.90"),
                10,
                "Bolsa artesanal",
                "Laranja",
                "Algodão"
        );
    }


}