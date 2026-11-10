package com.managementsystem.demo1.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.managementsystem.demo1.BackendApplication;
import com.managementsystem.demo1.model.MovimentacaoFinanceira;
import com.managementsystem.demo1.model.SituacaoMovimentacaoFinanceira;
import com.managementsystem.demo1.model.TipoMovimentacaoFinanceira;
import com.managementsystem.demo1.repository.MovimentacaoFinanceiraRepository;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = BackendApplication.class)
@AutoConfigureMockMvc
class DespesaRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MovimentacaoFinanceiraRepository movimentacaoFinanceiraRepository;

    @Autowired
    private com.managementsystem.demo1.service.CategoriaDespesaService categoriaDespesaService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        movimentacaoFinanceiraRepository.deleteAll();
        categoriaDespesaService.garantirCategoriasPadrao();
    }

    @Test
    void deveCadastrarDespesaViaPostComSucesso() throws Exception {
        Map<String, Object> payload = Map.of(
                "descricao", "Compra de Contas de Madeira e Metais",
                "valor", 215.90,
                "categoria", "Insumos",
                "situacao", "PAGO",
                "dataMovimentacao", "2026-11-09",
                "dataVencimento", "2026-11-15",
                "observacoes", "Nota fiscal #882"
        );

        mockMvc.perform(post("/despesas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idMovimentacao", notNullValue()))
                .andExpect(jsonPath("$.descricao", is("Compra de Contas de Madeira e Metais")))
                .andExpect(jsonPath("$.valor", is(215.9)))
                .andExpect(jsonPath("$.tipo", is("DESPESA")))
                .andExpect(jsonPath("$.categoria", is("Insumos")))
                .andExpect(jsonPath("$.situacao", is("PAGO")))
                .andExpect(jsonPath("$.dataMovimentacao", is("2026-11-09")))
                .andExpect(jsonPath("$.dataVencimento", is("2026-11-15")))
                .andExpect(jsonPath("$.observacoes", is("Nota fiscal #882")));

        assertEquals(1, movimentacaoFinanceiraRepository.count());
    }

    @Test
    void deveCadastrarDespesaUsandoAliasDataPagamentoENaRotaFinanceiroDespesas() throws Exception {
        Map<String, Object> payload = Map.of(
                "descricao", "Compra de Caixas Aveludadas",
                "valor", 94.00,
                "categoria", "Embalagens",
                "dataPagamento", "2026-11-09"
        );

        mockMvc.perform(post("/financeiro/despesas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idMovimentacao", notNullValue()))
                .andExpect(jsonPath("$.descricao", is("Compra de Caixas Aveludadas")))
                .andExpect(jsonPath("$.tipo", is("DESPESA")))
                .andExpect(jsonPath("$.categoria", is("Embalagens")))
                .andExpect(jsonPath("$.situacao", is("PAGO")))
                .andExpect(jsonPath("$.dataMovimentacao", is("2026-11-09")));
    }

    @Test
    void deveRetornar400QuandoCorpoDaRequisicaoForVazioOuJsonInvalido() throws Exception {
        mockMvc.perform(post("/despesas")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", is("Dados da despesa são obrigatórios.")));

        mockMvc.perform(post("/despesas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{json_invalido"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", is("Formato de requisição inválido ou campos incompatíveis.")));
    }

    @Test
    void deveRetornar400QuandoCamposObrigatoriosOuCategoriaNaoForemInformados() throws Exception {
        Map<String, Object> semDescricao = Map.of(
                "valor", 50.00,
                "categoria", "Insumos",
                "dataMovimentacao", "2026-11-09"
        );

        mockMvc.perform(post("/despesas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(semDescricao)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", is("Descrição da despesa é obrigatória.")));

        Map<String, Object> semCategoria = Map.of(
                "descricao", "Compra de Fios",
                "valor", 50.00,
                "dataMovimentacao", "2026-11-09"
        );

        mockMvc.perform(post("/despesas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(semCategoria)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", is("Categoria da despesa é obrigatória.")));

        Map<String, Object> semData = Map.of(
                "descricao", "Compra de Fios",
                "valor", 50.00,
                "categoria", "Insumos"
        );

        mockMvc.perform(post("/despesas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(semData)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", is("Data da movimentação é obrigatória.")));
    }

    @Test
    void deveRetornar400QuandoValorForZeroOuNegativo() throws Exception {
        Map<String, Object> valorZero = Map.of(
                "descricao", "Compra de Fios",
                "valor", 0,
                "categoria", "Insumos",
                "dataMovimentacao", "2026-11-09"
        );

        mockMvc.perform(post("/despesas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(valorZero)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", is("Valor da despesa deve ser maior que zero.")));

        Map<String, Object> valorNegativo = Map.of(
                "descricao", "Compra de Fios",
                "valor", -15.00,
                "categoria", "Insumos",
                "dataMovimentacao", "2026-11-09"
        );

        mockMvc.perform(post("/despesas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(valorNegativo)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", is("Valor da despesa deve ser maior que zero.")));
    }

    @Test
    void deveRetornar400QuandoDatasForemInconsistentes() throws Exception {
        Map<String, Object> vencimentoAnterior = Map.of(
                "descricao", "Boleto Fornecedor",
                "valor", 120.00,
                "categoria", "Insumos",
                "dataMovimentacao", "2026-11-10",
                "dataVencimento", "2026-11-05"
        );

        mockMvc.perform(post("/despesas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(vencimentoAnterior)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", is("Data de vencimento não pode ser anterior à data da movimentação.")));

        Map<String, Object> datasDivergentes = Map.of(
                "descricao", "Boleto Fornecedor",
                "valor", 120.00,
                "categoria", "Insumos",
                "dataMovimentacao", "2026-11-09",
                "dataPagamento", "2026-11-10"
        );

        mockMvc.perform(post("/despesas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(datasDivergentes)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", is("Data da movimentação e data de pagamento informadas divergem.")));
    }

    @Test
    void deveRetornar400QuandoTentarCadastrarReceitaNoEndpointDeDespesas() throws Exception {
        Map<String, Object> receitaNoEndpointDeDespesa = Map.of(
                "descricao", "Venda de Terço",
                "valor", 80.00,
                "categoria", "Vendas",
                "tipo", "RECEITA",
                "dataMovimentacao", "2026-11-09"
        );

        mockMvc.perform(post("/despesas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(receitaNoEndpointDeDespesa)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", containsString("Não é permitido cadastrar uma movimentação do tipo RECEITA como despesa")));
    }

    @Test
    void deveListarEBuscarDespesaPorIdViaGet() throws Exception {
        MovimentacaoFinanceira salvo = movimentacaoFinanceiraRepository.save(
                new MovimentacaoFinanceira(
                        "Frete Correios SEDEX",
                        new BigDecimal("32.50"),
                        TipoMovimentacaoFinanceira.DESPESA,
                        "Frete",
                        SituacaoMovimentacaoFinanceira.PAGO,
                        LocalDate.of(2026, 11, 9),
                        null,
                        null
                )
        );

        mockMvc.perform(get("/despesas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].descricao", is("Frete Correios SEDEX")))
                .andExpect(jsonPath("$[0].categoria", is("Frete")));

        mockMvc.perform(get("/despesas/" + salvo.getIdMovimentacao()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idMovimentacao", is(salvo.getIdMovimentacao().intValue())))
                .andExpect(jsonPath("$.tipo", is("DESPESA")));

        mockMvc.perform(get("/despesas/999999"))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/despesas/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", is("Identificador de despesa inválido.")));
    }

    @Test
    void deveAssociarCategoriaPorIdEFiltrarDespesasPorCategoriaViaRest() throws Exception {
        var embalagens = categoriaDespesaService.buscarPorNomeExato("Embalagens").orElseThrow();

        Map<String, Object> payloadComIdCategoria = Map.of(
                "descricao", "Compra de Saquinhos de Linho",
                "valor", 75.00,
                "idCategoria", embalagens.getIdCategoria(),
                "dataMovimentacao", "2026-11-10"
        );

        mockMvc.perform(post("/despesas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payloadComIdCategoria)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.categoria", is("Embalagens")))
                .andExpect(jsonPath("$.categoriaDespesa.idCategoria", is(embalagens.getIdCategoria().intValue())));

        mockMvc.perform(get("/despesas").param("idCategoria", embalagens.getIdCategoria().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].descricao", is("Compra de Saquinhos de Linho")));

        // Tentativa de associar com categoria inexistente por nome ou por ID
        Map<String, Object> payloadCategoriaInexistente = Map.of(
                "descricao", "Gasto avulso",
                "valor", 30.00,
                "categoria", "Categoria Que Não Existe",
                "dataMovimentacao", "2026-11-10"
        );

        mockMvc.perform(post("/despesas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payloadCategoriaInexistente)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", is("Categoria de despesa não encontrada: Categoria Que Não Existe")));

        Map<String, Object> payloadIdInexistente = Map.of(
                "descricao", "Gasto avulso",
                "valor", 30.00,
                "idCategoria", 999999L,
                "dataMovimentacao", "2026-11-10"
        );

        mockMvc.perform(post("/despesas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payloadIdInexistente)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", is("Categoria de despesa não encontrada para o ID informado: 999999")));
    }
}

