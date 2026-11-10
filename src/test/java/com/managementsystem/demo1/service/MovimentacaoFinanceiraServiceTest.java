package com.managementsystem.demo1.service;

import com.managementsystem.demo1.BackendApplication;
import com.managementsystem.demo1.model.MovimentacaoFinanceira;
import com.managementsystem.demo1.model.SituacaoMovimentacaoFinanceira;
import com.managementsystem.demo1.model.TipoMovimentacaoFinanceira;
import com.managementsystem.demo1.repository.MovimentacaoFinanceiraRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = BackendApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
class MovimentacaoFinanceiraServiceTest {

    @Autowired
    private MovimentacaoFinanceiraService movimentacaoFinanceiraService;

    @Autowired
    private MovimentacaoFinanceiraRepository movimentacaoFinanceiraRepository;

    @BeforeEach
    void setUp() {
        movimentacaoFinanceiraRepository.deleteAll();
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
}

