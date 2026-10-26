package com.managementsystem.demo1.controller;

import com.managementsystem.demo1.BackendApplication;
import com.managementsystem.demo1.model.Material;
import com.managementsystem.demo1.model.Produto;
import com.managementsystem.demo1.model.TipoProduto;
import com.managementsystem.demo1.repository.MaterialRepository;
import com.managementsystem.demo1.repository.ProdutoMaterialRepository;
import com.managementsystem.demo1.repository.ProdutoRepository;
import com.managementsystem.demo1.service.ItemMaterialInput;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = BackendApplication.class)
@AutoConfigureMockMvc
class EstoqueRestControllerTest {

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

    private Material criarMaterial(String nome, String preco, String unidade) {
        Material material = new Material();
        material.setNome(nome);
        material.setPrecoUnidade(new BigDecimal(preco));
        material.setUnidadeMedida(unidade);
        return materialRepository.save(material);
    }

    private Produto criarProdutoPrePronto(String codigo, String nome, int quantidadeEstoque, String valorVenda) {
        return produtoService.cadastrarComMateriais(
                codigo,
                nome,
                "Descrição do " + nome,
                TipoProduto.PRE_PRONTO,
                quantidadeEstoque,
                null,
                null,
                true,
                new BigDecimal(valorVenda),
                List.of()
        );
    }

    private Produto criarProdutoPersonalizado(String codigo, String nome, int prazoDias, String valorVenda) {
        return produtoService.cadastrarComMateriais(
                codigo,
                nome,
                "Descrição do " + nome,
                TipoProduto.PERSONALIZADO,
                null,
                prazoDias,
                "Sob encomenda",
                true,
                new BigDecimal(valorVenda),
                List.of()
        );
    }

    @Test
    void deveListarTodosOsProdutosDoEstoqueViaGet() throws Exception {
        criarProdutoPrePronto("EST-001", "Terço de madeira", 10, "25.00");
        criarProdutoPrePronto("EST-002", "Pulseira de contas", 15, "12.00");
        criarProdutoPersonalizado("PER-001", "Terço personalizado sob encomenda", 7, "50.00");

        mockMvc.perform(get("/estoque"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].nome", is("Terço de madeira")))
                .andExpect(jsonPath("$[0].quantidadeEstoque", is(10)))
                .andExpect(jsonPath("$[0].tipo", is("PRE_PRONTO")))
                .andExpect(jsonPath("$[1].nome", is("Pulseira de contas")))
                .andExpect(jsonPath("$[1].quantidadeEstoque", is(15)))
                .andExpect(jsonPath("$[1].tipo", is("PRE_PRONTO")));
    }

    @Test
    void naoDeveRetornarProdutosPersonalizadosNoEstoque() throws Exception {
        criarProdutoPersonalizado("PER-001", "Terço personalizado", 10, "60.00");

        mockMvc.perform(get("/estoque"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void deveRetornarArrayVazioQuandoEstoqueEstiverVazio() throws Exception {
        mockMvc.perform(get("/estoque"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void deveFiltrarProdutosDoEstoquePorNomeViaParametroGet() throws Exception {
        criarProdutoPrePronto("EST-001", "Terço de madeira", 10, "25.00");
        criarProdutoPrePronto("EST-002", "Pulseira de resina", 8, "15.00");

        mockMvc.perform(get("/estoque").param("nome", "madeira"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].codigo", is("EST-001")))
                .andExpect(jsonPath("$[0].nome", is("Terço de madeira")));
    }

    @Test
    void deveBuscarProdutoDoEstoquePorNomeViaRotaBuscar() throws Exception {
        criarProdutoPrePronto("EST-001", "Terço de madeira", 10, "25.00");
        criarProdutoPrePronto("EST-002", "Pulseira de resina", 8, "15.00");

        mockMvc.perform(get("/estoque/buscar").param("nome", "resina"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].codigo", is("EST-002")))
                .andExpect(jsonPath("$[0].nome", is("Pulseira de resina")));
    }

    @Test
    void deveBuscarProdutoDoEstoquePorNomeIgnorandoMaiusculasEMinusculas() throws Exception {
        criarProdutoPrePronto("EST-001", "Crucifixo Dourado", 5, "30.00");

        mockMvc.perform(get("/estoque/buscar").param("nome", "crucifixo dourado"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nome", is("Crucifixo Dourado")));
    }

    @Test
    void deveRetornarArrayVazioQuandoBuscaPorNomeNoEstoqueNaoTiverCorrespondencia() throws Exception {
        criarProdutoPrePronto("EST-001", "Terço de madeira", 10, "25.00");

        mockMvc.perform(get("/estoque/buscar").param("nome", "inexistente"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void deveRetornarTodosDoEstoqueQuandoNomeForVazioOuEmBranco() throws Exception {
        criarProdutoPrePronto("EST-001", "Terço de madeira", 10, "25.00");
        criarProdutoPrePronto("EST-002", "Pulseira de resina", 8, "15.00");

        mockMvc.perform(get("/estoque/buscar").param("nome", "   "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void deveBuscarProdutoDoEstoquePorIdComSucesso() throws Exception {
        Produto produto = criarProdutoPrePronto("EST-001", "Terço de madeira", 10, "25.00");

        mockMvc.perform(get("/estoque/" + produto.getIdProduto()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idProduto", is(produto.getIdProduto().intValue())))
                .andExpect(jsonPath("$.codigo", is("EST-001")))
                .andExpect(jsonPath("$.nome", is("Terço de madeira")))
                .andExpect(jsonPath("$.quantidadeEstoque", is(10)))
                .andExpect(jsonPath("$.tipo", is("PRE_PRONTO")));
    }

    @Test
    void deveRetornar404AoBuscarProdutoDoEstoquePorIdInexistente() throws Exception {
        mockMvc.perform(get("/estoque/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornar404AoBuscarProdutoPersonalizadoPeloEndpointDeEstoque() throws Exception {
        Produto personalizado = criarProdutoPersonalizado("PER-001", "Terço especial", 5, "75.00");

        mockMvc.perform(get("/estoque/" + personalizado.getIdProduto()))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveConsultarTotalEmEstoqueViaEndpointEstoque() throws Exception {
        criarProdutoPrePronto("EST-001", "Terço de madeira", 10, "25.00");
        criarProdutoPrePronto("EST-002", "Pulseira de contas", 15, "12.00");
        criarProdutoPersonalizado("PER-001", "Sob encomenda", 7, "50.00");

        mockMvc.perform(get("/estoque/total"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total", is(25)));

        mockMvc.perform(get("/estoque/total-estoque"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total", is(25)));
    }

    @Test
    void deveAcessarEstoquePelasRotasAlternativasCompativeis() throws Exception {
        criarProdutoPrePronto("EST-001", "Terço de madeira", 10, "25.00");

        // Rota compatível /produtos/estoque
        mockMvc.perform(get("/produtos/estoque"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nome", is("Terço de madeira")));

        // Rota compatível /estoque/produtos
        mockMvc.perform(get("/estoque/produtos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nome", is("Terço de madeira")));
    }
}
