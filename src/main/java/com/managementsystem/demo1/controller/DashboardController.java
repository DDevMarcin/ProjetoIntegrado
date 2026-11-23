package com.managementsystem.demo1.controller;

import com.managementsystem.demo1.model.Encomenda;
import com.managementsystem.demo1.model.ItemEncomenda;
import com.managementsystem.demo1.model.Material;
import com.managementsystem.demo1.model.SituacaoEncomenda;
import com.managementsystem.demo1.model.SituacaoMovimentacaoFinanceira;
import com.managementsystem.demo1.service.EncomendaService;
import com.managementsystem.demo1.service.MaterialService;
import com.managementsystem.demo1.service.MovimentacaoFinanceiraService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Component
@Scope("prototype")
public class DashboardController {

    private static final BigDecimal LIMITE_ESTOQUE_BAIXO = new BigDecimal("5");
    private static final DateTimeFormatter DATA_CURTA = DateTimeFormatter.ofPattern("dd/MM");
    private static final NumberFormat MOEDA_BR = NumberFormat.getCurrencyInstance(Locale.of("pt", "BR"));

    @FXML private Label lblEmAndamento;
    @FXML private Label lblProntos;
    @FXML private Label lblTotalEncomendas;

    @FXML private Label lblTotalVendas;
    @FXML private Label lblTotalDespesas;
    @FXML private Label lblGanhos;

    @FXML private TableView<Encomenda>           tabelaUrgentes;
    @FXML private TableColumn<Encomenda, String> colCliente;
    @FXML private TableColumn<Encomenda, String> colSituacao;
    @FXML private TableColumn<Encomenda, String> colValor;
    @FXML private TableColumn<Encomenda, String> colData;

    @FXML private VBox boxEstoqueBaixo;
    @FXML private VBox boxSemEstoque;

    private final EncomendaService encomendaService;
    private final MovimentacaoFinanceiraService financeiroService;
    private final MaterialService materialService;

    public DashboardController(EncomendaService encomendaService,
                               MovimentacaoFinanceiraService financeiroService,
                               MaterialService materialService) {
        this.encomendaService = encomendaService;
        this.financeiroService = financeiroService;
        this.materialService = materialService;
    }

    @FXML
    public void initialize() {
        configurarTabela();
        carregarEncomendas();
        carregarFinanceiro();
        carregarEstoque();
    }

    private void configurarTabela() {
        tabelaUrgentes.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        colCliente.setCellValueFactory(c -> {
            Encomenda e = c.getValue();
            String itens = (e.getItens() != null && !e.getItens().isEmpty())
                    ? e.getItens().stream().map(ItemEncomenda::getNome).collect(Collectors.joining(", "))
                    : "";
            String texto = itens.isBlank() ? e.getNomeCliente() : e.getNomeCliente() + " (" + itens + ")";
            return new SimpleStringProperty(texto);
        });

        colSituacao.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getSituacao() == SituacaoEncomenda.EM_PRODUCAO ? "Em Produção" : "Pendente"
        ));

        colValor.setCellValueFactory(c -> {
            BigDecimal v = c.getValue().getValorTotal();
            return new SimpleStringProperty(v != null ? MOEDA_BR.format(v) : "—");
        });

        colData.setCellValueFactory(c -> {
            var data = c.getValue().getDataEntregaPrevista();
            return new SimpleStringProperty(data != null ? data.format(DATA_CURTA) : "—");
        });

        colSituacao.setStyle("-fx-alignment: CENTER;");
        colValor.setStyle("-fx-alignment: CENTER;");
        colData.setStyle("-fx-alignment: CENTER;");
    }

    private void carregarEncomendas() {
        List<Encomenda> todas = encomendaService.listarTodas();

        long emAndamento = todas.stream()
                .filter(e -> e.getSituacao() == SituacaoEncomenda.EM_PRODUCAO)
                .count();
        long prontos = todas.stream()
                .filter(e -> e.getSituacao() == SituacaoEncomenda.PRONTA)
                .count();
        long ativas = todas.stream()
                .filter(e -> e.getSituacao() != SituacaoEncomenda.CANCELADA
                        && e.getSituacao() != SituacaoEncomenda.ENTREGUE)
                .count();

        lblEmAndamento.setText(String.valueOf(emAndamento));
        lblProntos.setText(String.valueOf(prontos));
        lblTotalEncomendas.setText(String.valueOf(ativas));

        // Encomendas urgentes: pendentes ou em produção ordenadas pela data de entrega mais próxima
        List<Encomenda> urgentes = todas.stream()
                .filter(e -> e.getSituacao() == SituacaoEncomenda.PENDENTE
                        || e.getSituacao() == SituacaoEncomenda.EM_PRODUCAO)
                .sorted(Comparator.comparing(
                        Encomenda::getDataEntregaPrevista,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .limit(8)
                .toList();

        tabelaUrgentes.setItems(FXCollections.observableArrayList(urgentes));
    }

    private void carregarFinanceiro() {
        BigDecimal receitas = financeiroService.calcularTotalReceitas();
        BigDecimal despesas = financeiroService.calcularTotalDespesasPorPeriodo(
                null, null, SituacaoMovimentacaoFinanceira.PAGO);
        BigDecimal ganhos = receitas.subtract(despesas);

        lblTotalVendas.setText(MOEDA_BR.format(receitas));
        lblTotalDespesas.setText(MOEDA_BR.format(despesas));
        lblGanhos.setText(MOEDA_BR.format(ganhos));
    }

    private void carregarEstoque() {
        boxEstoqueBaixo.getChildren().clear();
        boxSemEstoque.getChildren().clear();

        List<Material> materiais = materialService.listarTodos();

        List<Material> semEstoque = materiais.stream()
                .filter(m -> m.getQuantidadeEstoque() == null || m.getQuantidadeEstoque().signum() <= 0)
                .toList();

        List<Material> estoqueBaixo = materiais.stream()
                .filter(m -> m.getQuantidadeEstoque() != null
                        && m.getQuantidadeEstoque().signum() > 0
                        && m.getQuantidadeEstoque().compareTo(LIMITE_ESTOQUE_BAIXO) <= 0)
                .sorted(Comparator.comparing(Material::getQuantidadeEstoque))
                .toList();

        if (estoqueBaixo.isEmpty()) {
            Label vazio = new Label("Nenhum material com estoque baixo.");
            vazio.setStyle("-fx-text-fill: #999; -fx-font-size: 12px;");
            vazio.setWrapText(true);
            boxEstoqueBaixo.getChildren().add(vazio);
        } else {
            for (Material m : estoqueBaixo) {
                String qtd = m.getQuantidadeEstoque().stripTrailingZeros().toPlainString()
                        + " " + m.getUnidadeMedida();
                boxEstoqueBaixo.getChildren().add(criarLinhaAlerta(m.getNome(), qtd, "#64748B"));
            }
        }

        if (semEstoque.isEmpty()) {
            Label vazio = new Label("Nenhum material zerado.");
            vazio.setStyle("-fx-text-fill: #999; -fx-font-size: 12px;");
            vazio.setWrapText(true);
            boxSemEstoque.getChildren().add(vazio);
        } else {
            for (Material m : semEstoque) {
                boxSemEstoque.getChildren().add(criarLinhaAlerta(m.getNome(), "0 " + m.getUnidadeMedida(), "#D31946"));
            }
        }
    }

    private HBox criarLinhaAlerta(String nome, String quantidade, String corBolinha) {
        HBox linha = new HBox(8);
        linha.setAlignment(Pos.CENTER_LEFT);

        Label dot = new Label("•");
        dot.setStyle("-fx-text-fill: " + corBolinha + "; -fx-font-size: 16px; -fx-font-weight: bold;");

        Label lblNome = new Label(nome);
        lblNome.setStyle("-fx-text-fill: #1a1a2e; -fx-font-size: 13px; -fx-font-weight: bold;");
        lblNome.setWrapText(true);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label lblQtd = new Label(quantidade);
        lblQtd.setStyle("-fx-text-fill: #777; -fx-font-size: 12px;");

        linha.getChildren().addAll(dot, lblNome, spacer, lblQtd);
        return linha;
    }
}