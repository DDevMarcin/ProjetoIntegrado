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
class ReceitaRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MovimentacaoFinanceiraRepository movimentacaoFinanceiraRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        movimentacaoFinanceiraRepository.deleteAll();
    }

    @Test
    void deveCadastrarReceitaViaPostComSucesso() throws Exception {
        Map<String, Object> payload = Map.of(
                "descricao", "Venda de Terço Noiva",
                "valor", 150.00,
                "categoria", "Vendas",
                "situacao", "PAGO",
                "dataMovimentacao", "2026-11-09",
                "dataVencimento", "2026-11-15",
                "observacoes", "Pago via PIX"
        );

        mockMvc.perform(post("/receitas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idMovimentacao", notNullValue()))
                .andExpect(jsonPath("$.descricao", is("Venda de Terço Noiva")))
                .andExpect(jsonPath("$.valor", is(150.0)))
                .andExpect(jsonPath("$.tipo", is("RECEITA")))
                .andExpect(jsonPath("$.categoria", is("Vendas")))
                .andExpect(jsonPath("$.situacao", is("PAGO")))
                .andExpect(jsonPath("$.dataMovimentacao", is("2026-11-09")))
                .andExpect(jsonPath("$.dataVencimento", is("2026-11-15")))
                .andExpect(jsonPath("$.observacoes", is("Pago via PIX")));

        assertEquals(1, movimentacaoFinanceiraRepository.count());
    }

    @Test
    void deveCadastrarReceitaUsandoAliasDataRecebimentoENaRotaFinanceiroReceitas() throws Exception {
        Map<String, Object> payload = Map.of(
                "descricao", "Encomenda Terço Primeira Eucaristia",
                "valor", 95.00,
                "dataRecebimento", "2026-11-09"
        );

        mockMvc.perform(post("/financeiro/receitas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idMovimentacao", notNullValue()))
                .andExpect(jsonPath("$.descricao", is("Encomenda Terço Primeira Eucaristia")))
                .andExpect(jsonPath("$.tipo", is("RECEITA")))
                .andExpect(jsonPath("$.situacao", is("PAGO")))
                .andExpect(jsonPath("$.dataMovimentacao", is("2026-11-09")));
    }

    @Test
    void deveRetornar400QuandoCorpoDaRequisicaoForVazioOuJsonInvalido() throws Exception {
        mockMvc.perform(post("/receitas")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", is("Dados da receita são obrigatórios.")));

        mockMvc.perform(post("/receitas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{json_invalido"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", is("Formato de requisição inválido ou campos incompatíveis.")));
    }

    @Test
    void deveRetornar400QuandoCamposObrigatoriosNaoForemInformados() throws Exception {
        Map<String, Object> semDescricao = Map.of(
                "valor", 50.00,
                "dataMovimentacao", "2026-11-09"
        );

        mockMvc.perform(post("/receitas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(semDescricao)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", is("Descrição da receita é obrigatória.")));

        Map<String, Object> semData = Map.of(
                "descricao", "Venda Terço",
                "valor", 50.00
        );

        mockMvc.perform(post("/receitas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(semData)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", is("Data da movimentação é obrigatória.")));
    }

    @Test
    void deveRetornar400QuandoValorForZeroOuNegativo() throws Exception {
        Map<String, Object> valorZero = Map.of(
                "descricao", "Venda Terço",
                "valor", 0,
                "dataMovimentacao", "2026-11-09"
        );

        mockMvc.perform(post("/receitas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(valorZero)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", is("Valor da receita deve ser maior que zero.")));

        Map<String, Object> valorNegativo = Map.of(
                "descricao", "Venda Terço",
                "valor", -25.00,
                "dataMovimentacao", "2026-11-09"
        );

        mockMvc.perform(post("/receitas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(valorNegativo)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", is("Valor da receita deve ser maior que zero.")));
    }

    @Test
    void deveRetornar400QuandoDatasForemInconsistentes() throws Exception {
        Map<String, Object> vencimentoAnterior = Map.of(
                "descricao", "Encomenda",
                "valor", 100.00,
                "dataMovimentacao", "2026-11-10",
                "dataVencimento", "2026-11-05"
        );

        mockMvc.perform(post("/receitas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(vencimentoAnterior)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", is("Data de vencimento não pode ser anterior à data da movimentação.")));

        Map<String, Object> datasDivergentes = Map.of(
                "descricao", "Encomenda",
                "valor", 100.00,
                "dataMovimentacao", "2026-11-09",
                "dataRecebimento", "2026-11-10"
        );

        mockMvc.perform(post("/receitas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(datasDivergentes)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", is("Data da movimentação e data de recebimento informadas divergem.")));
    }

    @Test
    void deveRetornar400QuandoTentarCadastrarDespesaNoEndpointDeReceitas() throws Exception {
        Map<String, Object> despesaNoEndpointDeReceita = Map.of(
                "descricao", "Compra de material",
                "valor", 80.00,
                "tipo", "DESPESA",
                "dataMovimentacao", "2026-11-09"
        );

        mockMvc.perform(post("/receitas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(despesaNoEndpointDeObjeto(despesaNoEndpointDeReceita))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", containsString("Não é permitido cadastrar uma movimentação do tipo DESPESA como receita")));
    }

    private Map<String, Object> despesaNoEndpointDeObjeto(Map<String, Object> map) {
        return map;
    }

    @Test
    void deveListarEBuscarReceitaPorIdViaGet() throws Exception {
        MovimentacaoFinanceira salvo = movimentacaoFinanceiraRepository.save(
                new MovimentacaoFinanceira(
                        "Venda Dezena de Carro",
                        new BigDecimal("25.00"),
                        TipoMovimentacaoFinanceira.RECEITA,
                        "Vendas",
                        SituacaoMovimentacaoFinanceira.PAGO,
                        LocalDate.of(2026, 11, 9),
                        null,
                        null
                )
        );

        mockMvc.perform(get("/receitas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].descricao", is("Venda Dezena de Carro")));

        mockMvc.perform(get("/receitas/" + salvo.getIdMovimentacao()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idMovimentacao", is(salvo.getIdMovimentacao().intValue())))
                .andExpect(jsonPath("$.tipo", is("RECEITA")));

        mockMvc.perform(get("/receitas/999999"))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/receitas/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", is("Identificador de receita inválido.")));
    }
}

