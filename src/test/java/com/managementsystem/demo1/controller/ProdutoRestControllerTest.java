package com.managementsystem.demo1.controller;

import com.managementsystem.demo1.BackendApplication;
import com.managementsystem.demo1.model.Produto;
import com.managementsystem.demo1.model.TipoProduto;
import com.managementsystem.demo1.repository.MaterialRepository;
import com.managementsystem.demo1.repository.ProdutoMaterialRepository;
import com.managementsystem.demo1.repository.ProdutoRepository;
import com.managementsystem.demo1.service.ProdutoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = BackendApplication.class)
@AutoConfigureMockMvc
class ProdutoRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProdutoService produtoService;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private MaterialRepository materialRepository;

    @Autowired
    private ProdutoMaterialRepository produtoMaterialRepository;

    @BeforeEach
    void setUp() {
        produtoMaterialRepository.deleteAll();
        produtoRepository.deleteAll();
        materialRepository.deleteAll();
    }

    private Produto cadastrarProduto(String codigo, String nome, TipoProduto tipo, Integer estoque, Integer prazo) {
        return produtoService.cadastrarComMateriais(
                codigo,
                nome,
                "Descrição " + nome,
                tipo,
                estoque,
                prazo,
                tipo == TipoProduto.PERSONALIZADO ? "Sob demanda" : null,
                true,
                new BigDecimal("50.00"),
                List.of()
        );
    }

    @Test
    void deveListarTodosOsProdutosViaGet() throws Exception {
        cadastrarProduto("P-001", "Terço de madeira", TipoProduto.PRE_PRONTO, 10, null);
        cadastrarProduto("P-002", "Terço personalizado azul", TipoProduto.PERSONALIZADO, null, 7);

        mockMvc.perform(get("/produtos"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].nome", is("Terço de madeira")))
                .andExpect(jsonPath("$[1].nome", is("Terço personalizado azul")));
    }

    @Test
    void deveFiltrarProdutosPorNomeViaParametroGet() throws Exception {
        cadastrarProduto("P-001", "Terço de madeira", TipoProduto.PRE_PRONTO, 10, null);
        cadastrarProduto("P-002", "Pulseira de ouro", TipoProduto.PRE_PRONTO, 2, null);

        mockMvc.perform(get("/produtos").param("nome", "madeira"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nome", is("Terço de madeira")));
    }

    @Test
    void deveBuscarProdutoPorNomeViaRotaBuscar() throws Exception {
        cadastrarProduto("P-001", "Rosário vermelho", TipoProduto.PRE_PRONTO, 5, null);
        cadastrarProduto("P-002", "Rosário azul", TipoProduto.PERSONALIZADO, null, 10);

        mockMvc.perform(get("/produtos/buscar").param("nome", "vermelho"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nome", is("Rosário vermelho")));
    }

    @Test
    void deveBuscarProdutoPorIdComSucesso() throws Exception {
        Produto produto = cadastrarProduto("P-001", "Terço de madeira", TipoProduto.PRE_PRONTO, 10, null);

        mockMvc.perform(get("/produtos/" + produto.getIdProduto()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idProduto", is(produto.getIdProduto().intValue())))
                .andExpect(jsonPath("$.codigo", is("P-001")))
                .andExpect(jsonPath("$.nome", is("Terço de madeira")));
    }

    @Test
    void deveRetornar404AoBuscarProdutoPorIdInexistente() throws Exception {
        mockMvc.perform(get("/produtos/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveConsultarTotalEmEstoque() throws Exception {
        cadastrarProduto("P-001", "Terço de madeira", TipoProduto.PRE_PRONTO, 10, null);
        cadastrarProduto("P-002", "Pulseira de prata", TipoProduto.PRE_PRONTO, 15, null);
        cadastrarProduto("P-003", "Personalizado", TipoProduto.PERSONALIZADO, null, 5);

        mockMvc.perform(get("/produtos/total-estoque"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total", is(25)));
    }

    @Test
    void deveExcluirProdutoExistenteRetornando204() throws Exception {
        Produto produto = cadastrarProduto("P-DEL", "Para Deletar", TipoProduto.PRE_PRONTO, 5, null);

        mockMvc.perform(delete("/produtos/" + produto.getIdProduto()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/produtos/" + produto.getIdProduto()))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornar404AoExcluirProdutoInexistente() throws Exception {
        mockMvc.perform(delete("/produtos/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornar400AoCadastrarProdutoComCodigoDuplicado() throws Exception {
        cadastrarProduto("P-DUP", "Original", TipoProduto.PRE_PRONTO, 5, null);

        String json = """
                {
                    "codigo": "P-DUP",
                    "nome": "Duplicado",
                    "descricao": "Desc",
                    "tipo": "PRE_PRONTO",
                    "quantidadeEstoque": 10,
                    "calcularManualmente": true,
                    "valorManual": 30.00,
                    "materiais": []
                }
                """;

        mockMvc.perform(post("/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", containsString("Já existe um produto com o código informado")));
    }

    @Test
    void deveRetornar404AoAtualizarProdutoInexistente() throws Exception {
        String json = """
                {
                    "codigo": "P-NOVO",
                    "nome": "Inexistente",
                    "descricao": "Desc",
                    "quantidadeEstoque": 10
                }
                """;

        mockMvc.perform(put("/produtos/999999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornar400AoAtualizarProdutoComCodigoDuplicado() throws Exception {
        cadastrarProduto("P-01", "Produto 1", TipoProduto.PRE_PRONTO, 5, null);
        Produto p2 = cadastrarProduto("P-02", "Produto 2", TipoProduto.PRE_PRONTO, 5, null);

        String json = """
                {
                    "codigo": "P-01",
                    "nome": "Produto 2 com codigo do 1",
                    "descricao": "Desc",
                    "quantidadeEstoque": 10
                }
                """;

        mockMvc.perform(put("/produtos/" + p2.getIdProduto())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", containsString("Já existe outro produto cadastrado")));
    }
}
