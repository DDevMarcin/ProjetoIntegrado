package com.managementsystem.demo1.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.managementsystem.demo1.model.SituacaoMovimentacaoFinanceira;
import com.managementsystem.demo1.service.MovimentacaoFinanceiraService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TotalReceitasPorPeriodoTest {

    @Autowired MockMvc mockMvc;
    @Autowired MovimentacaoFinanceiraService service;

    @BeforeEach
    void dados() {
        receita("R1", "100.00", SituacaoMovimentacaoFinanceira.PAGO, LocalDate.of(2000, 11, 5));
        receita("R2", "50.50", SituacaoMovimentacaoFinanceira.PAGO, LocalDate.of(2000, 11, 30));
        receita("R3", "200.00", SituacaoMovimentacaoFinanceira.PENDENTE, LocalDate.of(2000, 11, 10));
        receita("R4", "30.00", SituacaoMovimentacaoFinanceira.CANCELADO, LocalDate.of(2000, 11, 15));
        receita("R5", "999.00", SituacaoMovimentacaoFinanceira.PAGO, LocalDate.of(2000, 10, 31));
        service.cadastrarDespesa("D1", new BigDecimal("70.00"), "Outros",
                LocalDate.of(2000, 11, 10));
    }

    private void receita(String desc, String valor, SituacaoMovimentacaoFinanceira sit, LocalDate data) {
        service.cadastrarReceita(desc, new BigDecimal(valor), "Vendas", sit, data, null, null);
    }

    @Test
    void somaPeriodoSemFiltroDeSituacao() throws Exception {
        mockMvc.perform(get("/movimentacoes-financeiras/total-receitas")
                        .param("dataInicio", "2000-11-01").param("dataFim", "2000-11-30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(150.50));   // antes: 380.50
    }

    @Test
    void somaApenasPagasIncluindoUltimoDia() throws Exception {
        mockMvc.perform(get("/movimentacoes-financeiras/total-receitas")
                        .param("dataInicio", "2000-11-01").param("dataFim", "2000-11-30")
                        .param("situacao", "PAGO"))
                .andExpect(jsonPath("$.total").value(150.50));
    }

    @Test
    void periodoSemReceitasRetornaZero() throws Exception {
        mockMvc.perform(get("/movimentacoes-financeiras/total-receitas")
                        .param("dataInicio", "1990-01-01").param("dataFim", "1990-12-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(0.00));
    }

    @Test
    void periodoInvertidoRetorna400() throws Exception {
        mockMvc.perform(get("/movimentacoes-financeiras/total-receitas")
                        .param("dataInicio", "2000-11-30").param("dataFim", "2000-11-01"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void semSituacaoSomaApenasPagas() throws Exception {
        mockMvc.perform(get("/movimentacoes-financeiras/total-receitas")
                        .param("dataInicio", "2000-11-01").param("dataFim", "2000-11-30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(150.50));   // antes: 380.50
    }

    @Test
    void situacaoPendenteExplicitaSomaSoPendentes() throws Exception {
        mockMvc.perform(get("/movimentacoes-financeiras/total-receitas")
                        .param("dataInicio", "2000-11-01").param("dataFim", "2000-11-30")
                        .param("situacao", "PENDENTE"))
                .andExpect(jsonPath("$.total").value(200.00));
    }
}