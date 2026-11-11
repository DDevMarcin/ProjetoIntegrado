package com.managementsystem.demo1.service;

import com.managementsystem.demo1.model.SituacaoMovimentacaoFinanceira;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import com.managementsystem.demo1.model.MovimentacaoFinanceira;
import com.managementsystem.demo1.model.TipoMovimentacaoFinanceira;
import java.io.ByteArrayInputStream;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RelatorioFinanceiroServiceTest {

    private final MovimentacaoFinanceiraService movimentacaoService =
            mock(MovimentacaoFinanceiraService.class);

    private final RelatorioFinanceiroService service =
            new RelatorioFinanceiroService(movimentacaoService);

    @Test
    void deveGerarArquivoPdf() throws Exception {
        LocalDate inicio = LocalDate.of(2026, 10, 1);
        LocalDate fim = LocalDate.of(2026, 10, 31);

        when(movimentacaoService.calcularTotalReceitasPorPeriodo(
                inicio, fim, SituacaoMovimentacaoFinanceira.PAGO
        )).thenReturn(new BigDecimal("1000.00"));

        when(movimentacaoService.calcularTotalDespesasPorPeriodo(
                inicio, fim, SituacaoMovimentacaoFinanceira.PAGO
        )).thenReturn(new BigDecimal("400.00"));

        when(movimentacaoService.calcularSaldoLiquidoPorPeriodo(
                inicio, fim
        )).thenReturn(new BigDecimal("600.00"));

        when(movimentacaoService.resumirDespesasPorCategoria(inicio, fim))
                .thenReturn(List.of());
        MovimentacaoFinanceira conta = new MovimentacaoFinanceira(
                "Venda a prazo",
                new BigDecimal("250.00"),
                TipoMovimentacaoFinanceira.RECEITA,
                null,
                SituacaoMovimentacaoFinanceira.PENDENTE,
                inicio,
                LocalDate.of(2026, 10, 15),
                null
        );

        when(movimentacaoService.listarContasAReceberPorPeriodo(inicio, fim))
                .thenReturn(List.of(conta));

        byte[] pdf = service.gerarRelatorio(inicio, fim);

        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
        assertEquals("%PDF-", new String(pdf, 0, 5));

        PdfReader reader = new PdfReader(new ByteArrayInputStream(pdf));

        PdfTextExtractor extractor = new PdfTextExtractor(reader);

        String conteudo = extractor.getTextFromPage(1);

        assertTrue(conteudo.contains("Relatório Financeiro"));
        assertTrue(conteudo.contains("CONTAS A RECEBER"));
        assertTrue(conteudo.contains("Venda a prazo"));
        assertTrue(conteudo.contains("250,00"));

        reader.close();
    }
}