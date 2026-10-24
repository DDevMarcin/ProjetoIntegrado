package com.managementsystem.demo1.controller;

import com.managementsystem.demo1.BackendApplication;
import com.managementsystem.demo1.model.Material;
import com.managementsystem.demo1.repository.MaterialRepository;
import com.managementsystem.demo1.repository.ProdutoMaterialRepository;
import com.managementsystem.demo1.repository.ProdutoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = BackendApplication.class)
@AutoConfigureMockMvc
class MaterialRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

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
    void deveListarTodosOsMateriaisViaGet() throws Exception {
        criarMaterial("Contas de madeira", "0.50", "un");
        criarMaterial("Fio encerado", "1.20", "m");

        mockMvc.perform(get("/materiais"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].nome", is("Contas de madeira")))
                .andExpect(jsonPath("$[1].nome", is("Fio encerado")));
    }

    @Test
    void deveFiltrarMateriaisPorNomeViaParametroGet() throws Exception {
        criarMaterial("Contas de madeira", "0.50", "un");
        criarMaterial("Fio encerado", "1.20", "m");

        mockMvc.perform(get("/materiais").param("nome", "madeira"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nome", is("Contas de madeira")));
    }

    @Test
    void deveFiltrarMateriaisPorUnidadeMedidaViaParametroGet() throws Exception {
        criarMaterial("Contas de madeira", "0.50", "un");
        criarMaterial("Fio encerado", "1.20", "m");

        mockMvc.perform(get("/materiais").param("unidadeMedida", "m"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nome", is("Fio encerado")));
    }

    @Test
    void deveBuscarMaterialPorNomeViaRotaBuscar() throws Exception {
        criarMaterial("Contas de resina", "0.75", "un");
        criarMaterial("Crucifixo dourado", "5.00", "un");

        mockMvc.perform(get("/materiais/buscar").param("nome", "resina"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nome", is("Contas de resina")));
    }

    @Test
    void deveRetornarArrayVazioQuandoBuscaPorNomeNaoTiverCorrespondencia() throws Exception {
        criarMaterial("Contas de madeira", "0.50", "un");

        mockMvc.perform(get("/materiais/buscar").param("nome", "inexistente"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void deveBuscarMaterialPorIdComSucesso() throws Exception {
        Material material = criarMaterial("Medalha Milagrosa", "4.50", "un");

        mockMvc.perform(get("/materiais/" + material.getIdMaterial()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idMaterial", is(material.getIdMaterial().intValue())))
                .andExpect(jsonPath("$.nome", is("Medalha Milagrosa")))
                .andExpect(jsonPath("$.precoUnidade", is(4.50)))
                .andExpect(jsonPath("$.unidadeMedida", is("un")));
    }

    @Test
    void deveRetornar404AoBuscarMaterialPorIdInexistente() throws Exception {
        mockMvc.perform(get("/materiais/999999"))
                .andExpect(status().isNotFound());
    }
}
