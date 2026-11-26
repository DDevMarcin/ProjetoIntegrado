package com.managementsystem.demo1.controller;

import com.managementsystem.demo1.model.CategoriaDespesa;
import com.managementsystem.demo1.model.MovimentacaoFinanceira;
import com.managementsystem.demo1.model.SituacaoMovimentacaoFinanceira;
import com.managementsystem.demo1.model.TipoMovimentacaoFinanceira;
import com.managementsystem.demo1.repository.ResumoDespesaCategoriaProjection;
import com.managementsystem.demo1.service.CategoriaDespesaService;
import com.managementsystem.demo1.service.MovimentacaoFinanceiraService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import java.io.IOException;

import java.io.File;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Locale;

@Component
@Scope("prototype")
public class FinanceiroController {

    private static final DateTimeFormatter DATA_CURTA = DateTimeFormatter.ofPattern("dd/MM");
    private static final DateTimeFormatter DATA_COMPLETA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final NumberFormat MOEDA_BR = NumberFormat.getCurrencyInstance(Locale.of("pt", "BR"));

    private static final String ESTILO_CHIP_ATIVO =
            "-fx-background-color: #F4A2B8; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 5 14; -fx-font-size: 12px; -fx-cursor: hand;";
    private static final String ESTILO_CHIP_INATIVO =
            "-fx-background-color: white; -fx-text-fill: #555; -fx-border-color: #E2E8F0; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 5 14; -fx-font-size: 12px; -fx-cursor: hand;";

    @FXML private Label lblPeriodoTitulo;
    @FXML private Button btnPeriodoSemanal;
    @FXML private Button btnPeriodoMensal;
    @FXML private Button btnPeriodoAnual;
    @FXML private Button btnPeriodoPersonalizado;
    @FXML private HBox boxDatasPersonalizadas;
    @FXML private DatePicker dpInicio;
    @FXML private DatePicker dpFim;

    @FXML private Button btnFiltroTodos;
    @FXML private Button btnFiltroReceitas;
    @FXML private Button btnFiltroDespesas;

    @FXML private Label lblSaldoTotal;
    @FXML private Label lblEntradaTopo;
    @FXML private Label lblSaidaTopo;

    @FXML private Label lblReceitasPeriodo;
    @FXML private Label lblDespesasPeriodo;
    @FXML private Label lblSaldoLiquido;

    @FXML private Label lblTotalCategorias;
    @FXML private FlowPane flowCategorias;

    @FXML private TextField txtBuscaMovimentacao;
    @FXML private VBox boxMovimentacoes;

    private final MovimentacaoFinanceiraService financeiroService;
    private final CategoriaDespesaService categoriaDespesaService;

    private LocalDate dataInicio;
    private LocalDate dataFim;
    private TipoMovimentacaoFinanceira tipoFiltro = null;
    

    public FinanceiroController(MovimentacaoFinanceiraService financeiroService,
                                CategoriaDespesaService categoriaDespesaService) {
        this.financeiroService = financeiroService;
        this.categoriaDespesaService = categoriaDespesaService;
    }

    @FXML
    public void initialize() {
        categoriaDespesaService.garantirCategoriasPadrao();

        txtBuscaMovimentacao.textProperty().addListener((obs, oldVal, newVal) -> atualizarListaMovimentacoes());

        selecionarMensal();
        filtrarTodos();
    }

    // ── Filtros de Período ──

    @FXML
    public void selecionarSemanal() {
        LocalDate hoje = LocalDate.now();
        dataInicio = hoje.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        dataFim = hoje.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        ocultarDatasPersonalizadas();
        destacarBotaoPeriodo(btnPeriodoSemanal);
        atualizarTela();
    }

    @FXML
    public void selecionarMensal() {
        LocalDate hoje = LocalDate.now();
        dataInicio = hoje.withDayOfMonth(1);
        dataFim = hoje.with(TemporalAdjusters.lastDayOfMonth());
        ocultarDatasPersonalizadas();
        destacarBotaoPeriodo(btnPeriodoMensal);
        atualizarTela();
    }

    @FXML
    public void selecionarAnual() {
        LocalDate hoje = LocalDate.now();
        dataInicio = hoje.withDayOfYear(1);
        dataFim = hoje.with(TemporalAdjusters.lastDayOfYear());
        ocultarDatasPersonalizadas();
        destacarBotaoPeriodo(btnPeriodoAnual);
        atualizarTela();
    }

    @FXML
    public void selecionarPersonalizado() {
        boxDatasPersonalizadas.setVisible(true);
        boxDatasPersonalizadas.setManaged(true);
        if (dpInicio.getValue() == null) dpInicio.setValue(dataInicio);
        if (dpFim.getValue() == null) dpFim.setValue(dataFim);
        destacarBotaoPeriodo(btnPeriodoPersonalizado);
    }

    @FXML
    public void aplicarDatasPersonalizadas() {
        if (dpInicio.getValue() != null && dpFim.getValue() != null) {
            if (dpInicio.getValue().isAfter(dpFim.getValue())) {
                mostrarErro("Período inválido", "A data inicial não pode ser posterior à data final.");
                return;
            }
            dataInicio = dpInicio.getValue();
            dataFim = dpFim.getValue();
            atualizarTela();
        }
    }

    private void ocultarDatasPersonalizadas() {
        boxDatasPersonalizadas.setVisible(false);
        boxDatasPersonalizadas.setManaged(false);
    }

    private void destacarBotaoPeriodo(Button ativo) {
        for (Button b : List.of(btnPeriodoSemanal, btnPeriodoMensal, btnPeriodoAnual, btnPeriodoPersonalizado)) {
            b.setStyle(b == ativo ? ESTILO_CHIP_ATIVO : ESTILO_CHIP_INATIVO);
        }
    }

    // ── Filtros de Tipo ──

    @FXML
    public void filtrarTodos() {
        tipoFiltro = null;
        destacarBotaoTipo(btnFiltroTodos);
        atualizarListaMovimentacoes();
    }

    @FXML
    public void filtrarReceitas() {
        tipoFiltro = TipoMovimentacaoFinanceira.RECEITA;
        destacarBotaoTipo(btnFiltroReceitas);
        atualizarListaMovimentacoes();
    }

    @FXML
    public void filtrarDespesas() {
        tipoFiltro = TipoMovimentacaoFinanceira.DESPESA;
        destacarBotaoTipo(btnFiltroDespesas);
        atualizarListaMovimentacoes();
    }

    private void destacarBotaoTipo(Button ativo) {
        for (Button b : List.of(btnFiltroTodos, btnFiltroReceitas, btnFiltroDespesas)) {
            b.setStyle(b == ativo ? ESTILO_CHIP_ATIVO : ESTILO_CHIP_INATIVO);
        }
    }

    // ── Atualização dos Dados ──

    private void atualizarTela() {
        if (dataInicio != null && dataFim != null) {
            lblPeriodoTitulo.setText(String.format("Período de análise (%s - %s)",
                    dataInicio.format(DATA_CURTA), dataFim.format(DATA_CURTA)));
        }

        // Totais acumulados gerais (Card Principal do Topo - apenas PAGO)
        BigDecimal entradasGerais = financeiroService.calcularTotalReceitasPorPeriodo(
                null, null, SituacaoMovimentacaoFinanceira.PAGO);
        BigDecimal saidasGerais = financeiroService.calcularTotalDespesasPorPeriodo(
                null, null, SituacaoMovimentacaoFinanceira.PAGO);
        BigDecimal saldoTotal = entradasGerais.subtract(saidasGerais);

        lblSaldoTotal.setText(MOEDA_BR.format(saldoTotal));
        lblEntradaTopo.setText(MOEDA_BR.format(entradasGerais));
        lblSaidaTopo.setText(MOEDA_BR.format(saidasGerais));

        // Totais do período selecionado (3 Cards de Análise - apenas PAGO)
        BigDecimal receitasPeriodo = financeiroService.calcularTotalReceitasPorPeriodo(
                dataInicio, dataFim, SituacaoMovimentacaoFinanceira.PAGO);
        BigDecimal despesasPeriodo = financeiroService.calcularTotalDespesasPorPeriodo(
                dataInicio, dataFim, SituacaoMovimentacaoFinanceira.PAGO);
        BigDecimal saldoLiquido = financeiroService.calcularSaldoLiquidoPorPeriodo(dataInicio, dataFim);

        lblReceitasPeriodo.setText(MOEDA_BR.format(receitasPeriodo));
        lblDespesasPeriodo.setText(MOEDA_BR.format(despesasPeriodo));
        lblSaldoLiquido.setText(MOEDA_BR.format(saldoLiquido));

        atualizarResumoCategorias(despesasPeriodo);
        atualizarListaMovimentacoes();
    }

    private void atualizarResumoCategorias(BigDecimal totalDespesas) {
        flowCategorias.getChildren().clear();
        lblTotalCategorias.setText("Total: " + MOEDA_BR.format(totalDespesas));

        List<ResumoDespesaCategoriaProjection> resumo = financeiroService.resumirDespesasPorCategoria(
                dataInicio, dataFim, SituacaoMovimentacaoFinanceira.PAGO);

        if (resumo.isEmpty()) {
            Label vazio = new Label("Nenhuma despesa paga neste período.");
            vazio.setStyle("-fx-text-fill: #999; -fx-font-size: 12px;");
            flowCategorias.getChildren().add(vazio);
            return;
        }

        for (ResumoDespesaCategoriaProjection item : resumo) {
            VBox card = new VBox(4);
            card.setPrefWidth(120);
            card.setStyle("-fx-background-color: #FFF8FB; -fx-border-color: #f5d6e0; -fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 10 12;");

            String nomeCat = (item.getCategoria() != null && !item.getCategoria().isBlank())
                    ? item.getCategoria() : "Outros";
            Label lblNome = new Label(nomeCat);
            lblNome.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");

            BigDecimal val = item.getTotal() != null ? item.getTotal() : BigDecimal.ZERO;
            Label lblValor = new Label(MOEDA_BR.format(val));
            lblValor.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #D31946;");

            card.getChildren().addAll(lblNome, lblValor);
            flowCategorias.getChildren().add(card);
        }
    }

    private void atualizarListaMovimentacoes() {
        boxMovimentacoes.getChildren().clear();
        String busca = txtBuscaMovimentacao.getText();

        List<MovimentacaoFinanceira> lista = financeiroService.consultar(
                tipoFiltro, null, null, null, dataInicio, dataFim, busca);

        if (lista.isEmpty()) {
            Label vazio = new Label("Nenhuma movimentação encontrada.");
            vazio.setStyle("-fx-text-fill: #999; -fx-font-size: 12px;");
            boxMovimentacoes.getChildren().add(vazio);
            return;
        }

        for (MovimentacaoFinanceira mov : lista) {
            HBox itemBox = new HBox(8);
            itemBox.setAlignment(Pos.CENTER_LEFT);
            itemBox.setStyle("-fx-border-color: transparent transparent #F1F5F9 transparent; -fx-padding: 0 0 8 0;");

            VBox info = new VBox(2);
            HBox.setHgrow(info, Priority.ALWAYS);

            Label lblDesc = new Label(mov.getDescricao());
            lblDesc.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #1a1a2e;");
            lblDesc.setWrapText(true);

            String dataStr = mov.getDataMovimentacao() != null ? mov.getDataMovimentacao().format(DATA_CURTA) : "";
            String catStr = mov.getCategoria() != null ? mov.getCategoria() : "";
            String sub = catStr.isBlank() ? dataStr : dataStr + " • " + catStr;
            boolean pendente = mov.getSituacao() == SituacaoMovimentacaoFinanceira.PENDENTE;
            if (pendente) {
                sub += " • Pendente";
            }

            Label lblSub = new Label(sub);
            lblSub.setStyle("-fx-font-size: 11px; -fx-text-fill: " + (pendente ? "#D97706;" : "#888;"));

            info.getChildren().addAll(lblDesc, lblSub);

            boolean isReceita = mov.getTipo() == TipoMovimentacaoFinanceira.RECEITA;
            String sinal = isReceita ? "+" : "-";
            String cor = isReceita ? "#16A34A" : "#D31946";

            VBox direita = new VBox(4);
            direita.setAlignment(Pos.CENTER_RIGHT);

            Label lblVal = new Label(sinal + " " + MOEDA_BR.format(mov.getValor()));
            lblVal.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: " + cor + ";");
            direita.getChildren().add(lblVal);

            if (pendente) {
                Button btnPagar = new Button("✓ Pagar");
                btnPagar.setStyle("-fx-background-color: #FEF3C7; -fx-text-fill: #B45309; -fx-font-size: 10px; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 2 8; -fx-cursor: hand;");
                btnPagar.setOnAction(e -> confirmarPagamento(mov));
                direita.getChildren().add(btnPagar);
            }

            itemBox.getChildren().addAll(info, direita);
            boxMovimentacoes.getChildren().add(itemBox);
        }
    }

    // ── Modais de Cadastro ──

    
    @FXML
    public void abrirModalNovaDespesa() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/NovaDespesa.fxml")
            );

            Parent conteudo = loader.load();
            NovaDespesaController controller = loader.getController();

            controller.carregarCategorias(categoriaDespesaService.listarTodas());

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Nova Despesa");

            ButtonType btnSalvar = new ButtonType(
                    "Salvar Despesa",
                    ButtonBar.ButtonData.OK_DONE
            );

            dialog.getDialogPane().getButtonTypes().addAll(
                    btnSalvar,
                    ButtonType.CANCEL
            );

            dialog.getDialogPane().setContent(conteudo);

            dialog.showAndWait().ifPresent(res -> {
                if (res == btnSalvar) {
                    try {
                        BigDecimal valor = parseMoeda(controller.getValor());

                        CategoriaDespesa categoria = controller.getCategoria();

                        if (categoria == null) {
                            throw new IllegalArgumentException(
                                    "Selecione uma categoria."
                            );
                        }

                        financeiroService.cadastrarDespesaPorCategoria(
                                controller.getDescricao(),
                                valor,
                                categoria.getIdCategoria(),
                                controller.getSituacao(),
                                controller.getData(),
                                null,
                                controller.getObservacoes()
                        );

                        atualizarTela();

                    } catch (Exception e) {
                        mostrarErro("Erro ao salvar despesa", e.getMessage());
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            mostrarErro("Erro ao abrir modal", e.toString());
        }
    }


    @FXML
    public void abrirModalNovaReceita() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/NovaReceita.fxml")
            );

            Parent conteudo = loader.load();
            NovaReceitaController controller = loader.getController();

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Nova Receita");

            ButtonType btnSalvar = new ButtonType(
                    "Salvar Receita",
                    ButtonBar.ButtonData.OK_DONE
            );

            dialog.getDialogPane().getButtonTypes().addAll(
                    btnSalvar,
                    ButtonType.CANCEL
            );

            dialog.getDialogPane().setContent(conteudo);

            dialog.showAndWait().ifPresent(res -> {
                if (res == btnSalvar) {
                    try {
                        BigDecimal valor = parseMoeda(controller.getValor());

                        financeiroService.cadastrarReceita(
                                controller.getDescricao(),
                                valor,
                                controller.getCategoria(),
                                controller.getSituacao(),
                                controller.getData(),
                                null,
                                controller.getObservacoes()
                        );

                        atualizarTela();

                    } catch (Exception e) {
                        mostrarErro("Erro ao salvar receita", e.getMessage());
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            mostrarErro("Erro ao abrir modal", e.toString());
        }
    }

    @FXML
    public void exportarRelatorioCsv() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Salvar Relatório Financeiro");
        chooser.setInitialFileName("relatorio_financeiro.csv");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Arquivo CSV", "*.csv"));

        File arquivo = chooser.showSaveDialog(lblSaldoTotal.getScene().getWindow());
        if (arquivo == null) return;

        List<MovimentacaoFinanceira> movs = financeiroService.consultar(
                tipoFiltro, null, null, null, dataInicio, dataFim, txtBuscaMovimentacao.getText());

        try (PrintWriter writer = new PrintWriter(arquivo, StandardCharsets.UTF_8)) {
            writer.println("Data;Tipo;Categoria;Descricao;Situacao;Valor");
            for (MovimentacaoFinanceira m : movs) {
                writer.printf("%s;%s;%s;%s;%s;%s%n",
                        m.getDataMovimentacao() != null ? m.getDataMovimentacao().format(DATA_COMPLETA) : "",
                        m.getTipo(),
                        m.getCategoria() != null ? m.getCategoria() : "",
                        m.getDescricao().replace(";", ","),
                        m.getSituacao(),
                        m.getValor().toPlainString().replace(".", ","));
            }
            Alert ok = new Alert(Alert.AlertType.INFORMATION, "Relatório exportado com sucesso!");
            ok.setHeaderText(null);
            ok.showAndWait();
        } catch (Exception e) {
            mostrarErro("Erro ao exportar", e.getMessage());
        }
    }

    private BigDecimal parseMoeda(String texto) {
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException("Informe um valor válido.");
        }
        String limpo = texto.trim().replace("R$", "").replace(".", "").replace(",", ".").trim();
        return new BigDecimal(limpo);
    }

    private void mostrarErro(String titulo, String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private void confirmarPagamento(MovimentacaoFinanceira mov) {
        try {
            mov.setSituacao(SituacaoMovimentacaoFinanceira.PAGO);
            if (mov.getTipo() == TipoMovimentacaoFinanceira.RECEITA) {
                financeiroService.cadastrarReceita(mov);
            } else {
                financeiroService.cadastrarDespesa(mov);
            }
            atualizarTela();
        } catch (Exception e) {
            mostrarErro("Erro ao confirmar pagamento", e.getMessage());
        }
    }
}