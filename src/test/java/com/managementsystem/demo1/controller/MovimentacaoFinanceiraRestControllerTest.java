package com.managementsystem.demo1.controller;

import com.managementsystem.demo1.BackendApplication;
import com.managementsystem.demo1.model.MovimentacaoFinanceira;
import com.managementsystem.demo1.model.SituacaoMovimentacaoFinanceira;
import com.managementsystem.demo1.model.TipoMovimentacaoFinanceira;
import com.managementsystem.demo1.repository.MovimentacaoFinanceiraRepository;
import com.managementsystem.demo1.service.MovimentacaoFinanceiraService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = BackendApplication.class)
@AutoConfigureMockMvc
class MovimentacaoFinanceiraRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MovimentacaoFinanceiraService movimentacaoFinanceiraService;

    @Autowired
    private MovimentacaoFinanceiraRepository movimentacaoFinanceiraRepository;

    @BeforeEach
    void setUp() {
        movimentacaoFinanceiraRepository.deleteAll();
    }

    @Test
    void deveListarTodasAsMovimentacoesFinanceirasReceitasEDespesasViaGet() throws Exception {
        MovimentacaoFinanceira despesa = movimentacaoFinanceiraService.cadastrarDespesa(
                "Compra de Contas de Cristal Tcheco",
                new BigDecimal("180.50"),
                "Insumos",
                SituacaoMovimentacaoFinanceira.PAGO,
                LocalDate.of(2026, 11, 2),
                LocalDate.of(2026, 11, 5),
                "Nota fiscal #1020"
        );

        MovimentacaoFinanceira receita = movimentacaoFinanceiraService.cadastrarReceita(
                "Venda de Terço Noiva Pérolas",
                new BigDecimal("245.75"),
                "Encomendas",
                SituacaoMovimentacaoFinanceira.PENDENTE,
                LocalDate.of(2026, 11, 9),
                LocalDate.of(2026, 11, 15),
                "Sinal 50% recebido"
        );

        mockMvc.perform(get("/movimentacoes-financeiras"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].idMovimentacao", is(receita.getIdMovimentacao().intValue())))
                .andExpect(jsonPath("$[0].descricao", is("Venda de Terço Noiva Pérolas")))
                .andExpect(jsonPath("$[0].valor", is(245.75)))
                .andExpect(jsonPath("$[0].tipo", is("RECEITA")))
                .andExpect(jsonPath("$[0].categoria", is("Encomendas")))
                .andExpect(jsonPath("$[0].situacao", is("PENDENTE")))
                .andExpect(jsonPath("$[0].dataMovimentacao", is("2026-11-09")))
                .andExpect(jsonPath("$[0].dataVencimento", is("2026-11-15")))
                .andExpect(jsonPath("$[0].observacoes", is("Sinal 50% recebido")))
                .andExpect(jsonPath("$[1].idMovimentacao", is(despesa.getIdMovimentacao().intValue())))
                .andExpect(jsonPath("$[1].descricao", is("Compra de Contas de Cristal Tcheco")))
                .andExpect(jsonPath("$[1].valor", is(180.5)))
                .andExpect(jsonPath("$[1].tipo", is("DESPESA")))
                .andExpect(jsonPath("$[1].categoria", is("Insumos")))
                .andExpect(jsonPath("$[1].situacao", is("PAGO")))
                .andExpect(jsonPath("$[1].dataMovimentacao", is("2026-11-02")))
                .andExpect(jsonPath("$[1].dataVencimento", is("2026-11-05")))
                .andExpect(jsonPath("$[1].observacoes", is("Nota fiscal #1020")));
    }

    @Test
    void deveRetornarArrayVazioQuandoNaoExistiremMovimentacoesCadastradas() throws Exception {
        mockMvc.perform(get("/movimentacoes-financeiras"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void deveFiltrarMovimentacoesPorTipoSituacaoCategoriaPeriodoEDescricaoViaGet() throws Exception {
        movimentacaoFinanceiraService.cadastrarDespesa(
                "Compra de Fios Encerados",
                new BigDecimal("42.00"),
                "Insumos",
                SituacaoMovimentacaoFinanceira.PAGO,
                LocalDate.of(2026, 11, 1),
                null,
                null
        );

        movimentacaoFinanceiraService.cadastrarReceita(
                "Venda Terço São Bento",
                new BigDecimal("60.00"),
                "Vendas",
                SituacaoMovimentacaoFinanceira.PAGO,
                LocalDate.of(2026, 11, 5),
                null,
                null
        );

        movimentacaoFinanceiraService.cadastrarDespesa(
                "Compra de Caixas Aveludadas",
                new BigDecimal("95.00"),
                "Embalagens",
                SituacaoMovimentacaoFinanceira.PENDENTE,
                LocalDate.of(2026, 11, 8),
                LocalDate.of(2026, 11, 18),
                "Boleto 10 dias"
        );

        // Filtro por tipo RECEITA
        mockMvc.perform(get("/movimentacoes-financeiras").param("tipo", "RECEITA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].descricao", is("Venda Terço São Bento")))
                .andExpect(jsonPath("$[0].tipo", is("RECEITA")));

        // Filtro por tipo DESPESA
        mockMvc.perform(get("/movimentacoes-financeiras").param("tipo", "DESPESA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        // Filtro por situação PENDENTE
        mockMvc.perform(get("/movimentacoes-financeiras").param("situacao", "PENDENTE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].descricao", is("Compra de Caixas Aveludadas")));

        // Filtro por categoria (case-insensitive)
        mockMvc.perform(get("/movimentacoes-financeiras").param("categoria", "insumos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].categoria", is("Insumos")));

        // Filtro por período
        mockMvc.perform(get("/movimentacoes-financeiras")
                        .param("dataInicio", "2026-11-04")
                        .param("dataFim", "2026-11-09"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        // Filtro combinado
        mockMvc.perform(get("/movimentacoes-financeiras")
                        .param("tipo", "DESPESA")
                        .param("situacao", "PAGO")
                        .param("categoria", "Insumos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].descricao", is("Compra de Fios Encerados")));
    }

    @Test
    void deveBuscarMovimentacoesPorDescricaoViaRotaBuscar() throws Exception {
        movimentacaoFinanceiraService.cadastrarReceita(
                "Venda Terço Casamento",
                new BigDecimal("180.00"),
                LocalDate.of(2026, 11, 9)
        );
        movimentacaoFinanceiraService.cadastrarDespesa(
                "Frete Correios",
                new BigDecimal("28.00"),
                "Logística",
                LocalDate.of(2026, 11, 9)
        );

        mockMvc.perform(get("/movimentacoes-financeiras/buscar").param("descricao", "casamento"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].descricao", is("Venda Terço Casamento")));

        mockMvc.perform(get("/movimentacoes-financeiras/buscar").param("descricao", "inexistente"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        mockMvc.perform(get("/movimentacoes-financeiras/buscar").param("descricao", "   "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void deveBuscarMovimentacaoFinanceiraPorIdComSucessoERetornar404QuandoInexistente() throws Exception {
        MovimentacaoFinanceira receita = movimentacaoFinanceiraService.cadastrarReceita(
                "Venda Dezena de Carro",
                new BigDecimal("25.00"),
                LocalDate.of(2026, 11, 9)
        );
        MovimentacaoFinanceira despesa = movimentacaoFinanceiraService.cadastrarDespesa(
                "Compra de Metais",
                new BigDecimal("70.00"),
                "Insumos",
                LocalDate.of(2026, 11, 9)
        );

        mockMvc.perform(get("/movimentacoes-financeiras/" + receita.getIdMovimentacao()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idMovimentacao", is(receita.getIdMovimentacao().intValue())))
                .andExpect(jsonPath("$.tipo", is("RECEITA")));

        mockMvc.perform(get("/movimentacoes-financeiras/" + despesa.getIdMovimentacao()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idMovimentacao", is(despesa.getIdMovimentacao().intValue())))
                .andExpect(jsonPath("$.tipo", is("DESPESA")))
                .andExpect(jsonPath("$.categoria", is("Insumos")));

        mockMvc.perform(get("/movimentacoes-financeiras/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornar400ParaParametrosOuIdInvalidosEPeriodoInconsistente() throws Exception {
        mockMvc.perform(get("/movimentacoes-financeiras/id-invalido"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", is("Identificador de movimentação financeira inválido.")));

        mockMvc.perform(get("/movimentacoes-financeiras").param("tipo", "TIPO_INEXISTENTE"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", containsString("Tipo de movimentação financeira inválido")));

        mockMvc.perform(get("/movimentacoes-financeiras").param("situacao", "SITUACAO_INEXISTENTE"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", containsString("Situação de movimentação financeira inválida")));

        mockMvc.perform(get("/movimentacoes-financeiras").param("dataInicio", "data-invalida"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", containsString("Formato de data inválido")));

        mockMvc.perform(get("/movimentacoes-financeiras")
                        .param("dataInicio", "2026-11-20")
                        .param("dataFim", "2026-11-05"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", is("Data final não pode ser anterior à data inicial.")));
    }

    @Test
    void deveAtenderRotasAlternativasCompativeisDoModuloFinanceiro() throws Exception {
        movimentacaoFinanceiraService.cadastrarReceita(
                "Venda Terço",
                new BigDecimal("50.00"),
                LocalDate.of(2026, 11, 9)
        );

        mockMvc.perform(get("/financeiro/movimentacoes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].descricao", is("Venda Terço")));

        mockMvc.perform(get("/financeiro/movimentacoes-financeiras"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].descricao", is("Venda Terço")));
    }
}
