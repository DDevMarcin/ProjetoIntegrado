package com.managementsystem.demo1.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.managementsystem.demo1.BackendApplication;
import com.managementsystem.demo1.model.CategoriaDespesa;
import com.managementsystem.demo1.repository.CategoriaDespesaRepository;
import com.managementsystem.demo1.repository.MovimentacaoFinanceiraRepository;
import com.managementsystem.demo1.service.CategoriaDespesaService;
import com.managementsystem.demo1.service.MovimentacaoFinanceiraService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = BackendApplication.class)
@AutoConfigureMockMvc
class CategoriaDespesaRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoriaDespesaService categoriaDespesaService;

    @Autowired
    private MovimentacaoFinanceiraService movimentacaoFinanceiraService;

    @Autowired
    private CategoriaDespesaRepository categoriaDespesaRepository;

    @Autowired
    private MovimentacaoFinanceiraRepository movimentacaoFinanceiraRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        movimentacaoFinanceiraRepository.deleteAll();
        categoriaDespesaRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        movimentacaoFinanceiraRepository.deleteAll();
        categoriaDespesaService.garantirCategoriasPadrao();
    }

    @Test
    void deveCadastrarCategoriaViaPostComSucessoEBloquearDuplicidade() throws Exception {
        Map<String, Object> payload = Map.of(
                "nome", "Insumos Especiais",
                "descricao", "Cristais importados e metais banhados"
        );

        mockMvc.perform(post("/categorias-despesas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idCategoria", notNullValue()))
                .andExpect(jsonPath("$.nome", is("Insumos Especiais")))
                .andExpect(jsonPath("$.descricao", is("Cristais importados e metais banhados")));

        mockMvc.perform(post("/financeiro/categorias-despesas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("nome", "insumos especiais"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", is("Já existe uma categoria de despesa cadastrada com o nome informado: insumos especiais")));
    }

    @Test
    void deveRetornar400QuandoNomeAusenteOuCorpoInvalido() throws Exception {
        mockMvc.perform(post("/categorias-despesas")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", is("Dados da categoria de despesa são obrigatórios.")));

        mockMvc.perform(post("/categorias-despesas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("nome", "   "))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", is("Nome da categoria de despesa é obrigatório.")));

        mockMvc.perform(post("/categorias-despesas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{json_invalido"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", is("Formato de requisição inválido ou campos incompatíveis.")));
    }

    @Test
    void deveListarBuscarPorIdAtualizarEExcluirCategoriaViaRest() throws Exception {
        CategoriaDespesa c1 = categoriaDespesaService.salvar("Embalagens", "Caixas e sacolas");
        CategoriaDespesa c2 = categoriaDespesaService.salvar("Frete", "Correios");

        mockMvc.perform(get("/categorias-despesas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].nome", is("Embalagens")))
                .andExpect(jsonPath("$[1].nome", is("Frete")));

        mockMvc.perform(get("/categorias-despesas").param("nome", "fre"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nome", is("Frete")));

        mockMvc.perform(get("/categorias-despesas/" + c1.getIdCategoria()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome", is("Embalagens")));

        mockMvc.perform(put("/categorias-despesas/" + c1.getIdCategoria())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "nome", "Embalagens Personalizadas",
                                "descricao", "Caixas aveludadas e saquinhos de linho"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome", is("Embalagens Personalizadas")))
                .andExpect(jsonPath("$.descricao", is("Caixas aveludadas e saquinhos de linho")));

        // Excluir categoria sem vínculos
        mockMvc.perform(delete("/categorias-despesas/" + c2.getIdCategoria()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/categorias-despesas/" + c2.getIdCategoria()))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornar400AoTentarExcluirCategoriaVinculadaADespesa() throws Exception {
        CategoriaDespesa categoria = categoriaDespesaService.salvar("Insumos", "Contas e fios");
        movimentacaoFinanceiraService.cadastrarDespesaPorCategoria(
                "Compra de Fio de Seda",
                new BigDecimal("38.00"),
                categoria.getIdCategoria(),
                LocalDate.of(2026, 11, 10)
        );

        mockMvc.perform(delete("/categorias-despesas/" + categoria.getIdCategoria()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", is("Não é possível excluir a categoria pois ela está vinculada a uma ou mais despesas.")));

        mockMvc.perform(delete("/categorias-despesas/999999"))
                .andExpect(status().isNotFound());
    }
}
