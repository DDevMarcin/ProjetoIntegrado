package com.managementsystem.demo1.service;

import com.managementsystem.demo1.BackendApplication;
import com.managementsystem.demo1.model.Material;
import com.managementsystem.demo1.model.Produto;
import com.managementsystem.demo1.model.ProdutoMaterial;
import com.managementsystem.demo1.model.TipoProduto;
import com.managementsystem.demo1.repository.MaterialRepository;
import com.managementsystem.demo1.repository.ProdutoMaterialRepository;
import com.managementsystem.demo1.repository.ProdutoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = BackendApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
class ProdutoServiceTest {

    @Autowired
    private ProdutoService produtoService;

    @Autowired
    private MaterialRepository materialRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private ProdutoMaterialRepository produtoMaterialRepository;

    @BeforeEach
    void setUp() {
        produtoMaterialRepository.deleteAll();
        produtoRepository.deleteAll();
        materialRepository.deleteAll();
    }

    @Test
    void deveCadastrarProdutoComMaterial() {

        Material madeira = new Material();
        madeira.setNome("Contas de madeira");
        madeira.setUnidadeMedida("un");
        madeira.setPrecoUnidade(new BigDecimal("0.50"));
        madeira = materialRepository.save(madeira);

        ItemMaterialInput item = new ItemMaterialInput(
                madeira.getIdMaterial(),
                new BigDecimal("59")
        );

        Produto produto = produtoService.cadastrarComMateriais(
                "TP-001",
                "Terço de madeira",
                "Terço artesanal religioso",
                TipoProduto.PRE_PRONTO,
                10,
                null,
                null,
                false,
                null,
                List.of(item)
        );

        assertNotNull(produto.getIdProduto());
        assertEquals("TP-001", produto.getCodigo());
        assertEquals("Terço de madeira", produto.getNome());
        assertEquals("Terço artesanal religioso", produto.getDescricao());
        assertEquals(TipoProduto.PRE_PRONTO, produto.getTipo());
        assertEquals(10, produto.getQuantidadeEstoque());

        // 59 contas x R$ 0,50 = R$ 29,50
        assertEquals(
                0,
                new BigDecimal("29.50").compareTo(produto.getValor())
        );

        assertEquals(1, produto.getMateriaisUtilizados().size());

        ProdutoMaterial produtoMaterial =
                produto.getMateriaisUtilizados().get(0);

        assertEquals(
                madeira.getIdMaterial(),
                produtoMaterial.getMaterial().getIdMaterial()
        );

        assertEquals(
                0,
                new BigDecimal("59")
                        .compareTo(produtoMaterial.getQuantidadeUtilizada())
        );
    }

    @Test
    void naoDeveCadastrarProdutoSemCodigo() {

        Material material = new Material();
        material.setNome("Crucifixo");
        material.setUnidadeMedida("un");
        material.setPrecoUnidade(new BigDecimal("3.00"));
        material = materialRepository.save(material);

        ItemMaterialInput item = new ItemMaterialInput(
                material.getIdMaterial(),
                new BigDecimal("1")
        );

        assertThrows(IllegalArgumentException.class, () ->
                produtoService.cadastrarComMateriais(
                        "",
                        "Produto inválido",
                        "Descrição",
                        TipoProduto.PRE_PRONTO,
                        1,
                        null,
                        null,
                        false,
                        null,
                        List.of(item)
                )
        );
    }

    @Test
    void naoDeveCadastrarProdutoSemNome() {

        Material material = new Material();
        material.setNome("Crucifixo");
        material.setUnidadeMedida("un");
        material.setPrecoUnidade(new BigDecimal("3.00"));
        material = materialRepository.save(material);

        ItemMaterialInput item = new ItemMaterialInput(
                material.getIdMaterial(),
                new BigDecimal("1")
        );

        assertThrows(IllegalArgumentException.class, () ->
                produtoService.cadastrarComMateriais(
                        "TP-002",
                        "",
                        "Produto inválido",
                        TipoProduto.PRE_PRONTO,
                        1,
                        null,
                        null,
                        false,
                        null,
                        List.of(item)
                )
        );
    }

    @Test
    void naoDeveCadastrarProdutoSemMaterial() {

        assertThrows(IllegalArgumentException.class, () ->
                produtoService.cadastrarComMateriais(
                        "TP-003",
                        "Terço",
                        "Terço religioso",
                        TipoProduto.PRE_PRONTO,
                        10,
                        null,
                        null,
                        false,
                        null,
                        List.of()
                )
        );
    }

    @Test
    void naoDeveCadastrarComMaterialInexistente() {

        ItemMaterialInput item = new ItemMaterialInput(
                999999L,
                new BigDecimal("1")
        );

        assertThrows(java.util.NoSuchElementException.class, () ->
                produtoService.cadastrarComMateriais(
                        "TP-004",
                        "Terço",
                        "Terço religioso",
                        TipoProduto.PRE_PRONTO,
                        10,
                        null,
                        null,
                        false,
                        null,
                        List.of(item)
                )
        );
    }

    @Test
    void deveCadastrarProdutoComValorManual() {

        Produto produto = produtoService.cadastrarComMateriais(
                "TP-005",
                "Produto manual",
                "Produto com preço definido manualmente",
                TipoProduto.PRE_PRONTO,
                5,
                null,
                null,
                true,
                new BigDecimal("50.00"),
                List.of()
        );

        assertNotNull(produto.getIdProduto());
        assertEquals("TP-005", produto.getCodigo());
        assertEquals(0, new BigDecimal("50.00").compareTo(produto.getValor()));
        assertTrue(produto.getValorManual());
        assertTrue(produto.getMateriaisUtilizados().isEmpty());
    }

    @Test
    void deveListarProdutos() {

        Material material = new Material();
        material.setNome("Conta azul");
        material.setUnidadeMedida("un");
        material.setPrecoUnidade(new BigDecimal("0.50"));
        material = materialRepository.save(material);

        Produto produto = produtoService.cadastrarComMateriais(
                "TP-006",
                "Terço azul",
                "Terço artesanal azul",
                TipoProduto.PRE_PRONTO,
                5,
                null,
                null,
                false,
                null,
                List.of(
                        new ItemMaterialInput(
                                material.getIdMaterial(),
                                new BigDecimal("59")
                        )
                )
        );

        List<Produto> produtos = produtoService.listarTodos();

        assertFalse(produtos.isEmpty());

        assertTrue(
                produtos.stream()
                        .anyMatch(p ->
                                p.getIdProduto().equals(produto.getIdProduto()))
        );
    }

    @Test
    void deveBuscarProdutoPorNome() {

        Material material = new Material();
        material.setNome("Conta vermelha");
        material.setUnidadeMedida("un");
        material.setPrecoUnidade(new BigDecimal("0.50"));
        material = materialRepository.save(material);

        produtoService.cadastrarComMateriais(
                "TP-007",
                "Terço vermelho",
                "Terço artesanal vermelho",
                TipoProduto.PRE_PRONTO,
                5,
                null,
                null,
                false,
                null,
                List.of(
                        new ItemMaterialInput(
                                material.getIdMaterial(),
                                new BigDecimal("59")
                        )
                )
        );

        List<Produto> encontrados =
                produtoService.buscarPorNome("vermelho");

        assertFalse(encontrados.isEmpty());

        assertTrue(
                encontrados.stream()
                        .anyMatch(p ->
                                p.getNome().equals("Terço vermelho"))
        );
    }

    @Test
    void deveAtualizarProduto() {

        Material material = new Material();
        material.setNome("Conta branca");
        material.setUnidadeMedida("un");
        material.setPrecoUnidade(new BigDecimal("0.40"));
        material = materialRepository.save(material);

        Produto produto = produtoService.cadastrarComMateriais(
                "TP-008",
                "Terço branco",
                "Terço artesanal branco",
                TipoProduto.PRE_PRONTO,
                5,
                null,
                null,
                false,
                null,
                List.of(
                        new ItemMaterialInput(
                                material.getIdMaterial(),
                                new BigDecimal("59")
                        )
                )
        );

        Produto atualizado = produtoService.atualizar(
                produto.getIdProduto(),
                "TP-008-ALTERADO",
                "Terço branco grande",
                "Terço artesanal branco grande",
                10,
                null,
                null
        );

        assertEquals("TP-008-ALTERADO", atualizado.getCodigo());
        assertEquals("Terço branco grande", atualizado.getNome());
        assertEquals(
                "Terço artesanal branco grande",
                atualizado.getDescricao()
        );
        assertEquals(10, atualizado.getQuantidadeEstoque());
    }

    @Test
    void deveExcluirProduto() {

        Material material = new Material();
        material.setNome("Medalha");
        material.setUnidadeMedida("un");
        material.setPrecoUnidade(new BigDecimal("2.00"));
        material = materialRepository.save(material);

        Produto produto = produtoService.cadastrarComMateriais(
                "TP-009",
                "Chaveiro religioso",
                "Chaveiro com medalha",
                TipoProduto.PRE_PRONTO,
                5,
                null,
                null,
                false,
                null,
                List.of(
                        new ItemMaterialInput(
                                material.getIdMaterial(),
                                new BigDecimal("1")
                        )
                )
        );

        Long id = produto.getIdProduto();

        produtoService.excluir(id);

        assertFalse(produtoRepository.findById(id).isPresent());
    }

    @Test
    void deveListarApenasProdutosPreProntoNoEstoque() {
        produtoService.cadastrarComMateriais(
                "EST-001", "Terço de madeira", "Terço em estoque",
                TipoProduto.PRE_PRONTO, 10, null, null,
                true, new BigDecimal("25.00"), List.of()
        );
        produtoService.cadastrarComMateriais(
                "EST-002", "Pulseira de resina", "Pulseira em estoque",
                TipoProduto.PRE_PRONTO, 5, null, null,
                true, new BigDecimal("15.00"), List.of()
        );
        produtoService.cadastrarComMateriais(
                "PER-001", "Terço personalizado azul", "Terço sob encomenda",
                TipoProduto.PERSONALIZADO, null, 7, "Sob demanda",
                true, new BigDecimal("50.00"), List.of()
        );

        List<Produto> produtosEstoque = produtoService.listarEstoque();

        assertEquals(2, produtosEstoque.size());
        assertTrue(produtosEstoque.stream().allMatch(p -> p.getTipo() == TipoProduto.PRE_PRONTO));
        assertTrue(produtosEstoque.stream().anyMatch(p -> p.getCodigo().equals("EST-001")));
        assertTrue(produtosEstoque.stream().anyMatch(p -> p.getCodigo().equals("EST-002")));
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoHouverProdutosNoEstoque() {
        List<Produto> produtosEstoque = produtoService.listarEstoque();

        assertNotNull(produtosEstoque);
        assertTrue(produtosEstoque.isEmpty());
    }

    @Test
    void deveBuscarProdutoNoEstoquePorNome() {
        produtoService.cadastrarComMateriais(
                "EST-001", "Terço de madeira", "Terço em estoque",
                TipoProduto.PRE_PRONTO, 10, null, null,
                true, new BigDecimal("25.00"), List.of()
        );
        produtoService.cadastrarComMateriais(
                "EST-002", "Pulseira de resina", "Pulseira em estoque",
                TipoProduto.PRE_PRONTO, 5, null, null,
                true, new BigDecimal("15.00"), List.of()
        );

        List<Produto> encontrados = produtoService.buscarEstoquePorNome("madeira");

        assertEquals(1, encontrados.size());
        assertEquals("Terço de madeira", encontrados.get(0).getNome());
        assertEquals(TipoProduto.PRE_PRONTO, encontrados.get(0).getTipo());
    }

    @Test
    void deveBuscarProdutoNoEstoquePorNomeIgnorandoCaseEComEspacos() {
        produtoService.cadastrarComMateriais(
                "EST-001", "Crucifixo Dourado", "Crucifixo",
                TipoProduto.PRE_PRONTO, 8, null, null,
                true, new BigDecimal("35.00"), List.of()
        );

        List<Produto> encontrados = produtoService.buscarEstoquePorNome("  crucifixo dourado  ");

        assertEquals(1, encontrados.size());
        assertEquals("Crucifixo Dourado", encontrados.get(0).getNome());
    }

    @Test
    void deveRetornarTodosDoEstoqueAoBuscarPorNomeNuloOuEmBranco() {
        produtoService.cadastrarComMateriais(
                "EST-001", "Produto A", "Desc",
                TipoProduto.PRE_PRONTO, 5, null, null,
                true, new BigDecimal("10.00"), List.of()
        );
        produtoService.cadastrarComMateriais(
                "EST-002", "Produto B", "Desc",
                TipoProduto.PRE_PRONTO, 10, null, null,
                true, new BigDecimal("20.00"), List.of()
        );

        assertEquals(2, produtoService.buscarEstoquePorNome(null).size());
        assertEquals(2, produtoService.buscarEstoquePorNome("   ").size());
    }

    @Test
    void deveRetornarVazioAoBuscarPorNomeSemCorrespondenciaNoEstoque() {
        produtoService.cadastrarComMateriais(
                "EST-001", "Produto A", "Desc",
                TipoProduto.PRE_PRONTO, 5, null, null,
                true, new BigDecimal("10.00"), List.of()
        );

        List<Produto> encontrados = produtoService.buscarEstoquePorNome("inexistente");

        assertNotNull(encontrados);
        assertTrue(encontrados.isEmpty());
    }

    @Test
    void naoDeveRetornarProdutoPersonalizadoAoBuscarPorNomeNoEstoque() {
        produtoService.cadastrarComMateriais(
                "PER-001", "Terço de cristal", "Personalizado",
                TipoProduto.PERSONALIZADO, null, 10, "Sob encomenda",
                true, new BigDecimal("80.00"), List.of()
        );

        List<Produto> encontrados = produtoService.buscarEstoquePorNome("cristal");

        assertTrue(encontrados.isEmpty());
    }

    @Test
    void deveBuscarProdutoDoEstoquePorId() {
        Produto salvo = produtoService.cadastrarComMateriais(
                "EST-001", "Terço de madeira", "Desc",
                TipoProduto.PRE_PRONTO, 10, null, null,
                true, new BigDecimal("25.00"), List.of()
        );

        Optional<Produto> encontrado = produtoService.buscarEstoquePorId(salvo.getIdProduto());

        assertTrue(encontrado.isPresent());
        assertEquals("EST-001", encontrado.get().getCodigo());
        assertEquals(TipoProduto.PRE_PRONTO, encontrado.get().getTipo());
        assertEquals(10, encontrado.get().getQuantidadeEstoque());
    }

    @Test
    void deveRetornarVazioAoBuscarProdutoDoEstoquePorIdInexistente() {
        Optional<Produto> encontrado = produtoService.buscarEstoquePorId(999999L);

        assertFalse(encontrado.isPresent());
    }

    @Test
    void deveRetornarVazioAoBuscarPorIdNuloNoEstoque() {
        Optional<Produto> encontrado = produtoService.buscarEstoquePorId(null);

        assertFalse(encontrado.isPresent());
    }

    @Test
    void naoDeveEncontrarProdutoPersonalizadoAoBuscarPorIdNoEstoque() {
        Produto personalizado = produtoService.cadastrarComMateriais(
                "PER-001", "Terço especial", "Personalizado",
                TipoProduto.PERSONALIZADO, null, 5, "Sob demanda",
                true, new BigDecimal("70.00"), List.of()
        );

        Optional<Produto> encontrado = produtoService.buscarEstoquePorId(personalizado.getIdProduto());

        assertFalse(encontrado.isPresent());
    }
}