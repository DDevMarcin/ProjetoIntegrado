package com.managementsystem.demo1.service;

import com.managementsystem.demo1.BackendApplication;
import com.managementsystem.demo1.model.Material;
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
class MaterialServiceTest {

    @Autowired
    private MaterialService materialService;

    @Autowired
    private MaterialRepository materialRepository;

    @Autowired
    private ProdutoMaterialRepository produtoMaterialRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

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

    @Test
    void deveListarTodosOsMateriaisComResultados() {
        criarMaterial("Contas de madeira", "0.50", "un");
        criarMaterial("Fio encerado", "1.20", "m");

        List<Material> materiais = materialService.listarTodos();

        assertEquals(2, materiais.size());
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoHouverMateriais() {
        List<Material> materiais = materialService.listarTodos();

        assertNotNull(materiais);
        assertTrue(materiais.isEmpty());
    }

    @Test
    void deveBuscarMaterialPorIdExistente() {
        Material salvo = criarMaterial("Crucifixo prateado", "3.50", "un");

        Optional<Material> encontrado = materialService.buscarPorId(salvo.getIdMaterial());

        assertTrue(encontrado.isPresent());
        assertEquals("Crucifixo prateado", encontrado.get().getNome());
        assertEquals(0, new BigDecimal("3.50").compareTo(encontrado.get().getPrecoUnidade()));
        assertEquals("un", encontrado.get().getUnidadeMedida());
    }

    @Test
    void deveRetornarVazioAoBuscarPorIdInexistente() {
        Optional<Material> encontrado = materialService.buscarPorId(999999L);

        assertFalse(encontrado.isPresent());
    }

    @Test
    void deveRetornarVazioAoBuscarPorIdNulo() {
        Optional<Material> encontrado = materialService.buscarPorId(null);

        assertFalse(encontrado.isPresent());
    }

    @Test
    void deveBuscarMaterialPorNome() {
        criarMaterial("Contas de resina azul", "0.60", "un");
        criarMaterial("Contas de madeira", "0.50", "un");
        criarMaterial("Medalha de São Bento", "4.00", "un");

        List<Material> resultados = materialService.buscarPorNome("resina");

        assertEquals(1, resultados.size());
        assertEquals("Contas de resina azul", resultados.get(0).getNome());
    }

    @Test
    void deveBuscarMaterialPorNomeIgnorandoMaiusculasEMinusculas() {
        criarMaterial("Fio Encerado Marrom", "1.50", "m");

        List<Material> resultados = materialService.buscarPorNome("fio encerado");

        assertFalse(resultados.isEmpty());
        assertEquals("Fio Encerado Marrom", resultados.get(0).getNome());
    }

    @Test
    void deveBuscarMaterialPorNomeParcial() {
        criarMaterial("Contas de cristal 8mm", "1.00", "un");
        criarMaterial("Contas de acrílico", "0.30", "un");

        List<Material> resultados = materialService.buscarPorNome("Contas");

        assertEquals(2, resultados.size());
    }

    @Test
    void deveRetornarListaVaziaAoBuscarNomeSemCorrespondencia() {
        criarMaterial("Contas de madeira", "0.50", "un");

        List<Material> resultados = materialService.buscarPorNome("ouro");

        assertNotNull(resultados);
        assertTrue(resultados.isEmpty());
    }

    @Test
    void deveRetornarTodosAoBuscarComNomeVazioOuNulo() {
        criarMaterial("Contas de madeira", "0.50", "un");
        criarMaterial("Crucifixo", "3.00", "un");

        assertEquals(2, materialService.buscarPorNome(null).size());
        assertEquals(2, materialService.buscarPorNome("   ").size());
    }

    @Test
    void deveConsultarMaterialPorUnidadeMedida() {
        criarMaterial("Fio de nylon", "0.80", "m");
        criarMaterial("Fio encerado", "1.20", "m");
        criarMaterial("Medalha", "2.00", "un");

        List<Material> metros = materialService.consultar(null, "m");
        List<Material> unidades = materialService.consultar("", "un");

        assertEquals(2, metros.size());
        assertEquals(1, unidades.size());
    }

    @Test
    void deveConsultarMaterialPorNomeEUnidadeMedida() {
        criarMaterial("Fio de seda", "2.00", "m");
        criarMaterial("Fita de cetim", "1.00", "m");
        criarMaterial("Fio de contas", "15.00", "un");

        List<Material> resultados = materialService.consultar("Fio", "m");

        assertEquals(1, resultados.size());
        assertEquals("Fio de seda", resultados.get(0).getNome());
    }
}
