package com.managementsystem.demo1.controller;

import com.managementsystem.demo1.model.Produto;
import com.managementsystem.demo1.model.ProdutoMaterial;
import com.managementsystem.demo1.model.TipoProduto;
import com.managementsystem.demo1.service.ProdutoService;
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
import java.util.List;
import java.util.Optional;

@Component
@Scope("prototype")
public class EncomendasController {

    @FXML private TextField                      campoBusca;
    @FXML private TableView<Produto>             tabelaEncomendas;
    @FXML private TableColumn<Produto, String>   colNome;
    @FXML private TableColumn<Produto, String>   colCodigo;
    @FXML private TableColumn<Produto, String>   colPrazo;
    @FXML private TableColumn<Produto, String>   colValor;
    @FXML private Label                          labelTotal;
    @FXML private Label                          labelPagina;
    @FXML private Button                         btnAnterior;
    @FXML private Button                         btnProximo;

    @FXML private ScrollPane painelDetalhes;
    @FXML private Label      detNome;
    @FXML private Label      detCodigo;
    @FXML private Label      detValor;
    @FXML private Label      detPrazo;
    @FXML private Label      detDescricao;
    @FXML private Label      detObservacoes;
    @FXML private VBox       secaoMateriais;
    @FXML private FlowPane   painelMateriais;

    private static final int ITENS_POR_PAGINA = 14;
    private int paginaAtual = 1;
    private List<Produto> todos     = List.of();
    private List<Produto> filtrados = List.of();

    private final ProdutoService produtoService;

    public EncomendasController(ProdutoService produtoService) {
        this.produtoService = produtoService;
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
        colNome.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNome()));
        colCodigo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCodigo()));
        colPrazo.setCellValueFactory(c -> {
            Integer prazo = c.getValue().getPrazoProducaoDias();
            return new SimpleStringProperty(prazo != null ? prazo + " dias" : "—");
        });
        colValor.setCellValueFactory(c -> {
            BigDecimal v = c.getValue().getValor();
            return new SimpleStringProperty(v != null ? "R$" + String.format("%.2f", v) : "—");
        });
        colCodigo.setStyle("-fx-alignment: CENTER;");
        colPrazo.setStyle("-fx-alignment: CENTER;");
        colValor.setStyle("-fx-alignment: CENTER;");
    }

    private void configurarSelecao() {
        tabelaEncomendas.getSelectionModel().selectedItemProperty()
                .addListener((obs, ant, sel) -> { if (sel != null) mostrarDetalhes(sel); });
    }

    private void carregar() {
        todos = produtoService.listarTodos().stream()
                .filter(p -> p.getTipo() == TipoProduto.PERSONALIZADO)
                .toList();
        filtrar(campoBusca.getText());
    }

    private void filtrar(String termo) {
        paginaAtual = 1;
        if (termo == null || termo.isBlank()) {
            filtrados = todos;
        } else {
            String lower = termo.toLowerCase();
            filtrados = todos.stream()
                    .filter(p -> p.getNome().toLowerCase().contains(lower)
                              || p.getCodigo().toLowerCase().contains(lower))
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

    private void mostrarDetalhes(Produto p) {
        detNome.setText(p.getNome());
        detCodigo.setText(p.getCodigo());
        detValor.setText(p.getValor() != null ? "R$ " + String.format("%.2f", p.getValor()) : "—");
        detPrazo.setText(p.getPrazoProducaoDias() != null ? p.getPrazoProducaoDias() + " dias" : "Não informado");
        detDescricao.setText(Optional.ofNullable(p.getDescricao()).filter(d -> !d.isBlank()).orElse("Sem descrição."));
        detObservacoes.setText(Optional.ofNullable(p.getObservacoes()).filter(o -> !o.isBlank()).orElse("Sem observações."));

        painelMateriais.getChildren().clear();
        List<ProdutoMaterial> mats = p.getMateriaisUtilizados();
        if (mats != null && !mats.isEmpty()) {
            for (ProdutoMaterial pm : mats) {
                Label tag = new Label(pm.getQuantidadeUtilizada().stripTrailingZeros().toPlainString()
                        + "x " + pm.getMaterial().getNome());
                tag.getStyleClass().add("material-tag");
                painelMateriais.getChildren().add(tag);
            }
        } else {
            Label sem = new Label("Nenhum material registrado.");
            sem.setStyle("-fx-text-fill: #aaa; -fx-font-size: 14px;");
            painelMateriais.getChildren().add(sem);
        }

        painelDetalhes.setVisible(true);
        painelDetalhes.setManaged(true);
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
    public void deletarEncomenda() {
        Produto selecionado = tabelaEncomendas.getSelectionModel().getSelectedItem();
        if (selecionado == null) return;

        Dialog<ButtonType> dialog = criarDialogConfirmacao(selecionado.getNome());
        dialog.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                produtoService.excluir(selecionado.getIdProduto());
                painelDetalhes.setVisible(false);
                painelDetalhes.setManaged(false);
                carregar();
            }
        });
    }

    @FXML
    public void editarEncomenda() {
        Produto selecionado = tabelaEncomendas.getSelectionModel().getSelectedItem();
        if (selecionado == null) return;
        javafx.scene.Scene scene = tabelaEncomendas.getScene();
        if (scene == null) return;
        javafx.scene.Node sp = scene.lookup("#contentArea");
        if (sp instanceof javafx.scene.layout.StackPane stackPane
                && stackPane.getUserData() instanceof MainLayoutController main)
            main.navEditarProduto(selecionado);
    }

    private Dialog<ButtonType> criarDialogConfirmacao(String nome) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Confirmar exclusão");
        dialog.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        VBox conteudo = new VBox(16);
        conteudo.setPadding(new Insets(24, 28, 24, 28));
        conteudo.setStyle("-fx-background-color: #FFF0F5; -fx-background-radius: 12;");
        Label titulo = new Label("Deletar \"" + nome + "\"?");
        titulo.setFont(Font.font("System", FontWeight.BOLD, 17));
        titulo.setTextFill(Color.web("#D31946"));
        titulo.setWrapText(true);
        Label msg = new Label("Esta ação não pode ser desfeita.");
        msg.setFont(Font.font("System", 15));
        msg.setTextFill(Color.web("#555"));
        HBox botoes = new HBox(12);
        botoes.setAlignment(Pos.CENTER_RIGHT);
        Button btnCancelar = new Button("Cancelar");
        btnCancelar.setStyle("-fx-background-color: white; -fx-background-radius: 20; " +
                "-fx-border-color: #ccc; -fx-border-radius: 20; -fx-border-width: 1.5; " +
                "-fx-text-fill: #555; -fx-font-size: 14px; -fx-font-weight: bold; " +
                "-fx-padding: 7 18 7 18; -fx-cursor: hand;");
        Button btnDeletar = new Button("Deletar");
        btnDeletar.setStyle("-fx-background-color: #D31946; -fx-background-radius: 20; " +
                "-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; " +
                "-fx-padding: 7 22 7 22; -fx-cursor: hand;");
        btnCancelar.setOnAction(e -> { dialog.setResult(ButtonType.CANCEL); dialog.close(); });
        btnDeletar.setOnAction(e ->  { dialog.setResult(ButtonType.OK);     dialog.close(); });
        botoes.getChildren().addAll(btnCancelar, btnDeletar);
        conteudo.getChildren().addAll(titulo, msg, botoes);
        dialog.getDialogPane().setContent(conteudo);
        dialog.getDialogPane().getButtonTypes().clear();
        dialog.getDialogPane().setStyle("-fx-background-color: transparent;");
        return dialog;
    }
}
