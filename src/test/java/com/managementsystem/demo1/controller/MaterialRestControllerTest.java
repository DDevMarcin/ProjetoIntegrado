package com.managementsystem.demo1.controller;

import com.managementsystem.demo1.BackendApplication;
import com.managementsystem.demo1.model.Material;
import com.managementsystem.demo1.repository.MaterialRepository;
import com.managementsystem.demo1.repository.ProdutoMaterialRepository;
import com.managementsystem.demo1.repository.ProdutoRepository;
import com.managementsystem.demo1.model.TipoProduto;
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
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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

    @Autowired
    private ProdutoService produtoService;

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

    @Test
    void deveCadastrarMaterialValidoViaPostRetornando201() throws Exception {
        String json = """
                {
                    "nome": "Contas de madeira",
                    "precoUnidade": 0.50,
                    "unidadeMedida": "un"
                }
                """;

        mockMvc.perform(post("/materiais")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.idMaterial", notNullValue()))
                .andExpect(jsonPath("$.nome", is("Contas de madeira")))
                .andExpect(jsonPath("$.precoUnidade", is(0.50)))
                .andExpect(jsonPath("$.unidadeMedida", is("un")));

        assertEquals(1, materialRepository.count());
    }

    @Test
    void deveRetornar400AoCadastrarMaterialComBodyNulo() throws Exception {
        mockMvc.perform(post("/materiais")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", is("Dados do material são obrigatórios.")));
    }

    @Test
    void deveRetornar400AoCadastrarMaterialComNomeEmBranco() throws Exception {
        String json = """
                {
                    "nome": "   ",
                    "precoUnidade": 1.00,
                    "unidadeMedida": "un"
                }
                """;

        mockMvc.perform(post("/materiais")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", is("Nome do material é obrigatório.")));
    }

    @Test
    void deveRetornar400AoCadastrarMaterialComPrecoNegativo() throws Exception {
        String json = """
                {
                    "nome": "Fio de nylon",
                    "precoUnidade": -0.50,
                    "unidadeMedida": "m"
                }
                """;

        mockMvc.perform(post("/materiais")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", is("Preço do material inválido.")));
    }

    @Test
    void deveRetornar400AoCadastrarMaterialComPrecoNulo() throws Exception {
        String json = """
                {
                    "nome": "Fio de nylon",
                    "unidadeMedida": "m"
                }
                """;

        mockMvc.perform(post("/materiais")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", is("Preço do material inválido.")));
    }

    @Test
    void deveRetornar400AoCadastrarMaterialComUnidadeMedidaInvalida() throws Exception {
        String json = """
                {
                    "nome": "Fio de nylon",
                    "precoUnidade": 1.50,
                    "unidadeMedida": "centimetros"
                }
                """;

        mockMvc.perform(post("/materiais")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", is("Unidade de medida não pode ultrapassar 10 caracteres.")));
    }

    @Test
    void deveAtualizarMaterialExistenteViaPut() throws Exception {
        Material salvo = criarMaterial("Contas simples", "0.40", "un");

        String jsonAtualizacao = """
                {
                    "nome": "Contas especiais",
                    "precoUnidade": 0.80,
                    "unidadeMedida": "un"
                }
                """;

        mockMvc.perform(put("/materiais/" + salvo.getIdMaterial())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonAtualizacao))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idMaterial", is(salvo.getIdMaterial().intValue())))
                .andExpect(jsonPath("$.nome", is("Contas especiais")))
                .andExpect(jsonPath("$.precoUnidade", is(0.80)))
                .andExpect(jsonPath("$.unidadeMedida", is("un")));
    }

    @Test
    void deveRetornar400AoAtualizarMaterialComBodyNulo() throws Exception {
        Material salvo = criarMaterial("Contas", "0.40", "un");

        mockMvc.perform(put("/materiais/" + salvo.getIdMaterial())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", is("Dados do material são obrigatórios.")));
    }

    @Test
    void deveRetornar404AoAtualizarMaterialInexistenteViaPut() throws Exception {
        String jsonAtualizacao = """
                {
                    "nome": "Contas",
                    "precoUnidade": 0.80,
                    "unidadeMedida": "un"
                }
                """;

        mockMvc.perform(put("/materiais/999999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonAtualizacao))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveExcluirMaterialSemVinculoViaDelete() throws Exception {
        Material salvo = criarMaterial("Material avulso", "2.00", "un");

        mockMvc.perform(delete("/materiais/" + salvo.getIdMaterial()))
                .andExpect(status().isNoContent());

        assertFalse(materialRepository.findById(salvo.getIdMaterial()).isPresent());
    }

    @Test
    void deveRetornar404AoExcluirMaterialInexistenteViaDelete() throws Exception {
        mockMvc.perform(delete("/materiais/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornar400AoExcluirMaterialVinculadoAProdutoViaDelete() throws Exception {
        Material material = criarMaterial("Contas de madeira", "0.50", "un");

        produtoService.cadastrarComMateriais(
                "COD-DEL-01",
                "Rosario",
                "Rosario simples",
                TipoProduto.PRE_PRONTO,
                1,
                null,
                null,
                false,
                null,
                List.of(new ItemMaterialInput(material.getIdMaterial(), new BigDecimal("10")))
        );

        mockMvc.perform(delete("/materiais/" + material.getIdMaterial()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", is("Não é possível excluir o material pois ele está vinculado a um ou mais produtos.")));

        assertTrue(materialRepository.findById(material.getIdMaterial()).isPresent());
    }
}
