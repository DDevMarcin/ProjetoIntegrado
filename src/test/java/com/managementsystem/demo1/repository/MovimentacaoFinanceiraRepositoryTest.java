package com.managementsystem.demo1.repository;

import com.managementsystem.demo1.BackendApplication;
import com.managementsystem.demo1.model.MovimentacaoFinanceira;
import com.managementsystem.demo1.model.SituacaoMovimentacaoFinanceira;
import com.managementsystem.demo1.model.TipoMovimentacaoFinanceira;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = BackendApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
class MovimentacaoFinanceiraRepositoryTest {

    @Autowired
    private MovimentacaoFinanceiraRepository movimentacaoFinanceiraRepository;

    @BeforeEach
    void setUp() {
        movimentacaoFinanceiraRepository.deleteAll();
    }

    @Test
    void devePersistirERecuperarMovimentacaoFinanceiraDeReceitaCompleta() {
        LocalDate dataMov = LocalDate.of(2026, 11, 7);
        LocalDate dataVenc = LocalDate.of(2026, 11, 10);

        MovimentacaoFinanceira receita = new MovimentacaoFinanceira(
                "Venda de Terço Personalizado",
                new BigDecimal("150.00"),
                TipoMovimentacaoFinanceira.RECEITA,
                "Vendas",
                SituacaoMovimentacaoFinanceira.PAGO,
                dataMov,
                dataVenc,
                "Pagamento via PIX"
        );

        MovimentacaoFinanceira salva = movimentacaoFinanceiraRepository.save(receita);

        assertNotNull(salva.getIdMovimentacao());

        Optional<MovimentacaoFinanceira> encontradaOpt =
                movimentacaoFinanceiraRepository.findById(salva.getIdMovimentacao());

        assertTrue(encontradaOpt.isPresent());
        MovimentacaoFinanceira encontrada = encontradaOpt.get();
        assertEquals("Venda de Terço Personalizado", encontrada.getDescricao());
        assertEquals(0, new BigDecimal("150.00").compareTo(encontrada.getValor()));
        assertEquals(TipoMovimentacaoFinanceira.RECEITA, encontrada.getTipo());
        assertEquals("Vendas", encontrada.getCategoria());
        assertEquals(SituacaoMovimentacaoFinanceira.PAGO, encontrada.getSituacao());
        assertEquals(dataMov, encontrada.getDataMovimentacao());
        assertEquals(dataVenc, encontrada.getDataVencimento());
        assertEquals("Pagamento via PIX", encontrada.getObservacoes());
    }

    @Test
    void devePersistirMovimentacaoDeDespesaApenasComCamposObrigatoriosAssumindoSituacaoPadrao() {
        LocalDate dataMov = LocalDate.of(2026, 11, 5);

        MovimentacaoFinanceira despesa = new MovimentacaoFinanceira(
                "Compra de fios encerados",
                new BigDecimal("45.90"),
                TipoMovimentacaoFinanceira.DESPESA,
                dataMov
        );

        MovimentacaoFinanceira salva = movimentacaoFinanceiraRepository.save(despesa);

        assertNotNull(salva.getIdMovimentacao());
        assertEquals(SituacaoMovimentacaoFinanceira.PAGO, salva.getSituacao());
        assertNull(salva.getCategoria());
        assertNull(salva.getDataVencimento());
        assertNull(salva.getObservacoes());
    }

    @Test
    void deveFiltrarMovimentacoesPorTipoSituacaoCategoriaPeriodoEDescricao() {
        MovimentacaoFinanceira m1 = new MovimentacaoFinanceira(
                "Venda Terço Madeira",
                new BigDecimal("50.00"),
                TipoMovimentacaoFinanceira.RECEITA,
                "Vendas",
                SituacaoMovimentacaoFinanceira.PAGO,
                LocalDate.of(2026, 11, 1),
                null,
                null
        );

        MovimentacaoFinanceira m2 = new MovimentacaoFinanceira(
                "Compra Contas de Cristal",
                new BigDecimal("120.50"),
                TipoMovimentacaoFinanceira.DESPESA,
                "Insumos",
                SituacaoMovimentacaoFinanceira.PENDENTE,
                LocalDate.of(2026, 11, 5),
                LocalDate.of(2026, 11, 15),
                "Boleto fornecedor"
        );

        MovimentacaoFinanceira m3 = new MovimentacaoFinanceira(
                "Encomenda Rosário Especial",
                new BigDecimal("210.00"),
                TipoMovimentacaoFinanceira.RECEITA,
                "Vendas",
                SituacaoMovimentacaoFinanceira.CANCELADO,
                LocalDate.of(2026, 11, 7),
                null,
                "Cancelado pelo cliente"
        );

        movimentacaoFinanceiraRepository.saveAll(List.of(m1, m2, m3));

        List<MovimentacaoFinanceira> receitas =
                movimentacaoFinanceiraRepository.findByTipoOrderByDataMovimentacaoDescIdMovimentacaoDesc(
                        TipoMovimentacaoFinanceira.RECEITA);
        assertEquals(2, receitas.size());
        assertEquals("Encomenda Rosário Especial", receitas.get(0).getDescricao());

        List<MovimentacaoFinanceira> pendentes =
                movimentacaoFinanceiraRepository.findBySituacaoOrderByDataMovimentacaoDescIdMovimentacaoDesc(
                        SituacaoMovimentacaoFinanceira.PENDENTE);
        assertEquals(1, pendentes.size());
        assertEquals("Compra Contas de Cristal", pendentes.get(0).getDescricao());

        List<MovimentacaoFinanceira> categoriaVendas =
                movimentacaoFinanceiraRepository.findByCategoriaIgnoreCaseOrderByDataMovimentacaoDescIdMovimentacaoDesc(
                        "vendas");
        assertEquals(2, categoriaVendas.size());

        List<MovimentacaoFinanceira> noPeriodo =
                movimentacaoFinanceiraRepository.findByDataMovimentacaoBetweenOrderByDataMovimentacaoDescIdMovimentacaoDesc(
                        LocalDate.of(2026, 11, 2),
                        LocalDate.of(2026, 11, 7));
        assertEquals(2, noPeriodo.size());

        List<MovimentacaoFinanceira> porDescricao =
                movimentacaoFinanceiraRepository.findByDescricaoContainingIgnoreCaseOrderByDataMovimentacaoDescIdMovimentacaoDesc(
                        "terço");
        assertEquals(1, porDescricao.size());
        assertEquals("Venda Terço Madeira", porDescricao.get(0).getDescricao());

        List<MovimentacaoFinanceira> todasOrdenadas =
                movimentacaoFinanceiraRepository.findAllByOrderByDataMovimentacaoDescIdMovimentacaoDesc();
        assertEquals(3, todasOrdenadas.size());
        assertEquals("Encomenda Rosário Especial", todasOrdenadas.get(0).getDescricao());
        assertEquals("Compra Contas de Cristal", todasOrdenadas.get(1).getDescricao());
        assertEquals("Venda Terço Madeira", todasOrdenadas.get(2).getDescricao());

        List<MovimentacaoFinanceira> receitasPagas =
                movimentacaoFinanceiraRepository.findByTipoAndSituacaoOrderByDataMovimentacaoDescIdMovimentacaoDesc(
                        TipoMovimentacaoFinanceira.RECEITA,
                        SituacaoMovimentacaoFinanceira.PAGO
                );
        assertEquals(1, receitasPagas.size());
        assertEquals("Venda Terço Madeira", receitasPagas.get(0).getDescricao());

        List<MovimentacaoFinanceira> despesasInsumos =
                movimentacaoFinanceiraRepository.findByTipoAndCategoriaIgnoreCaseOrderByDataMovimentacaoDescIdMovimentacaoDesc(
                        TipoMovimentacaoFinanceira.DESPESA,
                        "insumos"
                );
        assertEquals(1, despesasInsumos.size());
        assertEquals("Compra Contas de Cristal", despesasInsumos.get(0).getDescricao());

        List<MovimentacaoFinanceira> aPartirDeCinco =
                movimentacaoFinanceiraRepository.findByDataMovimentacaoGreaterThanEqualOrderByDataMovimentacaoDescIdMovimentacaoDesc(
                        LocalDate.of(2026, 11, 5)
                );
        assertEquals(2, aPartirDeCinco.size());
        assertEquals("Encomenda Rosário Especial", aPartirDeCinco.get(0).getDescricao());
        assertEquals("Compra Contas de Cristal", aPartirDeCinco.get(1).getDescricao());

        List<MovimentacaoFinanceira> ateCinco =
                movimentacaoFinanceiraRepository.findByDataMovimentacaoLessThanEqualOrderByDataMovimentacaoDescIdMovimentacaoDesc(
                        LocalDate.of(2026, 11, 5)
                );
        assertEquals(2, ateCinco.size());
        assertEquals("Compra Contas de Cristal", ateCinco.get(0).getDescricao());
        assertEquals("Venda Terço Madeira", ateCinco.get(1).getDescricao());

        List<MovimentacaoFinanceira> receitasNoPeriodo =
                movimentacaoFinanceiraRepository.findByTipoAndDataMovimentacaoBetweenOrderByDataMovimentacaoDescIdMovimentacaoDesc(
                        TipoMovimentacaoFinanceira.RECEITA,
                        LocalDate.of(2026, 11, 1),
                        LocalDate.of(2026, 11, 6)
                );
        assertEquals(1, receitasNoPeriodo.size());
        assertEquals("Venda Terço Madeira", receitasNoPeriodo.get(0).getDescricao());
    }

    @Test
    void deveRejeitarPersistenciaQuandoCamposObrigatoriosForemNulos() {
        MovimentacaoFinanceira semDescricao = new MovimentacaoFinanceira(
                null,
                new BigDecimal("10.00"),
                TipoMovimentacaoFinanceira.RECEITA,
                LocalDate.now()
        );
        assertThrows(DataIntegrityViolationException.class,
                () -> movimentacaoFinanceiraRepository.saveAndFlush(semDescricao));

        MovimentacaoFinanceira semValor = new MovimentacaoFinanceira(
                "Receita teste",
                null,
                TipoMovimentacaoFinanceira.RECEITA,
                LocalDate.now()
        );
        assertThrows(DataIntegrityViolationException.class,
                () -> movimentacaoFinanceiraRepository.saveAndFlush(semValor));

        MovimentacaoFinanceira semTipo = new MovimentacaoFinanceira(
                "Receita teste",
                new BigDecimal("10.00"),
                null,
                LocalDate.now()
        );
        assertThrows(DataIntegrityViolationException.class,
                () -> movimentacaoFinanceiraRepository.saveAndFlush(semTipo));

        MovimentacaoFinanceira semData = new MovimentacaoFinanceira(
                "Receita teste",
                new BigDecimal("10.00"),
                TipoMovimentacaoFinanceira.RECEITA,
                null
        );
        assertThrows(DataIntegrityViolationException.class,
                () -> movimentacaoFinanceiraRepository.saveAndFlush(semData));
    }

    @Test
    void deveAtualizarEExcluirMovimentacaoFinanceiraPersistida() {
        MovimentacaoFinanceira lancamento = new MovimentacaoFinanceira(
                "Compra de Embalagens",
                new BigDecimal("35.75"),
                TipoMovimentacaoFinanceira.DESPESA,
                "Embalagens",
                SituacaoMovimentacaoFinanceira.PENDENTE,
                LocalDate.of(2026, 11, 7),
                LocalDate.of(2026, 11, 20),
                "Aguardando nota fiscal"
        );

        MovimentacaoFinanceira salvo = movimentacaoFinanceiraRepository.save(lancamento);
        Long id = salvo.getIdMovimentacao();
        assertNotNull(id);

        salvo.setSituacao(SituacaoMovimentacaoFinanceira.PAGO);
        salvo.setValor(new BigDecimal("38.90"));
        salvo.setObservacoes("Pago com frete incluso");
        movimentacaoFinanceiraRepository.save(salvo);

        Optional<MovimentacaoFinanceira> atualizadoOpt = movimentacaoFinanceiraRepository.findById(id);
        assertTrue(atualizadoOpt.isPresent());
        assertEquals(SituacaoMovimentacaoFinanceira.PAGO, atualizadoOpt.get().getSituacao());
        assertEquals(0, new BigDecimal("38.90").compareTo(atualizadoOpt.get().getValor()));
        assertEquals("Pago com frete incluso", atualizadoOpt.get().getObservacoes());

        movimentacaoFinanceiraRepository.deleteById(id);
        assertFalse(movimentacaoFinanceiraRepository.findById(id).isPresent());
    }

    @Test
    void deveRespeitarEqualsHashCodeEToStringDaEntidade() {
        MovimentacaoFinanceira m1 = new MovimentacaoFinanceira(
                1L,
                "Lançamento 1",
                new BigDecimal("80.00"),
                TipoMovimentacaoFinanceira.RECEITA,
                "Vendas",
                null,
                LocalDate.of(2026, 11, 7),
                null,
                null
        );
        MovimentacaoFinanceira m2 = new MovimentacaoFinanceira();
        m2.setIdMovimentacao(1L);
        m2.setSituacao(null);

        MovimentacaoFinanceira mSemId = new MovimentacaoFinanceira();

        assertEquals(m1, m1);
        assertEquals(m1, m2);
        assertNotEquals(m1, mSemId);
        assertNotEquals(mSemId, m1);
        assertNotEquals(m1, null);
        assertNotEquals(m1, "outroObjeto");
        assertEquals(m1.hashCode(), m2.hashCode());
        assertEquals(SituacaoMovimentacaoFinanceira.PAGO, m1.getSituacao());
        assertEquals(SituacaoMovimentacaoFinanceira.PAGO, m2.getSituacao());
        assertTrue(m1.toString().contains("MovimentacaoFinanceira{"));
    }
}

