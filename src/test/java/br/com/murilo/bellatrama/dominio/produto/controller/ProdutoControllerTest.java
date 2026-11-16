package br.com.murilo.bellatrama.dominio.produto.controller;

import br.com.murilo.bellatrama.dominio.produto.dto.ProdutoRequest;
import br.com.murilo.bellatrama.dominio.produto.dto.ProdutoResponse;
import br.com.murilo.bellatrama.dominio.produto.exception.ProdutoNaoEncontradoException;
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
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

        @Test
        @DisplayName("Deve rejeitar cadastro de produto com campos inválidos sem chamar o service")
        void deveRejeitarProdutoComCamposInvalidosSemChamarOService() throws Exception {
            ProdutoResponse produtoResponse = criarResponse();
            String json = """
                    {
                        "nome": "",
                        "preco": -299.90,
                        "estoque": 10,
                        "descricao": "Bolsa artesanal",
                        "cor": "Laranja",
                        "material": "Algodão"
                    }
                    """;

            when(produtoService.cadastrar(any())).thenReturn(produtoResponse); // any() - só existe para a quebra do @Valid ficar legível

            mockMvc.perform(post("/produtos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.mensagem").value("Existem campos inválidos na requisição."))
                    .andExpect(jsonPath("$.erros.nome").value("O nome não pode ser nulo ou em branco."))
                    .andExpect(jsonPath("$.erros.preco").value("O preço deve ser maior que zero."))
                    .andExpect(jsonPath("$.erros.estoque").doesNotExist());

            verifyNoInteractions(produtoService);
        }

    }

    @Nested
    @DisplayName("Listar")
    class Listar {

        @Test
        @DisplayName("Deve listar todos os produtos cadastrados")
        void deveListarTodosOsProdutosCadastrados() throws Exception {
            ProdutoResponse produto1 = criarResponse("Bolsa 1", BigDecimal.valueOf(150.90), 2, "Bolsa artesanal", "Preta", "Algodão");
            ProdutoResponse produto2 = criarResponse("Bolsa 2", BigDecimal.valueOf(200), 4, "Bolsa de praia", "Amarela", "Poliéster");
            List<ProdutoResponse> produtoResponses = List.of(produto1, produto2);

            when(produtoService.listarProdutos()).thenReturn(produtoResponses);

            mockMvc.perform(get("/produtos"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].nome").value("Bolsa 1"))
                    .andExpect(jsonPath("$[1].nome").value("Bolsa 2"))
                    .andExpect(jsonPath("$[0].ativo").doesNotExist())
                    .andExpect(jsonPath("$[1].ativo").doesNotExist());
        }

        @Test
        @DisplayName("Deve retornar uma lista vazia de produtos")
        void deveRetornarUmaListaVaziaDeProdutos() throws Exception {
            List<ProdutoResponse> produtoResponses = List.of();

            when(produtoService.listarProdutos()).thenReturn(produtoResponses);

            mockMvc.perform(get("/produtos"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$").isEmpty());
        }

    }

    @Nested
    @DisplayName("Obter por id")
    class ObterPorId {

        @Test
        @DisplayName("Deve retornar produto encontrado pelo id")
        void deveRetornarProdutoEncontradoPorId() throws Exception {
            ProdutoResponse produtoResponse = criarResponse();

            when(produtoService.buscarPorId(produtoResponse.id())).thenReturn(produtoResponse);

            mockMvc.perform(get("/produtos/{id}", produtoResponse.id()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(produtoResponse.id().toString()))
                    .andExpect(jsonPath("$.nome").value(produtoResponse.nome()))
                    .andExpect(jsonPath("$.preco").value(produtoResponse.preco().doubleValue()))
                    .andExpect(jsonPath("$.estoque").value(produtoResponse.estoque()))
                    .andExpect(jsonPath("$.descricao").value(produtoResponse.descricao()))
                    .andExpect(jsonPath("$.cor").value(produtoResponse.cor()))
                    .andExpect(jsonPath("$.material").value(produtoResponse.material()));

            verify(produtoService).buscarPorId(produtoResponse.id());
            verifyNoMoreInteractions(produtoService);
        }

        @Test
        @DisplayName("Deve retornar 404 no formato padronizado quando o produto não existe")
        void deveRetornarProdutoNaoEncontradoPorId() throws Exception {
            UUID idInexistente = UUID.randomUUID();

            when(produtoService.buscarPorId(idInexistente)).thenThrow(new ProdutoNaoEncontradoException("Produto não encontrado!"));

            mockMvc.perform(get("/produtos/{id}", idInexistente))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.mensagem").value("Produto não encontrado!"));

            verify(produtoService).buscarPorId(idInexistente);
            verifyNoMoreInteractions(produtoService);
        }

    }

    private ProdutoResponse criarResponse(String nome, BigDecimal preco, Integer estoque, String descricao, String cor, String material) {
        return new ProdutoResponse(
                UUID.randomUUID(),
                nome,
                preco,
                estoque,
                descricao,
                cor,
                material
        );
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