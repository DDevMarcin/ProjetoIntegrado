package com.managementsystem.demo1.service;

import com.managementsystem.demo1.config.AppConfig;
import com.managementsystem.demo1.model.Material;
import com.managementsystem.demo1.model.Produto;
import com.managementsystem.demo1.model.ProdutoMaterial;
import com.managementsystem.demo1.repository.MaterialRepository;
import com.managementsystem.demo1.repository.ProdutoMaterialRepository;
import com.managementsystem.demo1.repository.ProdutoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProdutoServiceTest {

    private AnnotationConfigApplicationContext context;

    private ProdutoService produtoService;
    private MaterialRepository materialRepository;
    private ProdutoRepository produtoRepository;
    private ProdutoMaterialRepository produtoMaterialRepository;

    @BeforeEach
    void configurar() {
        context = new AnnotationConfigApplicationContext(AppConfig.class);

        produtoService = context.getBean(ProdutoService.class);
        materialRepository = context.getBean(MaterialRepository.class);
        produtoRepository = context.getBean(ProdutoRepository.class);
        produtoMaterialRepository = context.getBean(ProdutoMaterialRepository.class);
    }

    @Test
    void deveCadastrarProdutoComMaterial() {

        // Arrange
        Material madeira = new Material();
        madeira.setNome("Contas de madeira");
        madeira.setUnidadeMedida("un");
        madeira.setPrecoUnidade(new BigDecimal("0.50"));

        madeira = materialRepository.save(madeira);

        ItemMaterialInput item = new ItemMaterialInput(
                madeira.getIdMaterial(),
                new BigDecimal("59")
        );

        // Act
        Produto produto = produtoService.cadastrarComMateriais(
                "Terço de madeira",
                "Terço artesanal religioso",
                10,
                List.of(item)
        );

        // Assert
        assertNotNull(produto.getIdProduto());

        assertEquals("Terço de madeira", produto.getNome());
        assertEquals("Terço artesanal religioso", produto.getDescricao());
        assertEquals(10, produto.getQuantidade());

        // 59 contas × R$ 0,50 = R$ 29,50
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
                new BigDecimal("59"),
                produtoMaterial.getQuantidadeUtilizada()
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

        assertThrows(
                IllegalArgumentException.class,
                () -> produtoService.cadastrarComMateriais(
                        "",
                        "Produto inválido",
                        1,
                        List.of(item)
                )
        );
    }

    @Test
    void naoDeveCadastrarProdutoComQuantidadeNegativa() {

        Material material = new Material();
        material.setNome("Crucifixo");
        material.setUnidadeMedida("un");
        material.setPrecoUnidade(new BigDecimal("3.00"));

        material = materialRepository.save(material);

        ItemMaterialInput item = new ItemMaterialInput(
                material.getIdMaterial(),
                new BigDecimal("1")
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> produtoService.cadastrarComMateriais(
                        "Terço",
                        "Terço religioso",
                        -1,
                        List.of(item)
                )
        );
    }

    @Test
    void naoDeveCadastrarProdutoSemMaterial() {

        assertThrows(
                IllegalArgumentException.class,
                () -> produtoService.cadastrarComMateriais(
                        "Terço",
                        "Terço religioso",
                        10,
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

        assertThrows(
                java.util.NoSuchElementException.class,
                () -> produtoService.cadastrarComMateriais(
                        "Terço",
                        "Terço religioso",
                        10,
                        List.of(item)
                )
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
                "Terço azul",
                "Terço artesanal azul",
                5,
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
                        .anyMatch(p -> p.getIdProduto().equals(produto.getIdProduto()))
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
                "Terço vermelho",
                "Terço artesanal vermelho",
                5,
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
                        .anyMatch(p -> p.getNome().equals("Terço vermelho"))
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
                "Terço branco",
                "Terço artesanal branco",
                5,
                List.of(
                        new ItemMaterialInput(
                                material.getIdMaterial(),
                                new BigDecimal("59")
                        )
                )
        );

        Produto atualizado = produtoService.atualizar(
                produto.getIdProduto(),
                "Terço branco grande",
                "Terço artesanal branco grande",
                10
        );

        assertEquals(
                "Terço branco grande",
                atualizado.getNome()
        );

        assertEquals(
                "Terço artesanal branco grande",
                atualizado.getDescricao()
        );

        assertEquals(
                10,
                atualizado.getQuantidade()
        );
    }

    @Test
    void deveExcluirProduto() {

        Material material = new Material();
        material.setNome("Medalha");
        material.setUnidadeMedida("un");
        material.setPrecoUnidade(new BigDecimal("2.00"));

        material = materialRepository.save(material);

        Produto produto = produtoService.cadastrarComMateriais(
                "Chaveiro religioso",
                "Chaveiro com medalha",
                5,
                List.of(
                        new ItemMaterialInput(
                                material.getIdMaterial(),
                                new BigDecimal("1")
                        )
                )
        );

        Long id = produto.getIdProduto();

        produtoService.excluir(id);

        assertFalse(
                produtoRepository.findById(id).isPresent()
        );
    }
}