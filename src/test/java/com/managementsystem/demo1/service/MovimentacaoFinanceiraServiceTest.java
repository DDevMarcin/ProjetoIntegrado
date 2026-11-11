package com.managementsystem.demo1.service;

import com.managementsystem.demo1.BackendApplication;
import com.managementsystem.demo1.model.CategoriaDespesa;
import com.managementsystem.demo1.model.MovimentacaoFinanceira;
import com.managementsystem.demo1.model.SituacaoMovimentacaoFinanceira;
import com.managementsystem.demo1.model.TipoMovimentacaoFinanceira;
import com.managementsystem.demo1.repository.MovimentacaoFinanceiraRepository;
import com.managementsystem.demo1.repository.ResumoDespesaCategoriaProjection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = BackendApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
class MovimentacaoFinanceiraServiceTest {

    @Autowired
    private MovimentacaoFinanceiraService movimentacaoFinanceiraService;

    @Autowired
    private CategoriaDespesaService categoriaDespesaService;

    @Autowired
    private MovimentacaoFinanceiraRepository movimentacaoFinanceiraRepository;

    @BeforeEach
    void setUp() {
        movimentacaoFinanceiraRepository.deleteAll();
        categoriaDespesaService.garantirCategoriasPadrao();
    }

    @Test
    void deveCadastrarReceitaCompletaComSucesso() {
        LocalDate dataMov = LocalDate.of(2026, 11, 9);
        LocalDate dataVenc = LocalDate.of(2026, 11, 15);

        MovimentacaoFinanceira salva = movimentacaoFinanceiraService.cadastrarReceita(
                "Encomenda Terço de Noiva",
                new BigDecimal("185.50"),
                "Encomendas",
                SituacaoMovimentacaoFinanceira.PENDENTE,
                dataMov,
                dataVenc,
                "Sinal de 50% acordado"
        );

        assertNotNull(salva.getIdMovimentacao());
        assertEquals("Encomenda Terço de Noiva", salva.getDescricao());
        assertEquals(0, new BigDecimal("185.50").compareTo(salva.getValor()));
        assertEquals(TipoMovimentacaoFinanceira.RECEITA, salva.getTipo());
        assertEquals("Encomendas", salva.getCategoria());
        assertEquals(SituacaoMovimentacaoFinanceira.PENDENTE, salva.getSituacao());
        assertEquals(dataMov, salva.getDataMovimentacao());
        assertEquals(dataVenc, salva.getDataVencimento());
        assertEquals("Sinal de 50% acordado", salva.getObservacoes());

        Optional<MovimentacaoFinanceira> doBanco =
                movimentacaoFinanceiraRepository.findById(salva.getIdMovimentacao());
        assertTrue(doBanco.isPresent());
        assertEquals(TipoMovimentacaoFinanceira.RECEITA, doBanco.get().getTipo());
    }

    @Test
    void deveCadastrarReceitaApenasComCamposObrigatoriosAssumindoSituacaoPago() {
        LocalDate dataMov = LocalDate.of(2026, 11, 9);

        MovimentacaoFinanceira salva = movimentacaoFinanceiraService.cadastrarReceita(
                "Venda Terço São Bento",
                new BigDecimal("45.00"),
                dataMov
        );

        assertNotNull(salva.getIdMovimentacao());
        assertEquals(TipoMovimentacaoFinanceira.RECEITA, salva.getTipo());
        assertEquals(SituacaoMovimentacaoFinanceira.PAGO, salva.getSituacao());
        assertEquals(dataMov, salva.getDataMovimentacao());
        assertNull(salva.getCategoria());
        assertNull(salva.getDataVencimento());
        assertNull(salva.getObservacoes());
    }

    @Test
    void deveFazerTrimNosCamposTextuaisELimparOpcionaisEmBranco() {
        MovimentacaoFinanceira receita = new MovimentacaoFinanceira();
        receita.setDescricao("   Venda Pulseira Terço   ");
        receita.setValor(new BigDecimal("29.90"));
        receita.setCategoria("   ");
        receita.setObservacoes("  Pagamento em espécie  ");
        receita.setDataMovimentacao(LocalDate.of(2026, 11, 9));

        MovimentacaoFinanceira salva = movimentacaoFinanceiraService.cadastrarReceita(receita);

        assertEquals("Venda Pulseira Terço", salva.getDescricao());
        assertNull(salva.getCategoria());
        assertEquals("Pagamento em espécie", salva.getObservacoes());
        assertEquals(TipoMovimentacaoFinanceira.RECEITA, salva.getTipo());
    }

    @Test
    void deveRejeitarCadastroDeReceitaNula() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                movimentacaoFinanceiraService.cadastrarReceita(null)
        );
        assertEquals("Receita não pode ser nula.", ex.getMessage());
    }

    @Test
    void deveRejeitarCadastroDeReceitaComTipoDespesa() {
        MovimentacaoFinanceira despesaDisfarcada = new MovimentacaoFinanceira(
                "Compra de fios",
                new BigDecimal("50.00"),
                TipoMovimentacaoFinanceira.DESPESA,
                LocalDate.of(2026, 11, 9)
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                movimentacaoFinanceiraService.cadastrarReceita(despesaDisfarcada)
        );
        assertTrue(ex.getMessage().contains("Não é permitido cadastrar uma movimentação do tipo DESPESA como receita"));
        assertEquals(0, movimentacaoFinanceiraRepository.count());
    }

    @Test
    void deveRejeitarReceitaSemDescricaoOuComDescricaoAcimaDe255Caracteres() {
        LocalDate hoje = LocalDate.of(2026, 11, 9);

        IllegalArgumentException exNula = assertThrows(IllegalArgumentException.class, () ->
                movimentacaoFinanceiraService.cadastrarReceita(null, new BigDecimal("50.00"), hoje)
        );
        assertEquals("Descrição da receita é obrigatória.", exNula.getMessage());

        IllegalArgumentException exBranco = assertThrows(IllegalArgumentException.class, () ->
                movimentacaoFinanceiraService.cadastrarReceita("   ", new BigDecimal("50.00"), hoje)
        );
        assertEquals("Descrição da receita é obrigatória.", exBranco.getMessage());

        IllegalArgumentException exLonga = assertThrows(IllegalArgumentException.class, () ->
                movimentacaoFinanceiraService.cadastrarReceita("R".repeat(256), new BigDecimal("50.00"), hoje)
        );
        assertEquals("Descrição da receita não pode ultrapassar 255 caracteres.", exLonga.getMessage());
    }

    @Test
    void deveRejeitarReceitaComValorNuloZeroOuNegativo() {
        LocalDate hoje = LocalDate.of(2026, 11, 9);

        IllegalArgumentException exNulo = assertThrows(IllegalArgumentException.class, () ->
                movimentacaoFinanceiraService.cadastrarReceita("Venda", null, hoje)
        );
        assertEquals("Valor da receita deve ser maior que zero.", exNulo.getMessage());

        IllegalArgumentException exZero = assertThrows(IllegalArgumentException.class, () ->
                movimentacaoFinanceiraService.cadastrarReceita("Venda", BigDecimal.ZERO, hoje)
        );
        assertEquals("Valor da receita deve ser maior que zero.", exZero.getMessage());

        IllegalArgumentException exNegativo = assertThrows(IllegalArgumentException.class, () ->
                movimentacaoFinanceiraService.cadastrarReceita("Venda", new BigDecimal("-10.00"), hoje)
        );
        assertEquals("Valor da receita deve ser maior que zero.", exNegativo.getMessage());
    }

    @Test
    void deveRejeitarReceitaSemDataMovimentacao() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                movimentacaoFinanceiraService.cadastrarReceita("Venda Terço", new BigDecimal("40.00"), null)
        );
        assertEquals("Data da movimentação é obrigatória.", ex.getMessage());
    }

    @Test
    void deveRejeitarReceitaQuandoDataVencimentoForAnteriorADataMovimentacao() {
        LocalDate dataMov = LocalDate.of(2026, 11, 10);
        LocalDate dataVencAnterior = LocalDate.of(2026, 11, 9);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                movimentacaoFinanceiraService.cadastrarReceita(
                        "Encomenda Terço",
                        new BigDecimal("100.00"),
                        "Encomendas",
                        SituacaoMovimentacaoFinanceira.PENDENTE,
                        dataMov,
                        dataVencAnterior,
                        null
                )
        );
        assertEquals("Data de vencimento não pode ser anterior à data da movimentação.", ex.getMessage());
    }

    @Test
    void devePermitirDataVencimentoIgualOuPosteriorADataMovimentacao() {
        LocalDate dataMov = LocalDate.of(2026, 11, 10);

        MovimentacaoFinanceira mesmaData = movimentacaoFinanceiraService.cadastrarReceita(
                "Venda à vista",
                new BigDecimal("50.00"),
                "Vendas",
                SituacaoMovimentacaoFinanceira.PAGO,
                dataMov,
                dataMov,
                null
        );
        assertEquals(dataMov, mesmaData.getDataVencimento());
    }

    @Test
    void deveRejeitarCategoriaEObservacoesAcimaDoLimiteDeCaracteres() {
        LocalDate dataMov = LocalDate.of(2026, 11, 9);

        IllegalArgumentException exCat = assertThrows(IllegalArgumentException.class, () ->
                movimentacaoFinanceiraService.cadastrarReceita(
                        "Venda",
                        new BigDecimal("30.00"),
                        "C".repeat(101),
                        SituacaoMovimentacaoFinanceira.PAGO,
                        dataMov,
                        null,
                        null
                )
        );
        assertEquals("Categoria não pode ultrapassar 100 caracteres.", exCat.getMessage());

        IllegalArgumentException exObs = assertThrows(IllegalArgumentException.class, () ->
                movimentacaoFinanceiraService.cadastrarReceita(
                        "Venda",
                        new BigDecimal("30.00"),
                        "Vendas",
                        SituacaoMovimentacaoFinanceira.PAGO,
                        dataMov,
                        null,
                        "O".repeat(501)
                )
        );
        assertEquals("Observações não podem ultrapassar 500 caracteres.", exObs.getMessage());
    }

    @Test
    void deveListarEBuscarApenasMovimentacoesDoTipoReceita() {
        MovimentacaoFinanceira receita = movimentacaoFinanceiraService.cadastrarReceita(
                "Venda Terço Cristal",
                new BigDecimal("90.00"),
                LocalDate.of(2026, 11, 9)
        );

        MovimentacaoFinanceira despesa = new MovimentacaoFinanceira(
                "Compra de Fio",
                new BigDecimal("20.00"),
                TipoMovimentacaoFinanceira.DESPESA,
                LocalDate.of(2026, 11, 9)
        );
        despesa = movimentacaoFinanceiraRepository.save(despesa);

        List<MovimentacaoFinanceira> receitas = movimentacaoFinanceiraService.listarReceitas();
        assertEquals(1, receitas.size());
        assertEquals(receita.getIdMovimentacao(), receitas.get(0).getIdMovimentacao());

        assertTrue(movimentacaoFinanceiraService.buscarReceitaPorId(receita.getIdMovimentacao()).isPresent());
        assertFalse(movimentacaoFinanceiraService.buscarReceitaPorId(despesa.getIdMovimentacao()).isPresent());
        assertFalse(movimentacaoFinanceiraService.buscarReceitaPorId(null).isPresent());

        Long idDespesa = despesa.getIdMovimentacao();
        assertThrows(NoSuchElementException.class, () ->
                movimentacaoFinanceiraService.obterReceitaPorId(idDespesa)
        );
    }

    // ── Testes de Cadastro e Consulta de Despesas ──

    @Test
    void deveCadastrarDespesaCompletaComSucesso() {
        LocalDate dataMov = LocalDate.of(2026, 11, 9);
        LocalDate dataVenc = LocalDate.of(2026, 11, 20);

        MovimentacaoFinanceira salva = movimentacaoFinanceiraService.cadastrarDespesa(
                "Compra de Contas de Cristal Tcheco",
                new BigDecimal("195.80"),
                "Insumos",
                SituacaoMovimentacaoFinanceira.PENDENTE,
                dataMov,
                dataVenc,
                "Boleto parcelado fornecedor SP"
        );

        assertNotNull(salva.getIdMovimentacao());
        assertEquals("Compra de Contas de Cristal Tcheco", salva.getDescricao());
        assertEquals(0, new BigDecimal("195.80").compareTo(salva.getValor()));
        assertEquals(TipoMovimentacaoFinanceira.DESPESA, salva.getTipo());
        assertEquals("Insumos", salva.getCategoria());
        assertEquals(SituacaoMovimentacaoFinanceira.PENDENTE, salva.getSituacao());
        assertEquals(dataMov, salva.getDataMovimentacao());
        assertEquals(dataVenc, salva.getDataVencimento());
        assertEquals("Boleto parcelado fornecedor SP", salva.getObservacoes());

        Optional<MovimentacaoFinanceira> doBanco =
                movimentacaoFinanceiraRepository.findById(salva.getIdMovimentacao());
        assertTrue(doBanco.isPresent());
        assertEquals(TipoMovimentacaoFinanceira.DESPESA, doBanco.get().getTipo());
    }

    @Test
    void deveCadastrarDespesaComCamposObrigatoriosAssumindoSituacaoPago() {
        LocalDate dataMov = LocalDate.of(2026, 11, 9);

        MovimentacaoFinanceira salva = movimentacaoFinanceiraService.cadastrarDespesa(
                "Compra de Fio Encerado",
                new BigDecimal("42.00"),
                "   Insumos   ",
                dataMov
        );

        assertNotNull(salva.getIdMovimentacao());
        assertEquals(TipoMovimentacaoFinanceira.DESPESA, salva.getTipo());
        assertEquals(SituacaoMovimentacaoFinanceira.PAGO, salva.getSituacao());
        assertEquals("Insumos", salva.getCategoria());
        assertEquals(dataMov, salva.getDataMovimentacao());
        assertNull(salva.getDataVencimento());
        assertNull(salva.getObservacoes());
    }

    @Test
    void deveRejeitarCadastroDeDespesaNulaOuComTipoReceita() {
        IllegalArgumentException exNula = assertThrows(IllegalArgumentException.class, () ->
                movimentacaoFinanceiraService.cadastrarDespesa(null)
        );
        assertEquals("Despesa não pode ser nula.", exNula.getMessage());

        MovimentacaoFinanceira receitaDisfarcada = new MovimentacaoFinanceira(
                "Venda de Terço",
                new BigDecimal("60.00"),
                TipoMovimentacaoFinanceira.RECEITA,
                "Vendas",
                SituacaoMovimentacaoFinanceira.PAGO,
                LocalDate.of(2026, 11, 9),
                null,
                null
        );

        IllegalArgumentException exTipo = assertThrows(IllegalArgumentException.class, () ->
                movimentacaoFinanceiraService.cadastrarDespesa(receitaDisfarcada)
        );
        assertTrue(exTipo.getMessage().contains("Não é permitido cadastrar uma movimentação do tipo RECEITA como despesa"));
        assertEquals(0, movimentacaoFinanceiraRepository.count());
    }

    @Test
    void deveRejeitarDespesaSemDescricaoOuComDescricaoAcimaDe255Caracteres() {
        LocalDate hoje = LocalDate.of(2026, 11, 9);

        IllegalArgumentException exNula = assertThrows(IllegalArgumentException.class, () ->
                movimentacaoFinanceiraService.cadastrarDespesa(null, new BigDecimal("50.00"), "Insumos", hoje)
        );
        assertEquals("Descrição da despesa é obrigatória.", exNula.getMessage());

        IllegalArgumentException exBranco = assertThrows(IllegalArgumentException.class, () ->
                movimentacaoFinanceiraService.cadastrarDespesa("   ", new BigDecimal("50.00"), "Insumos", hoje)
        );
        assertEquals("Descrição da despesa é obrigatória.", exBranco.getMessage());

        IllegalArgumentException exLonga = assertThrows(IllegalArgumentException.class, () ->
                movimentacaoFinanceiraService.cadastrarDespesa("D".repeat(256), new BigDecimal("50.00"), "Insumos", hoje)
        );
        assertEquals("Descrição da despesa não pode ultrapassar 255 caracteres.", exLonga.getMessage());
    }

    @Test
    void deveRejeitarDespesaSemCategoriaOuComCategoriaAcimaDe100Caracteres() {
        LocalDate hoje = LocalDate.of(2026, 11, 9);

        IllegalArgumentException exNula = assertThrows(IllegalArgumentException.class, () ->
                movimentacaoFinanceiraService.cadastrarDespesa("Compra de caixas", new BigDecimal("35.00"), null, hoje)
        );
        assertEquals("Categoria da despesa é obrigatória.", exNula.getMessage());

        IllegalArgumentException exBranco = assertThrows(IllegalArgumentException.class, () ->
                movimentacaoFinanceiraService.cadastrarDespesa("Compra de caixas", new BigDecimal("35.00"), "   ", hoje)
        );
        assertEquals("Categoria da despesa é obrigatória.", exBranco.getMessage());

        IllegalArgumentException exLonga = assertThrows(IllegalArgumentException.class, () ->
                movimentacaoFinanceiraService.cadastrarDespesa("Compra de caixas", new BigDecimal("35.00"), "C".repeat(101), hoje)
        );
        assertEquals("Categoria não pode ultrapassar 100 caracteres.", exLonga.getMessage());
    }

    @Test
    void deveRejeitarDespesaComValorNuloZeroOuNegativo() {
        LocalDate hoje = LocalDate.of(2026, 11, 9);

        IllegalArgumentException exNulo = assertThrows(IllegalArgumentException.class, () ->
                movimentacaoFinanceiraService.cadastrarDespesa("Frete", null, "Logística", hoje)
        );
        assertEquals("Valor da despesa deve ser maior que zero.", exNulo.getMessage());

        IllegalArgumentException exZero = assertThrows(IllegalArgumentException.class, () ->
                movimentacaoFinanceiraService.cadastrarDespesa("Frete", BigDecimal.ZERO, "Logística", hoje)
        );
        assertEquals("Valor da despesa deve ser maior que zero.", exZero.getMessage());

        IllegalArgumentException exNeg = assertThrows(IllegalArgumentException.class, () ->
                movimentacaoFinanceiraService.cadastrarDespesa("Frete", new BigDecimal("-5.00"), "Logística", hoje)
        );
        assertEquals("Valor da despesa deve ser maior que zero.", exNeg.getMessage());
    }

    @Test
    void deveRejeitarDespesaSemDataMovimentacaoOuComDataVencimentoAnterior() {
        IllegalArgumentException exSemData = assertThrows(IllegalArgumentException.class, () ->
                movimentacaoFinanceiraService.cadastrarDespesa("Frete", new BigDecimal("25.00"), "Logística", null)
        );
        assertEquals("Data da movimentação é obrigatória.", exSemData.getMessage());

        IllegalArgumentException exVencAnterior = assertThrows(IllegalArgumentException.class, () ->
                movimentacaoFinanceiraService.cadastrarDespesa(
                        "Conta de Energia",
                        new BigDecimal("110.00"),
                        "Custos Fixos",
                        SituacaoMovimentacaoFinanceira.PENDENTE,
                        LocalDate.of(2026, 11, 10),
                        LocalDate.of(2026, 11, 5),
                        null
                )
        );
        assertEquals("Data de vencimento não pode ser anterior à data da movimentação.", exVencAnterior.getMessage());
    }

    @Test
    void deveListarEBuscarApenasMovimentacoesDoTipoDespesa() {
        MovimentacaoFinanceira despesa = movimentacaoFinanceiraService.cadastrarDespesa(
                "Compra de Embalagens",
                new BigDecimal("64.00"),
                "Embalagens",
                LocalDate.of(2026, 11, 9)
        );

        MovimentacaoFinanceira receita = movimentacaoFinanceiraService.cadastrarReceita(
                "Venda Terço",
                new BigDecimal("45.00"),
                LocalDate.of(2026, 11, 9)
        );

        List<MovimentacaoFinanceira> despesas = movimentacaoFinanceiraService.listarDespesas();
        assertEquals(1, despesas.size());
        assertEquals(despesa.getIdMovimentacao(), despesas.get(0).getIdMovimentacao());

        assertTrue(movimentacaoFinanceiraService.buscarDespesaPorId(despesa.getIdMovimentacao()).isPresent());
        assertFalse(movimentacaoFinanceiraService.buscarDespesaPorId(receita.getIdMovimentacao()).isPresent());
        assertFalse(movimentacaoFinanceiraService.buscarDespesaPorId(null).isPresent());

        Long idReceita = receita.getIdMovimentacao();
        assertThrows(NoSuchElementException.class, () ->
                movimentacaoFinanceiraService.obterDespesaPorId(idReceita)
        );
    }

    // ── Testes de Consulta Unificada de Movimentações Financeiras ──

    @Test
    void deveListarTodasAsMovimentacoesContemplandoReceitasEDespesasOrdenadasPorDataDesc() {
        MovimentacaoFinanceira m1 = movimentacaoFinanceiraService.cadastrarDespesa(
                "Compra de Contas de Madeira",
                new BigDecimal("85.00"),
                "Insumos",
                LocalDate.of(2026, 11, 2)
        );

        MovimentacaoFinanceira m2 = movimentacaoFinanceiraService.cadastrarReceita(
                "Venda de Terço São Bento",
                new BigDecimal("120.00"),
                "Vendas",
                SituacaoMovimentacaoFinanceira.PAGO,
                LocalDate.of(2026, 11, 8),
                null,
                null
        );

        MovimentacaoFinanceira m3 = movimentacaoFinanceiraService.cadastrarDespesa(
                "Compra de Embalagens",
                new BigDecimal("45.00"),
                "Embalagens",
                SituacaoMovimentacaoFinanceira.PENDENTE,
                LocalDate.of(2026, 11, 5),
                LocalDate.of(2026, 11, 15),
                "Boleto fornecedor"
        );

        List<MovimentacaoFinanceira> todas = movimentacaoFinanceiraService.listarMovimentacoes();
        assertEquals(3, todas.size());
        assertEquals(m2.getIdMovimentacao(), todas.get(0).getIdMovimentacao());
        assertEquals(m3.getIdMovimentacao(), todas.get(1).getIdMovimentacao());
        assertEquals(m1.getIdMovimentacao(), todas.get(2).getIdMovimentacao());

        assertEquals(3, movimentacaoFinanceiraService.listarTodas().size());
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoExistiremMovimentacoesCadastradas() {
        List<MovimentacaoFinanceira> resultado = movimentacaoFinanceiraService.listarMovimentacoes();
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    @Test
    void deveBuscarEObterMovimentacaoPorIdTantoReceitaQuantoDespesa() {
        MovimentacaoFinanceira receita = movimentacaoFinanceiraService.cadastrarReceita(
                "Venda Terço Cristal",
                new BigDecimal("110.00"),
                "Vendas",
                SituacaoMovimentacaoFinanceira.PAGO,
                LocalDate.of(2026, 11, 9),
                null,
                null
        );

        MovimentacaoFinanceira despesa = movimentacaoFinanceiraService.cadastrarDespesa(
                "Compra Fio de Seda",
                new BigDecimal("35.00"),
                "Insumos",
                LocalDate.of(2026, 11, 9)
        );

        Optional<MovimentacaoFinanceira> recOpt = movimentacaoFinanceiraService.buscarPorId(receita.getIdMovimentacao());
        assertTrue(recOpt.isPresent());
        assertEquals(TipoMovimentacaoFinanceira.RECEITA, recOpt.get().getTipo());

        Optional<MovimentacaoFinanceira> despOpt = movimentacaoFinanceiraService.buscarMovimentacaoPorId(despesa.getIdMovimentacao());
        assertTrue(despOpt.isPresent());
        assertEquals(TipoMovimentacaoFinanceira.DESPESA, despOpt.get().getTipo());

        assertFalse(movimentacaoFinanceiraService.buscarPorId(null).isPresent());
        assertFalse(movimentacaoFinanceiraService.buscarPorId(999999L).isPresent());

        MovimentacaoFinanceira obtida = movimentacaoFinanceiraService.obterPorId(receita.getIdMovimentacao());
        assertEquals(receita.getIdMovimentacao(), obtida.getIdMovimentacao());
        assertEquals(despesa.getIdMovimentacao(),
                movimentacaoFinanceiraService.obterMovimentacaoPorId(despesa.getIdMovimentacao()).getIdMovimentacao());

        assertThrows(IllegalArgumentException.class, () -> movimentacaoFinanceiraService.obterPorId(null));
        assertThrows(NoSuchElementException.class, () -> movimentacaoFinanceiraService.obterPorId(999999L));
    }

    @Test
    void deveConsultarMovimentacoesComFiltrosIndividuaisECombinados() {
        movimentacaoFinanceiraService.cadastrarDespesa(
                "Compra de Contas e Cristais",
                new BigDecimal("150.00"),
                "Insumos",
                SituacaoMovimentacaoFinanceira.PAGO,
                LocalDate.of(2026, 11, 1),
                LocalDate.of(2026, 11, 1),
                "Nota 101"
        );

        movimentacaoFinanceiraService.cadastrarReceita(
                "Venda Terço Casamento",
                new BigDecimal("220.00"),
                "Encomendas",
                SituacaoMovimentacaoFinanceira.PENDENTE,
                LocalDate.of(2026, 11, 5),
                LocalDate.of(2026, 11, 20),
                "Sinal pendente"
        );

        movimentacaoFinanceiraService.cadastrarReceita(
                "Venda Terço São Bento",
                new BigDecimal("65.00"),
                "Vendas",
                SituacaoMovimentacaoFinanceira.PAGO,
                LocalDate.of(2026, 11, 9),
                null,
                null
        );

        movimentacaoFinanceiraService.cadastrarDespesa(
                "Compra de Caixas Personalizadas",
                new BigDecimal("80.00"),
                "Embalagens",
                SituacaoMovimentacaoFinanceira.PENDENTE,
                LocalDate.of(2026, 11, 10),
                LocalDate.of(2026, 11, 18),
                null
        );

        // Filtro por tipo
        assertEquals(2, movimentacaoFinanceiraService.listarPorTipo(TipoMovimentacaoFinanceira.RECEITA).size());
        assertEquals(2, movimentacaoFinanceiraService.listarPorTipo(TipoMovimentacaoFinanceira.DESPESA).size());
        assertEquals(4, movimentacaoFinanceiraService.listarPorTipo(null).size());

        // Filtro por situação
        assertEquals(2, movimentacaoFinanceiraService.listarPorSituacao(SituacaoMovimentacaoFinanceira.PAGO).size());
        assertEquals(2, movimentacaoFinanceiraService.listarPorSituacao(SituacaoMovimentacaoFinanceira.PENDENTE).size());
        assertEquals(0, movimentacaoFinanceiraService.listarPorSituacao(SituacaoMovimentacaoFinanceira.CANCELADO).size());
        assertEquals(4, movimentacaoFinanceiraService.listarPorSituacao(null).size());

        // Filtro por categoria (case-insensitive)
        assertEquals(1, movimentacaoFinanceiraService.listarPorCategoria("insumos").size());
        assertEquals(1, movimentacaoFinanceiraService.listarPorCategoria("  ENCOMENDAS ").size());
        assertEquals(4, movimentacaoFinanceiraService.listarPorCategoria("   ").size());

        // Filtro por período
        List<MovimentacaoFinanceira> noPeriodo = movimentacaoFinanceiraService.listarPorPeriodo(
                LocalDate.of(2026, 11, 4),
                LocalDate.of(2026, 11, 9)
        );
        assertEquals(2, noPeriodo.size());

        // Busca por descrição
        assertEquals(2, movimentacaoFinanceiraService.buscarPorDescricao("terço").size());
        assertEquals(4, movimentacaoFinanceiraService.buscarPorDescricao("   ").size());

        // Consulta combinada: tipo + situacao
        List<MovimentacaoFinanceira> receitasPagas = movimentacaoFinanceiraService.consultar(
                TipoMovimentacaoFinanceira.RECEITA,
                SituacaoMovimentacaoFinanceira.PAGO,
                null
        );
        assertEquals(1, receitasPagas.size());
        assertEquals("Venda Terço São Bento", receitasPagas.get(0).getDescricao());

        // Consulta combinada: tipo + situacao + categoria + período + descricao
        List<MovimentacaoFinanceira> combinada = movimentacaoFinanceiraService.consultar(
                TipoMovimentacaoFinanceira.DESPESA,
                SituacaoMovimentacaoFinanceira.PENDENTE,
                "Embalagens",
                LocalDate.of(2026, 11, 8),
                LocalDate.of(2026, 11, 12),
                "Caixas"
        );
        assertEquals(1, combinada.size());
        assertEquals("Compra de Caixas Personalizadas", combinada.get(0).getDescricao());
    }

    @Test
    void deveFiltrarPorPeriodoRespeitandoLimitesInclusivosEDatasUnilaterais() {
        MovimentacaoFinanceira anterior = movimentacaoFinanceiraService.cadastrarDespesa(
                "Compra Anterior ao Período",
                new BigDecimal("40.00"),
                "Insumos",
                LocalDate.of(2026, 11, 1)
        );

        MovimentacaoFinanceira limiteInicial = movimentacaoFinanceiraService.cadastrarReceita(
                "Receita no Limite Inicial",
                new BigDecimal("100.00"),
                LocalDate.of(2026, 11, 5)
        );

        MovimentacaoFinanceira intermediaria = movimentacaoFinanceiraService.cadastrarDespesa(
                "Despesa Intermediária",
                new BigDecimal("60.00"),
                "Embalagens",
                LocalDate.of(2026, 11, 8)
        );

        MovimentacaoFinanceira limiteFinal = movimentacaoFinanceiraService.cadastrarReceita(
                "Receita no Limite Final",
                new BigDecimal("150.00"),
                LocalDate.of(2026, 11, 10)
        );

        MovimentacaoFinanceira posterior = movimentacaoFinanceiraService.cadastrarReceita(
                "Receita Posterior ao Período",
                new BigDecimal("90.00"),
                LocalDate.of(2026, 11, 15)
        );

        // 1. Intervalo fechado [2026-11-05, 2026-11-10]: deve incluir limites e excluir anterior e posterior
        List<MovimentacaoFinanceira> noIntervalo = movimentacaoFinanceiraService.listarPorPeriodo(
                LocalDate.of(2026, 11, 5),
                LocalDate.of(2026, 11, 10)
        );
        assertEquals(3, noIntervalo.size());
        assertEquals(limiteFinal.getIdMovimentacao(), noIntervalo.get(0).getIdMovimentacao());
        assertEquals(intermediaria.getIdMovimentacao(), noIntervalo.get(1).getIdMovimentacao());
        assertEquals(limiteInicial.getIdMovimentacao(), noIntervalo.get(2).getIdMovimentacao());
        assertFalse(noIntervalo.contains(anterior));
        assertFalse(noIntervalo.contains(posterior));

        // 2. Somente dataInicial informada (>= 2026-11-08)
        List<MovimentacaoFinanceira> aPartirDe = movimentacaoFinanceiraService.listarPorPeriodo(
                LocalDate.of(2026, 11, 8),
                null
        );
        assertEquals(3, aPartirDe.size());
        assertEquals(posterior.getIdMovimentacao(), aPartirDe.get(0).getIdMovimentacao());
        assertEquals(limiteFinal.getIdMovimentacao(), aPartirDe.get(1).getIdMovimentacao());
        assertEquals(intermediaria.getIdMovimentacao(), aPartirDe.get(2).getIdMovimentacao());

        // 3. Somente dataFinal informada (<= 2026-11-05)
        List<MovimentacaoFinanceira> ateData = movimentacaoFinanceiraService.listarPorPeriodo(
                null,
                LocalDate.of(2026, 11, 5)
        );
        assertEquals(2, ateData.size());
        assertEquals(limiteInicial.getIdMovimentacao(), ateData.get(0).getIdMovimentacao());
        assertEquals(anterior.getIdMovimentacao(), ateData.get(1).getIdMovimentacao());

        // 4. Nenhuma data informada: mantém listagem completa
        List<MovimentacaoFinanceira> todas = movimentacaoFinanceiraService.listarPorPeriodo(null, null);
        assertEquals(5, todas.size());

        // 5. Receitas e despesas por período específico
        List<MovimentacaoFinanceira> receitasPeriodo = movimentacaoFinanceiraService.listarReceitasPorPeriodo(
                LocalDate.of(2026, 11, 5),
                LocalDate.of(2026, 11, 10)
        );
        assertEquals(2, receitasPeriodo.size());

        List<MovimentacaoFinanceira> despesasPeriodo = movimentacaoFinanceiraService.listarDespesasPorPeriodo(
                LocalDate.of(2026, 11, 5),
                LocalDate.of(2026, 11, 10)
        );
        assertEquals(1, despesasPeriodo.size());
        assertEquals(intermediaria.getIdMovimentacao(), despesasPeriodo.get(0).getIdMovimentacao());
    }

    @Test
    void deveRejeitarConsultaPorPeriodoComDatasInvalidasOuInconsistentes() {
        IllegalArgumentException exOrdem = assertThrows(IllegalArgumentException.class, () ->
                movimentacaoFinanceiraService.listarPorPeriodo(
                        LocalDate.of(2026, 11, 15),
                        LocalDate.of(2026, 11, 10)
                )
        );
        assertEquals("Data final não pode ser anterior à data inicial.", exOrdem.getMessage());

        IllegalArgumentException exReceitas = assertThrows(IllegalArgumentException.class, () ->
                movimentacaoFinanceiraService.listarReceitasPorPeriodo(
                        LocalDate.of(2026, 11, 15),
                        LocalDate.of(2026, 11, 10)
                )
        );
        assertEquals("Data final não pode ser anterior à data inicial.", exReceitas.getMessage());

        IllegalArgumentException exDespesas = assertThrows(IllegalArgumentException.class, () ->
                movimentacaoFinanceiraService.listarDespesasPorPeriodo(
                        LocalDate.of(2026, 11, 15),
                        LocalDate.of(2026, 11, 10)
                )
        );
        assertEquals("Data final não pode ser anterior à data inicial.", exDespesas.getMessage());

        IllegalArgumentException exConsultar = assertThrows(IllegalArgumentException.class, () ->
                movimentacaoFinanceiraService.consultar(
                        null,
                        null,
                        null,
                        LocalDate.of(2026, 11, 20),
                        LocalDate.of(2026, 11, 5)
                )
        );
        assertEquals("Data final não pode ser anterior à data inicial.", exConsultar.getMessage());

        IllegalArgumentException exTotalReceitas = assertThrows(IllegalArgumentException.class, () ->
                movimentacaoFinanceiraService.calcularTotalReceitasPorPeriodo(
                        LocalDate.of(2026, 11, 20),
                        LocalDate.of(2026, 11, 5)
                )
        );
        assertEquals("Data final não pode ser anterior à data inicial.", exTotalReceitas.getMessage());

        IllegalArgumentException exTotalDespesas = assertThrows(IllegalArgumentException.class, () ->
                movimentacaoFinanceiraService.calcularTotalDespesasPorPeriodo(
                        LocalDate.of(2026, 11, 20),
                        LocalDate.of(2026, 11, 5)
                )
        );
        assertEquals("Data final não pode ser anterior à data inicial.", exTotalDespesas.getMessage());
    }

    @Test
    void deveCalcularTotalDeReceitasPorPeriodoComPrecisaoDecimalExcluindoDespesasERespeitandoLimites() {
        // 1. Quando não existirem receitas cadastradas, retorna 0.00
        BigDecimal totalInicialVazio = movimentacaoFinanceiraService.calcularTotalReceitasPorPeriodo(
                LocalDate.of(2026, 11, 1),
                LocalDate.of(2026, 11, 30)
        );
        assertEquals(new BigDecimal("0.00"), totalInicialVazio);
        assertEquals(0, BigDecimal.ZERO.compareTo(totalInicialVazio));

        // 2. Cadastra receitas antes, nos limites, dentro e depois do período, além de despesas no mesmo período
        movimentacaoFinanceiraService.cadastrarReceita(
                "Receita Fora Antes",
                new BigDecimal("35.90"),
                LocalDate.of(2026, 11, 2)
        );

        movimentacaoFinanceiraService.cadastrarReceita(
                "Receita no Limite Inicial",
                new BigDecimal("125.33"),
                LocalDate.of(2026, 11, 5)
        );

        movimentacaoFinanceiraService.cadastrarReceita(
                "Receita Intermediária",
                new BigDecimal("89.67"),
                "Encomendas",
                SituacaoMovimentacaoFinanceira.PENDENTE,
                LocalDate.of(2026, 11, 7),
                LocalDate.of(2026, 11, 20),
                "Sinal pendente"
        );

        movimentacaoFinanceiraService.cadastrarDespesa(
                "Despesa no Período 1",
                new BigDecimal("300.00"),
                "Insumos",
                LocalDate.of(2026, 11, 6)
        );

        movimentacaoFinanceiraService.cadastrarDespesa(
                "Despesa no Período 2",
                new BigDecimal("140.50"),
                "Embalagens",
                LocalDate.of(2026, 11, 10)
        );

        movimentacaoFinanceiraService.cadastrarReceita(
                "Receita no Limite Final",
                new BigDecimal("45.10"),
                LocalDate.of(2026, 11, 10)
        );

        movimentacaoFinanceiraService.cadastrarReceita(
                "Receita Fora Depois",
                new BigDecimal("210.00"),
                LocalDate.of(2026, 11, 18)
        );

        // 3. Período fechado [2026-11-05, 2026-11-10]: 125.33 + 89.67 + 45.10 = 260.10 (exclui despesas de 300.00 e 140.50)
        BigDecimal totalNoPeriodo = movimentacaoFinanceiraService.calcularTotalReceitasPorPeriodo(
                LocalDate.of(2026, 11, 5),
                LocalDate.of(2026, 11, 10)
        );
        assertEquals(new BigDecimal("260.10"), totalNoPeriodo);

        // 4. Período de um único dia [2026-11-05, 2026-11-05]: 125.33
        BigDecimal totalDiaUnico = movimentacaoFinanceiraService.calcularTotalReceitasPorPeriodo(
                LocalDate.of(2026, 11, 5),
                LocalDate.of(2026, 11, 5)
        );
        assertEquals(new BigDecimal("125.33"), totalDiaUnico);

        // 5. Somente dataInicial informada (>= 2026-11-07): 89.67 + 45.10 + 210.00 = 344.77
        BigDecimal totalAPartirDe = movimentacaoFinanceiraService.calcularTotalReceitasPorPeriodo(
                LocalDate.of(2026, 11, 7),
                null
        );
        assertEquals(new BigDecimal("344.77"), totalAPartirDe);

        // 6. Somente dataFinal informada (<= 2026-11-05): 35.90 + 125.33 = 161.23
        BigDecimal totalAte = movimentacaoFinanceiraService.calcularTotalReceitasPorPeriodo(
                null,
                LocalDate.of(2026, 11, 5)
        );
        assertEquals(new BigDecimal("161.23"), totalAte);

        // 7. Nenhuma data informada: soma todas as receitas (35.90 + 125.33 + 89.67 + 45.10 + 210.00 = 506.00)
        BigDecimal totalGeral = movimentacaoFinanceiraService.calcularTotalReceitasPorPeriodo(null, null);
        assertEquals(new BigDecimal("506.00"), totalGeral);
        assertEquals(new BigDecimal("506.00"), movimentacaoFinanceiraService.calcularTotalReceitas());

        // 8. Período contendo apenas despesa (2026-11-06): deve retornar 0.00
        BigDecimal totalApenasDespesa = movimentacaoFinanceiraService.calcularTotalReceitasPorPeriodo(
                LocalDate.of(2026, 11, 6),
                LocalDate.of(2026, 11, 6)
        );
        assertEquals(new BigDecimal("0.00"), totalApenasDespesa);

        // 9. Filtro adicional por situação (PAGO no período [2026-11-05, 2026-11-10]): 125.33 + 45.10 = 170.43
        BigDecimal totalPagoNoPeriodo = movimentacaoFinanceiraService.calcularTotalReceitasPorPeriodo(
                LocalDate.of(2026, 11, 5),
                LocalDate.of(2026, 11, 10),
                SituacaoMovimentacaoFinanceira.PAGO
        );
        assertEquals(new BigDecimal("170.43"), totalPagoNoPeriodo);
    }

    @Test
    void deveAssociarCategoriaPorIdOuEntidadeERejeitarCategoriaInexistente() {
        CategoriaDespesa insumos = categoriaDespesaService.buscarPorNomeExato("Insumos").orElseThrow();
        LocalDate data = LocalDate.of(2026, 11, 10);

        MovimentacaoFinanceira despesaPorId = movimentacaoFinanceiraService.cadastrarDespesaPorCategoria(
                "Compra de Crucifixos",
                new BigDecimal("120.00"),
                insumos.getIdCategoria(),
                data
        );

        assertNotNull(despesaPorId.getIdMovimentacao());
        assertNotNull(despesaPorId.getCategoriaDespesa());
        assertEquals(insumos.getIdCategoria(), despesaPorId.getCategoriaDespesa().getIdCategoria());
        assertEquals("Insumos", despesaPorId.getCategoria());

        List<MovimentacaoFinanceira> porCategoriaId =
                movimentacaoFinanceiraService.listarDespesasPorCategoria(insumos.getIdCategoria());
        assertEquals(1, porCategoriaId.size());

        List<MovimentacaoFinanceira> porCategoriaNome =
                movimentacaoFinanceiraService.listarDespesasPorCategoria("insumos");
        assertEquals(1, porCategoriaNome.size());

        // Tentativa de associar com ID de categoria inexistente
        IllegalArgumentException exIdInexistente = assertThrows(IllegalArgumentException.class, () ->
                movimentacaoFinanceiraService.cadastrarDespesaPorCategoria(
                        "Despesa inválida",
                        new BigDecimal("50.00"),
                        999999L,
                        data
                )
        );
        assertEquals("Categoria de despesa não encontrada para o ID informado: 999999", exIdInexistente.getMessage());

        // Tentativa de associar com nome de categoria inexistente
        IllegalArgumentException exNomeInexistente = assertThrows(IllegalArgumentException.class, () ->
                movimentacaoFinanceiraService.cadastrarDespesa(
                        "Despesa inválida",
                        new BigDecimal("50.00"),
                        "Categoria Inexistente XYZ",
                        data
                )
        );
        assertEquals("Categoria de despesa não encontrada: Categoria Inexistente XYZ", exNomeInexistente.getMessage());
    }
    @Test
    void deveCalcularTotalDeDespesasPorPeriodoComPrecisaoDecimalExcluindoReceitasERespeitandoLimites() {

        // 1. Sem despesas cadastradas, retorna 0.00
        BigDecimal totalInicialVazio = movimentacaoFinanceiraService.calcularTotalDespesasPorPeriodo(
                LocalDate.of(2026, 11, 1),
                LocalDate.of(2026, 11, 30)
        );
        assertEquals(new BigDecimal("0.00"), totalInicialVazio);

        // 2. Despesas antes, nos limites, dentro e depois do período
        movimentacaoFinanceiraService.cadastrarDespesa(
                "Despesa Fora Antes",
                new BigDecimal("35.90"),
                "Insumos",
                LocalDate.of(2026, 11, 2)
        );

        movimentacaoFinanceiraService.cadastrarDespesa(
                "Despesa no Limite Inicial",
                new BigDecimal("125.33"),
                "Insumos",
                LocalDate.of(2026, 11, 5)
        );

        movimentacaoFinanceiraService.cadastrarDespesa(
                "Despesa Intermediária",
                new BigDecimal("89.67"),
                "Embalagens",
                SituacaoMovimentacaoFinanceira.PENDENTE,
                LocalDate.of(2026, 11, 7),
                LocalDate.of(2026, 11, 20),
                "Pagamento pendente"
        );

        // Receitas que não devem entrar no cálculo
        movimentacaoFinanceiraService.cadastrarReceita(
                "Receita no Período 1",
                new BigDecimal("300.00"),
                LocalDate.of(2026, 11, 6)
        );

        movimentacaoFinanceiraService.cadastrarReceita(
                "Receita no Período 2",
                new BigDecimal("140.50"),
                LocalDate.of(2026, 11, 10)
        );

        movimentacaoFinanceiraService.cadastrarDespesa(
                "Despesa no Limite Final",
                new BigDecimal("45.10"),
                "Embalagens",
                LocalDate.of(2026, 11, 10)
        );

        movimentacaoFinanceiraService.cadastrarDespesa(
                "Despesa Fora Depois",
                new BigDecimal("210.00"),
                "Insumos",
                LocalDate.of(2026, 11, 18)
        );

        // 3. Período fechado: 125.33 + 89.67 + 45.10 = 260.10
        BigDecimal totalNoPeriodo = movimentacaoFinanceiraService.calcularTotalDespesasPorPeriodo(
                LocalDate.of(2026, 11, 5),
                LocalDate.of(2026, 11, 10)
        );
        assertEquals(new BigDecimal("260.10"), totalNoPeriodo);

        // 4. Apenas um dia: 125.33
        BigDecimal totalDiaUnico = movimentacaoFinanceiraService.calcularTotalDespesasPorPeriodo(
                LocalDate.of(2026, 11, 5),
                LocalDate.of(2026, 11, 5)
        );
        assertEquals(new BigDecimal("125.33"), totalDiaUnico);

        // 5. Somente data inicial: 89.67 + 45.10 + 210.00 = 344.77
        BigDecimal totalAPartirDe = movimentacaoFinanceiraService.calcularTotalDespesasPorPeriodo(
                LocalDate.of(2026, 11, 7),
                null
        );
        assertEquals(new BigDecimal("344.77"), totalAPartirDe);

        // 6. Somente data final: 35.90 + 125.33 = 161.23
        BigDecimal totalAte = movimentacaoFinanceiraService.calcularTotalDespesasPorPeriodo(
                null,
                LocalDate.of(2026, 11, 5)
        );
        assertEquals(new BigDecimal("161.23"), totalAte);

        // 7. Nenhuma data: soma todas as despesas = 506.00
        BigDecimal totalGeral = movimentacaoFinanceiraService.calcularTotalDespesasPorPeriodo(null, null);
        assertEquals(new BigDecimal("506.00"), totalGeral);

        // 8. Período contendo somente receita: retorna 0.00
        BigDecimal totalApenasReceita = movimentacaoFinanceiraService.calcularTotalDespesasPorPeriodo(
                LocalDate.of(2026, 11, 6),
                LocalDate.of(2026, 11, 6)
        );
        assertEquals(new BigDecimal("0.00"), totalApenasReceita);

        // 9. Somente despesas PAGO no período: 125.33 + 45.10 = 170.43
        BigDecimal totalPagoNoPeriodo = movimentacaoFinanceiraService.calcularTotalDespesasPorPeriodo(
                LocalDate.of(2026, 11, 5),
                LocalDate.of(2026, 11, 10),
                SituacaoMovimentacaoFinanceira.PAGO
        );
        assertEquals(new BigDecimal("170.43"), totalPagoNoPeriodo);
    }

    @Test
    void deveCalcularSaldoLiquidoPorPeriodoConsiderandoSomenteMovimentacoesPagas() {
        // Receitas
        movimentacaoFinanceiraService.cadastrarReceita(
                "Venda de produtos",
                new BigDecimal("500.00"),
                "Vendas",
                SituacaoMovimentacaoFinanceira.PAGO,
                LocalDate.of(2026, 11, 5),
                null,
                null
        );

        movimentacaoFinanceiraService.cadastrarReceita(
                "Venda pendente",
                new BigDecimal("200.00"),
                "Vendas",
                SituacaoMovimentacaoFinanceira.PENDENTE,
                LocalDate.of(2026, 11, 6),
                LocalDate.of(2026, 11, 20),
                null
        );

        // Despesas
        movimentacaoFinanceiraService.cadastrarDespesa(
                "Compra de materiais",
                new BigDecimal("150.00"),
                "Insumos",
                SituacaoMovimentacaoFinanceira.PAGO,
                LocalDate.of(2026, 11, 7),
                null,
                null
        );

        movimentacaoFinanceiraService.cadastrarDespesa(
                "Compra pendente",
                new BigDecimal("50.00"),
                "Embalagens",
                SituacaoMovimentacaoFinanceira.PENDENTE,
                LocalDate.of(2026, 11, 8),
                LocalDate.of(2026, 11, 20),
                null
        );

        // Período completo
        BigDecimal saldo = movimentacaoFinanceiraService.calcularSaldoLiquidoPorPeriodo(
                LocalDate.of(2026, 11, 1),
                LocalDate.of(2026, 11, 10)
        );

        assertEquals(new BigDecimal("350.00"), saldo);
    }

    @Test
    void deveRetornarZeroQuandoNaoExistiremMovimentacoesNoPeriodo() {
        BigDecimal saldo = movimentacaoFinanceiraService.calcularSaldoLiquidoPorPeriodo(
                LocalDate.of(2026, 11, 1),
                LocalDate.of(2026, 11, 30)
        );

        assertEquals(new BigDecimal("0.00"), saldo);
    }
    @Test
    void deveRetornarSaldoPositivoQuandoExistiremApenasReceitasPagas() {
        movimentacaoFinanceiraService.cadastrarReceita(
                "Venda de produtos",
                new BigDecimal("300.00"),
                "Vendas",
                SituacaoMovimentacaoFinanceira.PAGO,
                LocalDate.of(2026, 11, 5),
                null,
                null
        );

        BigDecimal saldo = movimentacaoFinanceiraService.calcularSaldoLiquidoPorPeriodo(
                LocalDate.of(2026, 11, 1),
                LocalDate.of(2026, 11, 30)
        );

        assertEquals(new BigDecimal("300.00"), saldo);
    }
    @Test
    void deveRetornarSaldoNegativoQuandoExistiremApenasDespesasPagas() {
        movimentacaoFinanceiraService.cadastrarDespesa(
                "Compra de insumos",
                new BigDecimal("120.00"),
                "Insumos",
                SituacaoMovimentacaoFinanceira.PAGO,
                LocalDate.of(2026, 11, 5),
                null,
                null
        );

        BigDecimal saldo = movimentacaoFinanceiraService.calcularSaldoLiquidoPorPeriodo(
                LocalDate.of(2026, 11, 1),
                LocalDate.of(2026, 11, 30)
        );

        assertEquals(new BigDecimal("-120.00"), saldo);
    }
    @Test
    void deveLancarExcecaoQuandoPeriodoDoSaldoLiquidoForInvalido() {
        assertThrows(
                IllegalArgumentException.class,
                () -> movimentacaoFinanceiraService.calcularSaldoLiquidoPorPeriodo(
                        LocalDate.of(2026, 11, 30),
                        LocalDate.of(2026, 11, 1)
                )
        );
    }

    @Test
    void deveCalcularSaldoTotalConsiderandoApenasMovimentacoesPagas() {
        // Receita paga antiga
        movimentacaoFinanceiraService.cadastrarReceita(
                "Receita antiga",
                new BigDecimal("500.00"),
                "Vendas",
                SituacaoMovimentacaoFinanceira.PAGO,
                LocalDate.of(2020, 5, 10),
                null,
                null
        );

        // Receita pendente
        movimentacaoFinanceiraService.cadastrarReceita(
                "Receita pendente",
                new BigDecimal("300.00"),
                "Vendas",
                SituacaoMovimentacaoFinanceira.PENDENTE,
                LocalDate.of(2026, 11, 5),
                LocalDate.of(2026, 11, 20),
                null
        );

        // Receita cancelada
        movimentacaoFinanceiraService.cadastrarReceita(
                "Receita cancelada",
                new BigDecimal("100.00"),
                "Vendas",
                SituacaoMovimentacaoFinanceira.CANCELADO,
                LocalDate.of(2026, 11, 6),
                null,
                null
        );

        // Despesa paga
        movimentacaoFinanceiraService.cadastrarDespesa(
                "Despesa antiga",
                new BigDecimal("200.00"),
                "Insumos",
                SituacaoMovimentacaoFinanceira.PAGO,
                LocalDate.of(2025, 3, 15),
                null,
                null
        );

        // Despesa pendente
        movimentacaoFinanceiraService.cadastrarDespesa(
                "Despesa pendente",
                new BigDecimal("80.00"),
                "Embalagens",
                SituacaoMovimentacaoFinanceira.PENDENTE,
                LocalDate.of(2026, 11, 7),
                LocalDate.of(2026, 11, 20),
                null
        );

        // Despesa cancelada
        movimentacaoFinanceiraService.cadastrarDespesa(
                "Despesa cancelada",
                new BigDecimal("50.00"),
                "Insumos",
                SituacaoMovimentacaoFinanceira.CANCELADO,
                LocalDate.of(2026, 11, 8),
                null,
                null
        );

        BigDecimal saldo = movimentacaoFinanceiraService.calcularSaldoTotal();

        // 500.00 - 200.00 = 300.00
        assertEquals(new BigDecimal("300.00"), saldo);
    }

    @Test
    void deveRetornarZeroQuandoNaoExistiremMovimentacoesNoSaldoTotal() {
        BigDecimal saldo = movimentacaoFinanceiraService.calcularSaldoTotal();

        assertEquals(new BigDecimal("0.00"), saldo);
    }
    @Test
    void deveAgruparDespesasPorCategoriaSomandoOsValores() {
        LocalDate data = LocalDate.of(2026, 11, 10);

        movimentacaoFinanceiraService.cadastrarDespesa(
                "Compra de fios", new BigDecimal("300.00"), "Insumos", data);

        movimentacaoFinanceiraService.cadastrarDespesa(
                "Compra de materiais", new BigDecimal("200.00"), "Insumos", data);

        movimentacaoFinanceiraService.cadastrarDespesa(
                "Compra de caixas", new BigDecimal("150.00"), "Embalagens", data);

        movimentacaoFinanceiraService.cadastrarReceita(
                "Venda de produtos", new BigDecimal("500.00"), data);

        List<ResumoDespesaCategoriaProjection> resultado =
                movimentacaoFinanceiraService.resumirDespesasPorCategoria();

        assertEquals(2, resultado.size());

        ResumoDespesaCategoriaProjection insumos = resultado.stream()
                .filter(r -> r.getCategoria().equals("Insumos"))
                .findFirst()
                .orElseThrow();

        ResumoDespesaCategoriaProjection embalagens = resultado.stream()
                .filter(r -> r.getCategoria().equals("Embalagens"))
                .findFirst()
                .orElseThrow();

        assertEquals(0, new BigDecimal("500.00").compareTo(insumos.getTotal()));
        assertEquals(0, new BigDecimal("150.00").compareTo(embalagens.getTotal()));
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoExistiremDespesas() {
        List<ResumoDespesaCategoriaProjection> resultado =
                movimentacaoFinanceiraService.resumirDespesasPorCategoria();

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    @Test
    void deveListarApenasReceitasPendentesComoContasAReceber() {
        LocalDate data = LocalDate.of(2026, 11, 10);

        movimentacaoFinanceiraService.cadastrarReceita(
                "Venda pendente",
                new BigDecimal("500.00"),
                null,
                SituacaoMovimentacaoFinanceira.PENDENTE,
                data,
                null,
                null
        );

        movimentacaoFinanceiraService.cadastrarReceita(
                "Venda paga",
                new BigDecimal("300.00"),
                null,
                SituacaoMovimentacaoFinanceira.PAGO,
                data,
                null,
                null
        );

        movimentacaoFinanceiraService.cadastrarDespesa(
                "Compra de materiais",
                new BigDecimal("200.00"),
                "Insumos",
                data
        );

        List<MovimentacaoFinanceira> resultado =
                movimentacaoFinanceiraService.listarContasAReceber();

        assertEquals(1, resultado.size());
        assertEquals("Venda pendente", resultado.get(0).getDescricao());
        assertEquals(0, new BigDecimal("500.00").compareTo(resultado.get(0).getValor()));
        assertEquals(SituacaoMovimentacaoFinanceira.PENDENTE, resultado.get(0).getSituacao());
        assertEquals(TipoMovimentacaoFinanceira.RECEITA, resultado.get(0).getTipo());
    }

    @Test
    void deveResumirDespesasPorCategoriaDentroDoPeriodo() {
        LocalDate dataInicio = LocalDate.of(2026, 10, 1);
        LocalDate dataFim = LocalDate.of(2026, 10, 31);

        movimentacaoFinanceiraService.cadastrarDespesa(
                "Compra de materiais",
                new BigDecimal("200.00"),
                "Insumos",
                LocalDate.of(2026, 10, 10)
        );

        movimentacaoFinanceiraService.cadastrarDespesa(
                "Compra de embalagens",
                new BigDecimal("100.00"),
                "Embalagens",
                LocalDate.of(2026, 10, 20)
        );

        movimentacaoFinanceiraService.cadastrarDespesa(
                "Compra fora do período",
                new BigDecimal("500.00"),
                "Insumos",
                LocalDate.of(2026, 11, 5)
        );

        List<ResumoDespesaCategoriaProjection> resultado =
                movimentacaoFinanceiraService.resumirDespesasPorCategoria(
                        dataInicio, dataFim
                );

        assertEquals(2, resultado.size());

        Map<String, BigDecimal> totais = resultado.stream()
                .collect(Collectors.toMap(
                        ResumoDespesaCategoriaProjection::getCategoria,
                        ResumoDespesaCategoriaProjection::getTotal
                ));

        assertEquals(0, new BigDecimal("200.00").compareTo(totais.get("Insumos")));
        assertEquals(0, new BigDecimal("100.00").compareTo(totais.get("Embalagens")));
    }
}

