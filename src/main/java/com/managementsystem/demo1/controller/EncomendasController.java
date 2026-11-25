package com.managementsystem.demo1.controller;

import com.managementsystem.demo1.model.Encomenda;
import com.managementsystem.demo1.model.ItemEncomenda;
import com.managementsystem.demo1.model.SituacaoEncomenda;
import com.managementsystem.demo1.service.EncomendaService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Scope("prototype")
public class EncomendasController {

    @FXML private TextField                        campoBusca;
    @FXML private TableView<Encomenda>             tabelaEncomendas;
    @FXML private TableColumn<Encomenda, String>   colId;
    @FXML private TableColumn<Encomenda, String>   colCliente;
    @FXML private TableColumn<Encomenda, String>   colItens;
    @FXML private TableColumn<Encomenda, String>   colEntrega;
    @FXML private TableColumn<Encomenda, String>   colSituacao;
    @FXML private TableColumn<Encomenda, String>   colValor;
    @FXML private Label                            labelTotal;
    @FXML private Label                            labelPagina;
    @FXML private Button                           btnAnterior;
    @FXML private Button                           btnProximo;

    @FXML private ScrollPane painelDetalhes;
    @FXML private Label      detCliente;
    @FXML private Label      detSituacao;
    @FXML private Label      detContato;
    @FXML private Label      detSubtotal;
    @FXML private Label      detValorTotal;
    @FXML private Label      detDataPedido;
    @FXML private Label      detDataEntrega;
    @FXML private VBox       painelItens;
    @FXML private Label      detObservacoes;
    @FXML private Button     btnAvancarSituacao;
    @FXML private Button     btnCancelarEncomenda;

    private static final int ITENS_POR_PAGINA = 14;
    private static final DateTimeFormatter DATA_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private int paginaAtual = 1;
    private List<Encomenda> todos     = List.of();
    private List<Encomenda> filtrados = List.of();

    private final EncomendaService encomendaService;

    public EncomendasController(EncomendaService encomendaService) {
        this.encomendaService = encomendaService;
    }

    @FXML
    public void initialize() {
        tabelaEncomendas.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        configurarColunas();
        configurarSelecao();
        carregar();
        campoBusca.textProperty().addListener((obs, ant, novo) -> filtrar(novo));
    }

    private void configurarColunas() {
        colId.setCellValueFactory(c -> {
            Long id = c.getValue().getIdEncomenda();
            return new SimpleStringProperty(id != null ? String.format("%03d", id) : "—");
        });

        colCliente.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNomeCliente()));

        colItens.setCellValueFactory(c -> {
            List<ItemEncomenda> itens = c.getValue().getItens();
            if (itens == null || itens.isEmpty()) return new SimpleStringProperty("—");
            String resumo = itens.stream()
                    .map(ItemEncomenda::getNome)
                    .collect(Collectors.joining(", "));
            return new SimpleStringProperty(resumo);
        });

        colEntrega.setCellValueFactory(c -> {
            var data = c.getValue().getDataEntregaPrevista();
            return new SimpleStringProperty(data != null ? data.format(DATA_FMT) : "Sem prazo");
        });

        colSituacao.setCellValueFactory(c ->
                new SimpleStringProperty(formatarSituacao(c.getValue().getSituacao())));

        colValor.setCellValueFactory(c -> {
            BigDecimal v = c.getValue().getValorTotal();
            return new SimpleStringProperty(v != null ? "R$ " + String.format("%.2f", v) : "—");
        });

        colId.setStyle("-fx-alignment: CENTER; -fx-text-fill: #888888; -fx-font-size: 12px;");
        colEntrega.setStyle("-fx-alignment: CENTER;");
        colSituacao.setStyle("-fx-alignment: CENTER;");
        colValor.setStyle("-fx-alignment: CENTER;");
    }

    private void configurarSelecao() {
        tabelaEncomendas.getSelectionModel().selectedItemProperty()
                .addListener((obs, ant, sel) -> { if (sel != null) mostrarDetalhes(sel); });
    }

    private void carregar() {
        todos = encomendaService.listarTodas().stream()
                .filter(e -> e.getSituacao() != SituacaoEncomenda.ENTREGUE)
                .toList();
        filtrar(campoBusca.getText());
    }

    private void filtrar(String termo) {
        paginaAtual = 1;
        if (termo == null || termo.isBlank()) {
            filtrados = todos;
        } else {
            String lower = termo.toLowerCase().trim().replace("#", "");
            filtrados = todos.stream()
                    .filter(e -> {
                        String idStr = e.getIdEncomenda() != null ? String.valueOf(e.getIdEncomenda()) : "";
                        String idFormatado = e.getIdEncomenda() != null ? String.format("%03d", e.getIdEncomenda()) : "";
                        boolean bateId = idStr.equals(lower) || idFormatado.contains(lower);
                        boolean bateCliente = e.getNomeCliente() != null && e.getNomeCliente().toLowerCase().contains(lower);
                        boolean bateItem = e.getItens() != null && e.getItens().stream()
                                .anyMatch(i -> i.getNome() != null && i.getNome().toLowerCase().contains(lower));
                        return bateId || bateCliente || bateItem;
                    })
                    .toList();
        }
        exibirPagina();
    }

    private void exibirPagina() {
        int total        = filtrados.size();
        int totalPaginas = Math.max(1, (int) Math.ceil((double) total / ITENS_POR_PAGINA));
        paginaAtual = Math.max(1, Math.min(paginaAtual, totalPaginas));
        int inicio = (paginaAtual - 1) * ITENS_POR_PAGINA;
        int fim    = Math.min(inicio + ITENS_POR_PAGINA, total);
        tabelaEncomendas.setItems(FXCollections.observableArrayList(filtrados.subList(inicio, fim)));
        labelTotal.setText("Total: " + total);
        labelPagina.setText("Página " + paginaAtual);
        btnAnterior.setDisable(paginaAtual <= 1);
        btnProximo.setDisable(paginaAtual >= totalPaginas);
    }

    private void mostrarDetalhes(Encomenda e) {
        detCliente.setText(e.getNomeCliente());
        detSituacao.setText(formatarSituacao(e.getSituacao()));
        detContato.setText(Optional.ofNullable(e.getContatoCliente())
                .filter(c -> !c.isBlank())
                .map(c -> "Contato: " + c)
                .orElse("Sem contato informado"));

        detSubtotal.setText(e.getSubtotal() != null ? "R$ " + String.format("%.2f", e.getSubtotal()) : "—");
        detValorTotal.setText(e.getValorTotal() != null ? "R$ " + String.format("%.2f", e.getValorTotal()) : "—");
        detDataPedido.setText(e.getDataPedido() != null ? e.getDataPedido().format(DATA_FMT) : "—");
        detDataEntrega.setText(e.getDataEntregaPrevista() != null ? e.getDataEntregaPrevista().format(DATA_FMT) : "Não definida");
        detObservacoes.setText(Optional.ofNullable(e.getObservacoes())
                .filter(o -> !o.isBlank())
                .orElse("Sem observações."));

        painelItens.getChildren().clear();
        if (e.getItens() != null && !e.getItens().isEmpty()) {
            for (ItemEncomenda item : e.getItens()) {
                VBox cardItem = new VBox(2);
                cardItem.setStyle("-fx-background-color: #FFF8FB; -fx-padding: 8 10; " +
                        "-fx-background-radius: 8; -fx-border-color: #f0d0da; -fx-border-radius: 8;");
                Label lblNome = new Label(item.getNome() + " — R$ " + String.format("%.2f", item.getValor()));
                lblNome.setStyle("-fx-font-weight: bold; -fx-text-fill: #1a1a2e; -fx-font-size: 13px;");
                cardItem.getChildren().add(lblNome);

                if (item.getDescricao() != null && !item.getDescricao().isBlank()) {
                    Label lblDesc = new Label(item.getDescricao());
                    lblDesc.setWrapText(true);
                    lblDesc.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");
                    cardItem.getChildren().add(lblDesc);
                }
                painelItens.getChildren().add(cardItem);
            }
        }

        SituacaoEncomenda prox = proximaSituacao(e.getSituacao());
        if (prox != null) {
            btnAvancarSituacao.setText("Marcar como " + formatarSituacao(prox));
            btnAvancarSituacao.setVisible(true);
            btnAvancarSituacao.setManaged(true);
        } else {
            btnAvancarSituacao.setVisible(false);
            btnAvancarSituacao.setManaged(false);
        }

        boolean podeCancelar = e.getSituacao() != SituacaoEncomenda.ENTREGUE
                && e.getSituacao() != SituacaoEncomenda.CANCELADA;
        btnCancelarEncomenda.setVisible(podeCancelar);
        btnCancelarEncomenda.setManaged(podeCancelar);

        painelDetalhes.setVisible(true);
        painelDetalhes.setManaged(true);
    }

    private SituacaoEncomenda proximaSituacao(SituacaoEncomenda atual) {
        return switch (atual) {
            case PENDENTE    -> SituacaoEncomenda.EM_PRODUCAO;
            case EM_PRODUCAO -> SituacaoEncomenda.PRONTA;
            case PRONTA      -> SituacaoEncomenda.ENTREGUE;
            case ENTREGUE, CANCELADA -> null;
        };
    }

    private String formatarSituacao(SituacaoEncomenda s) {
        if (s == null) return "—";
        return switch (s) {
            case PENDENTE    -> "Pendente";
            case EM_PRODUCAO -> "Em Produção";
            case PRONTA      -> "Pronta";
            case ENTREGUE    -> "Entregue";
            case CANCELADA   -> "Cancelada";
        };
    }

    @FXML
    public void avancarSituacao() {
        Encomenda sel = tabelaEncomendas.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        SituacaoEncomenda prox = proximaSituacao(sel.getSituacao());
        if (prox == null) return;

        Encomenda atualizada = encomendaService.alterarSituacao(sel.getIdEncomenda(), prox);
        carregar();
        if (atualizada.getSituacao() == SituacaoEncomenda.ENTREGUE) {
            painelDetalhes.setVisible(false);
            painelDetalhes.setManaged(false);
        } else {
            mostrarDetalhes(atualizada);
        }
    }

    @FXML
    public void cancelarEncomenda() {
        Encomenda sel = tabelaEncomendas.getSelectionModel().getSelectedItem();
        if (sel == null) return;

        Dialog<ButtonType> dialog = criarDialogConfirmacao(
                "Cancelar encomenda de \"" + sel.getNomeCliente() + "\"?",
                "A situação será alterada para Cancelada.",
                "Sim, Cancelar");
        dialog.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                Encomenda atualizada = encomendaService.alterarSituacao(
                        sel.getIdEncomenda(), SituacaoEncomenda.CANCELADA);
                carregar();
                mostrarDetalhes(atualizada);
            }
        });
    }

    @FXML public void paginaAnterior() { if (paginaAtual > 1) { paginaAtual--; exibirPagina(); } }
    @FXML public void proximaPagina()  { paginaAtual++; exibirPagina(); }

    @FXML
    public void abrirFormCadastro() {
        javafx.scene.Scene scene = tabelaEncomendas.getScene();
        if (scene == null) return;
        javafx.scene.Node sp = scene.lookup("#contentArea");
        if (sp instanceof javafx.scene.layout.StackPane stackPane
                && stackPane.getUserData() instanceof MainLayoutController main)
            main.navCadastrarEncomenda();
    }

    @FXML
    public void abrirHistorico() {
        javafx.scene.Scene scene = tabelaEncomendas.getScene();
        if (scene == null) return;
        javafx.scene.Node sp = scene.lookup("#contentArea");
        if (sp instanceof javafx.scene.layout.StackPane stackPane
                && stackPane.getUserData() instanceof MainLayoutController main)
            main.navHistoricoEncomendas();
    }

    @FXML
    public void deletarEncomenda() {
        Encomenda selecionado = tabelaEncomendas.getSelectionModel().getSelectedItem();
        if (selecionado == null) return;

        Dialog<ButtonType> dialog = criarDialogConfirmacao(
                "Deletar encomenda de \"" + selecionado.getNomeCliente() + "\"?",
                "Esta ação não pode ser desfeita.",
                "Deletar");
        dialog.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                encomendaService.excluir(selecionado.getIdEncomenda());
                painelDetalhes.setVisible(false);
                painelDetalhes.setManaged(false);
                carregar();
            }
        });
    }

    private Dialog<ButtonType> criarDialogConfirmacao(String tituloTexto, String msgTexto, String textoConfirmar) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Confirmação");
        dialog.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        VBox conteudo = new VBox(16);
        conteudo.setPadding(new Insets(24, 28, 24, 28));
        conteudo.setStyle("-fx-background-color: #FFF0F5; -fx-background-radius: 12;");
        Label titulo = new Label(tituloTexto);
        titulo.setFont(Font.font("System", FontWeight.BOLD, 17));
        titulo.setTextFill(Color.web("#D31946"));
        titulo.setWrapText(true);
        Label msg = new Label(msgTexto);
        msg.setFont(Font.font("System", 15));
        msg.setTextFill(Color.web("#555"));
        HBox botoes = new HBox(12);
        botoes.setAlignment(Pos.CENTER_RIGHT);
        Button btnCancelar = new Button("Voltar");
        btnCancelar.setStyle("-fx-background-color: white; -fx-background-radius: 20; " +
                "-fx-border-color: #ccc; -fx-border-radius: 20; -fx-border-width: 1.5; " +
                "-fx-text-fill: #555; -fx-font-size: 14px; -fx-font-weight: bold; " +
                "-fx-padding: 7 18 7 18; -fx-cursor: hand;");
        Button btnConfirmar = new Button(textoConfirmar);
        btnConfirmar.setStyle("-fx-background-color: #D31946; -fx-background-radius: 20; " +
                "-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; " +
                "-fx-padding: 7 22 7 22; -fx-cursor: hand;");
        btnCancelar.setOnAction(e -> { dialog.setResult(ButtonType.CANCEL); dialog.close(); });
        btnConfirmar.setOnAction(e ->  { dialog.setResult(ButtonType.OK);     dialog.close(); });
        botoes.getChildren().addAll(btnCancelar, btnConfirmar);
        conteudo.getChildren().addAll(titulo, msg, botoes);
        dialog.getDialogPane().setContent(conteudo);
        dialog.getDialogPane().getButtonTypes().clear();
        dialog.getDialogPane().setStyle("-fx-background-color: transparent;");
        return dialog;
    }
}