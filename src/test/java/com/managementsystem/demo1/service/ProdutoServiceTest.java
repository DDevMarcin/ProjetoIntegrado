package com.managementsystem.demo1.service;

import com.managementsystem.demo1.BackendApplication;
import com.managementsystem.demo1.model.Material;
import com.managementsystem.demo1.model.Produto;
import com.managementsystem.demo1.model.ProdutoMaterial;
import com.managementsystem.demo1.model.TipoProduto;
import com.managementsystem.demo1.repository.MaterialRepository;
import com.managementsystem.demo1.repository.ProdutoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;

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

    @Test
    void deveCadastrarProdutoComMaterial() {

        Material madeira = new Material();
        madeira.setNome("Contas de madeira");
        madeira.setUnidadeMedida("un");
        madeira.setPrecoUnidade(new BigDecimal("0.50"));
        madeira = materialRepository.save(madeira);

        ItemMaterialInput item = new ItemMaterialInput(madeira.getIdMaterial(), new BigDecimal("59"));

        Produto produto = produtoService.cadastrarComMateriais(
                "Terço de madeira",
                "Terço artesanal religioso",
                TipoProduto.PRE_PRONTO,
                10, null, null,
                List.of(item)
        );

        assertNotNull(produto.getIdProduto());
        assertEquals("Terço de madeira", produto.getNome());
        assertEquals("Terço artesanal religioso", produto.getDescricao());
        assertEquals(10, produto.getQuantidadeEstoque());

        // 59 contas x R$ 0,50 = R$ 29,50
        assertEquals(0, new BigDecimal("29.50").compareTo(produto.getValor()));

        assertEquals(1, produto.getMateriaisUtilizados().size());

        ProdutoMaterial produtoMaterial = produto.getMateriaisUtilizados().get(0);
        assertEquals(madeira.getIdMaterial(), produtoMaterial.getMaterial().getIdMaterial());
        assertEquals(0, new BigDecimal("59").compareTo(produtoMaterial.getQuantidadeUtilizada()));
    }

    @Test
    void naoDeveCadastrarProdutoSemNome() {
        Material material = new Material();
        material.setNome("Crucifixo");
        material.setUnidadeMedida("un");
        material.setPrecoUnidade(new BigDecimal("3.00"));
        material = materialRepository.save(material);

        ItemMaterialInput item = new ItemMaterialInput(material.getIdMaterial(), new BigDecimal("1"));

        assertThrows(IllegalArgumentException.class, () ->
                produtoService.cadastrarComMateriais(
                        "", "Produto inválido", TipoProduto.PRE_PRONTO,
                        1, null, null, List.of(item))
        );
    }

    @Test
    void naoDeveCadastrarProdutoSemMaterial() {
        assertThrows(IllegalArgumentException.class, () ->
                produtoService.cadastrarComMateriais(
                        "Terço", "Terço religioso", TipoProduto.PRE_PRONTO,
                        10, null, null, List.of())
        );
    }

    @Test
    void naoDeveCadastrarComMaterialInexistente() {
        ItemMaterialInput item = new ItemMaterialInput(999999L, new BigDecimal("1"));

        assertThrows(java.util.NoSuchElementException.class, () ->
                produtoService.cadastrarComMateriais(
                        "Terço", "Terço religioso", TipoProduto.PRE_PRONTO,
                        10, null, null, List.of(item))
        );
    }

    @Test
    void deveListarProdutos() {
        Material material = new Material();
        material.setNome("Conta azul");
        material.setUnidadeMedida("un");
        material.setPrecoUnidade(new BigDecimal("0.50"));
        material = materialRepository.save(material);

        Produto produto = produtoService.cadastrarComMateriais(
                "Terço azul", "Terço artesanal azul", TipoProduto.PRE_PRONTO,
                5, null, null,
                List.of(new ItemMaterialInput(material.getIdMaterial(), new BigDecimal("59")))
        );

        List<Produto> produtos = produtoService.listarTodos();

        assertFalse(produtos.isEmpty());
        assertTrue(produtos.stream().anyMatch(p -> p.getIdProduto().equals(produto.getIdProduto())));
    }

    @Test
    void deveBuscarProdutoPorNome() {
        Material material = new Material();
        material.setNome("Conta vermelha");
        material.setUnidadeMedida("un");
        material.setPrecoUnidade(new BigDecimal("0.50"));
        material = materialRepository.save(material);

        produtoService.cadastrarComMateriais(
                "Terço vermelho", "Terço artesanal vermelho", TipoProduto.PRE_PRONTO,
                5, null, null,
                List.of(new ItemMaterialInput(material.getIdMaterial(), new BigDecimal("59")))
        );

        List<Produto> encontrados = produtoService.buscarPorNome("vermelho");

        assertFalse(encontrados.isEmpty());
        assertTrue(encontrados.stream().anyMatch(p -> p.getNome().equals("Terço vermelho")));
    }

    @Test
    void deveAtualizarProduto() {
        Material material = new Material();
        material.setNome("Conta branca");
        material.setUnidadeMedida("un");
        material.setPrecoUnidade(new BigDecimal("0.40"));
        material = materialRepository.save(material);

        Produto produto = produtoService.cadastrarComMateriais(
                "Terço branco", "Terço artesanal branco", TipoProduto.PRE_PRONTO,
                5, null, null,
                List.of(new ItemMaterialInput(material.getIdMaterial(), new BigDecimal("59")))
        );

        Produto atualizado = produtoService.atualizar(
                produto.getIdProduto(),
                "Terço branco grande",
                "Terço artesanal branco grande",
                10, null, null
        );

        assertEquals("Terço branco grande", atualizado.getNome());
        assertEquals("Terço artesanal branco grande", atualizado.getDescricao());
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
                "Chaveiro religioso", "Chaveiro com medalha", TipoProduto.PRE_PRONTO,
                5, null, null,
                List.of(new ItemMaterialInput(material.getIdMaterial(), new BigDecimal("1")))
        );

        Long id = produto.getIdProduto();
        produtoService.excluir(id);

        assertFalse(produtoRepository.findById(id).isPresent());
    }
}