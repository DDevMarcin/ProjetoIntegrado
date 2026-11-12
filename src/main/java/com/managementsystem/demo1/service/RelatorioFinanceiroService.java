package com.managementsystem.demo1.service;


import com.managementsystem.demo1.model.MovimentacaoFinanceira;
import com.managementsystem.demo1.repository.ResumoDespesaCategoriaProjection;
import org.springframework.stereotype.Service;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import com.managementsystem.demo1.model.SituacaoMovimentacaoFinanceira;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import com.lowagie.text.pdf.PdfPTable;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;

@Service
public class RelatorioFinanceiroService {
    private final MovimentacaoFinanceiraService movimentacaoFinanceiraService;

    public RelatorioFinanceiroService(
            MovimentacaoFinanceiraService movimentacaoFinanceiraService
    ) {
        this.movimentacaoFinanceiraService = movimentacaoFinanceiraService;
    }

    public byte[] gerarRelatorio(LocalDate dataInicio, LocalDate dataFim) {

        BigDecimal totalReceitas =
                movimentacaoFinanceiraService.calcularTotalReceitasPorPeriodo(
                        dataInicio, dataFim, SituacaoMovimentacaoFinanceira.PAGO
                );

        BigDecimal totalDespesas =
                movimentacaoFinanceiraService.calcularTotalDespesasPorPeriodo(
                        dataInicio, dataFim, SituacaoMovimentacaoFinanceira.PAGO
                );

        BigDecimal saldoLiquido =
                movimentacaoFinanceiraService.calcularSaldoLiquidoPorPeriodo(
                        dataInicio, dataFim
                );

        List<ResumoDespesaCategoriaProjection> despesasPorCategoria =
                movimentacaoFinanceiraService.resumirDespesasPorCategoria(
                        dataInicio,
                        dataFim,
                        SituacaoMovimentacaoFinanceira.PAGO
                );

        NumberFormat formatoMoeda = NumberFormat.getCurrencyInstance(
                Locale.forLanguageTag("pt-BR")
        );

        List<MovimentacaoFinanceira> contasAReceber =
                movimentacaoFinanceiraService.listarContasAReceberPorPeriodo(
                        dataInicio, dataFim
                );

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        Document documento = new Document();

        try {
            PdfWriter.getInstance(documento, outputStream);

            documento.open();

            documento.add(new Paragraph("Relatório Financeiro"));
            documento.add(new Paragraph(
                    "Período: " + dataInicio + " até " + dataFim
            ));

            documento.add(new Paragraph(" "));
            documento.add(new Paragraph("RESUMO FINANCEIRO"));

            documento.add(new Paragraph(
                    "Total de receitas: " + formatoMoeda.format(totalReceitas)
            ));

            documento.add(new Paragraph(
                    "Total de despesas: " + formatoMoeda.format(totalDespesas)
            ));

            documento.add(new Paragraph(
                    "Saldo líquido: " + formatoMoeda.format(saldoLiquido)
            ));

            documento.add(new Paragraph(" "));
            documento.add(new Paragraph("DESPESAS POR CATEGORIA"));

            PdfPTable tabela = new PdfPTable(2);
            tabela.setWidthPercentage(100);

            tabela.addCell("Categoria");
            tabela.addCell("Total");

            for (ResumoDespesaCategoriaProjection despesa : despesasPorCategoria) {
                tabela.addCell(despesa.getCategoria());
                tabela.addCell(formatoMoeda.format(despesa.getTotal()));
            }

            documento.add(tabela);

            documento.add(new Paragraph(" "));
            documento.add(new Paragraph("CONTAS A RECEBER"));

            PdfPTable tabelaContas = new PdfPTable(3);
            tabelaContas.setWidthPercentage(100);

            tabelaContas.addCell("Descrição");
            tabelaContas.addCell("Valor");
            tabelaContas.addCell("Vencimento");

            for (MovimentacaoFinanceira conta : contasAReceber) {
                tabelaContas.addCell(conta.getDescricao());
                tabelaContas.addCell(formatoMoeda.format(conta.getValor()));
                tabelaContas.addCell(conta.getDataVencimento().toString());
            }

            documento.add(tabelaContas);

            documento.close();

            return outputStream.toByteArray();

        } catch (DocumentException e) {
            throw new RuntimeException("Erro ao gerar relatório financeiro.", e);
        }

    }
}