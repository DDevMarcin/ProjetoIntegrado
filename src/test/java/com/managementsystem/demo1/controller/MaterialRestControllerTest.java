package com.managementsystem.demo1.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.managementsystem.demo1.model.Material;
import com.managementsystem.demo1.repository.MaterialRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MaterialRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MaterialRepository materialRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void limparBanco() {
        materialRepository.deleteAll();
    }

    private Material criarMaterial(String nome, String preco, String unidade) {
        Material material = new Material();

        material.setNome(nome);
        material.setCor("Natural");
        material.setPrecoUnidade(new BigDecimal(preco));
        material.setUnidadeMedida(unidade);

        return materialRepository.save(material);
    }

    private Material criarMaterialComEstoque(
            String nome,
            String preco,
            String unidade,
            String estoque
    ) {
        Material material = new Material(
                nome,
                "Natural",
                new BigDecimal(preco),
                unidade,
                new BigDecimal(estoque)
        );

        return materialRepository.save(material);
    }

    @Test
    void deveListarMateriais() throws Exception {
        criarMaterial("Madeira", "10.00", "kg");
        criarMaterial("Tinta", "15.00", "L");

        mockMvc.perform(get("/materiais"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].nome", anyOf(is("Madeira"), is("Tinta"))))
                .andExpect(jsonPath("$[0].cor", is("Natural")));
    }

    @Test
    void deveBuscarMaterialPorId() throws Exception {
        Material material = criarMaterial("Madeira", "10.00", "kg");

        mockMvc.perform(get("/materiais/" + material.getIdMaterial()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idMaterial", is(material.getIdMaterial().intValue())))
                .andExpect(jsonPath("$.nome", is("Madeira")))
                .andExpect(jsonPath("$.cor", is("Natural")))
                .andExpect(jsonPath("$.precoUnidade", is(10.00)))
                .andExpect(jsonPath("$.unidadeMedida", is("kg")));
    }

    @Test
    void deveRetornar404AoBuscarMaterialInexistente() throws Exception {
        mockMvc.perform(get("/materiais/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveCriarMaterial() throws Exception {
        String json = """
                {
                    "nome": "Madeira",
                    "cor": "Natural",
                    "precoUnidade": 10.50,
                    "unidadeMedida": "kg"
                }
                """;

        mockMvc.perform(post("/materiais")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome", is("Madeira")))
                .andExpect(jsonPath("$.cor", is("Natural")))
                .andExpect(jsonPath("$.precoUnidade", is(10.50)))
                .andExpect(jsonPath("$.unidadeMedida", is("kg")))
                .andExpect(jsonPath("$.quantidadeEstoque", is(0)));
    }

    @Test
    void deveCriarMaterialComEstoqueInicial() throws Exception {
        String json = """
                {
                    "nome": "Madeira",
                    "cor": "Marrom",
                    "precoUnidade": 10.50,
                    "unidadeMedida": "kg",
                    "quantidadeEstoque": 20
                }
                """;

        mockMvc.perform(post("/materiais")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome", is("Madeira")))
                .andExpect(jsonPath("$.cor", is("Marrom")))
                .andExpect(jsonPath("$.quantidadeEstoque", is(20)));
    }

    @Test
    void deveAtualizarMaterial() throws Exception {
        Material material = criarMaterial("Madeira", "10.00", "kg");

        String json = """
                {
                    "nome": "Madeira Premium",
                    "cor": "Marrom",
                    "precoUnidade": 25.00,
                    "unidadeMedida": "kg"
                }
                """;

        mockMvc.perform(put("/materiais/" + material.getIdMaterial())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome", is("Madeira Premium")))
                .andExpect(jsonPath("$.cor", is("Marrom")))
                .andExpect(jsonPath("$.precoUnidade", is(25.00)))
                .andExpect(jsonPath("$.unidadeMedida", is("kg")));
    }

    @Test
    void deveRetornar404AoAtualizarMaterialInexistente() throws Exception {
        String json = """
                {
                    "nome": "Madeira",
                    "cor": "Natural",
                    "precoUnidade": 10.00,
                    "unidadeMedida": "kg"
                }
                """;

        mockMvc.perform(put("/materiais/99999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveExcluirMaterial() throws Exception {
        Material material = criarMaterial("Madeira", "10.00", "kg");

        mockMvc.perform(delete("/materiais/" + material.getIdMaterial()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/materiais/" + material.getIdMaterial()))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornar400AoCadastrarMaterialSemNome() throws Exception {
        String json = """
                {
                    "nome": "",
                    "cor": "Natural",
                    "precoUnidade": 10.00,
                    "unidadeMedida": "kg"
                }
                """;

        mockMvc.perform(post("/materiais")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", is("Nome do material é obrigatório.")));
    }

    @Test
    void deveRetornar400AoCadastrarMaterialSemCor() throws Exception {
        String json = """
                {
                    "nome": "Madeira",
                    "cor": "",
                    "precoUnidade": 10.00,
                    "unidadeMedida": "kg"
                }
                """;

        mockMvc.perform(post("/materiais")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", is("Cor do material é obrigatória.")));
    }

    @Test
    void deveRetornar400AoCadastrarMaterialComCorMuitoLonga() throws Exception {
        String corMuitoLonga = "A".repeat(51);

        String json = """
                {
                    "nome": "Madeira",
                    "cor": "%s",
                    "precoUnidade": 10.00,
                    "unidadeMedida": "kg"
                }
                """.formatted(corMuitoLonga);

        mockMvc.perform(post("/materiais")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", is(
                        "Cor do material não pode ultrapassar 50 caracteres."
                )));
    }

    @Test
    void deveRetornar400AoCadastrarMaterialComPrecoNulo() throws Exception {
        String json = """
                {
                    "nome": "Madeira",
                    "cor": "Natural",
                    "precoUnidade": null,
                    "unidadeMedida": "kg"
                }
                """;

        mockMvc.perform(post("/materiais")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRetornar400AoCadastrarMaterialComPrecoNegativo() throws Exception {
        String json = """
                {
                    "nome": "Madeira",
                    "cor": "Natural",
                    "precoUnidade": -10.00,
                    "unidadeMedida": "kg"
                }
                """;

        mockMvc.perform(post("/materiais")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRetornar400AoCadastrarMaterialSemUnidadeMedida() throws Exception {
        String json = """
                {
                    "nome": "Madeira",
                    "cor": "Natural",
                    "precoUnidade": 10.00,
                    "unidadeMedida": ""
                }
                """;

        mockMvc.perform(post("/materiais")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", is(
                        "Unidade de medida é obrigatória."
                )));
    }

    @Test
    void deveRetornar400AoCadastrarMaterialDuplicado() throws Exception {
        criarMaterial("Madeira", "10.00", "kg");

        String json = """
                {
                    "nome": "Madeira",
                    "cor": "Marrom",
                    "precoUnidade": 20.00,
                    "unidadeMedida": "kg"
                }
                """;

        mockMvc.perform(post("/materiais")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRetornar400AoAtualizarParaNomeDuplicado() throws Exception {
        criarMaterial("Madeira", "10.00", "kg");
        Material outro = criarMaterial("Tinta", "15.00", "L");

        String json = """
                {
                    "nome": "Madeira",
                    "cor": "Azul",
                    "precoUnidade": 20.00,
                    "unidadeMedida": "L"
                }
                """;

        mockMvc.perform(put("/materiais/" + outro.getIdMaterial())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveConsultarEstoqueTotal() throws Exception {
        criarMaterialComEstoque("Madeira", "10.00", "kg", "10");
        criarMaterialComEstoque("Tinta", "15.00", "L", "5");

        mockMvc.perform(get("/materiais/estoque/total"))
                .andExpect(status().isOk());
    }
}