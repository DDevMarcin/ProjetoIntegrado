package com.managementsystem.demo1.controller;

import com.managementsystem.demo1.model.Encomenda;
import com.managementsystem.demo1.model.Material;
import com.managementsystem.demo1.model.Produto;
import com.managementsystem.demo1.model.ProdutoMaterial;
import com.managementsystem.demo1.service.EncomendaService;
import com.managementsystem.demo1.service.ItemEncomendaInput;
import com.managementsystem.demo1.service.ItemMaterialInput;
import com.managementsystem.demo1.service.MaterialService;
import com.managementsystem.demo1.service.ProdutoService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
@Scope("prototype")
public class CadastrarEncomendaController {

    @FXML private TextField campoCliente;
    @FXML private TextField campoContato;

    // Seletor de tipo de item
    @FXML private RadioButton       rbTipoPersonalizado;
    @FXML private RadioButton       rbTipoPrePronto;
    @FXML private VBox              secaoPrePronto;
    @FXML private ComboBox<Produto> comboProdutoPrePronto;
    @FXML private TextField         campoQtdPrePronto;
    @FXML private Label             labelInfoPrePronto;
    @FXML private VBox              secaoPersonalizado;

    // Campos do item personalizado
    @FXML private TextField campoNome;
    @FXML private TextArea  campoDescricao;
    @FXML private TextField campoPrazo;
    @FXML private TextArea  campoObservacoes;

    @FXML private RadioButton rbPrecoMateriais;
    @FXML private RadioButton rbPrecoManual;
    @FXML private TextField   campoValorManual;
    @FXML private Label       labelValorItem;

    @FXML private VBox               secaoMateriais;
    @FXML private ComboBox<Material> comboMaterial;
    @FXML private TextField          campoQtdMaterial;
    @FXML private FlowPane           listaMateriais;

    @FXML private VBox listaItens;

    @FXML private Label previewCliente;
    @FXML private VBox  previewItens;
    @FXML private Label previewSubtotal;
    @FXML private Label previewTotal;
    @FXML private Label previewEntrega;

    @FXML private HBox  bannerSucesso;
    @FXML private Label bannerMensagem;
    @FXML private Label labelStatus;

    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // materiais do item que está sendo montado
    private final List<Material>   materiaisSelecionados = new ArrayList<>();
    private final List<BigDecimal> quantidades           = new ArrayList<>();

    // itens já adicionados à encomenda
    private final List<ItemEncomendaInput> itens        = new ArrayList<>();
    private final List<BigDecimal>         valoresItens = new ArrayList<>();

    private boolean salvando = false;

    private final EncomendaService encomendaService;
    private final MaterialService  materialService;
    private final ProdutoService   produtoService;

    public CadastrarEncomendaController(EncomendaService encomendaService,
                                        MaterialService materialService,
                                        ProdutoService produtoService) {
        this.encomendaService = encomendaService;
        this.materialService  = materialService;
        this.produtoService   = produtoService;
    }

    @FXML
    public void initialize() {
        configurarToggleTipoItem();
        configurarTogglePreco();
        carregarMateriais();
        carregarProdutosPreProntos();

        campoCliente.textProperty().addListener((o, a, v) ->
                previewCliente.setText(v.isBlank() ? "Cliente" : "Encomenda de " + v.trim()));
        campoValorManual.textProperty().addListener((o, a, v) -> atualizarValorItem());
        campoQtdPrePronto.textProperty().addListener((o, a, v) -> atualizarValorItem());
        comboProdutoPrePronto.valueProperty().addListener((o, a, produto) -> {
            atualizarInfoPrePronto(produto);
            atualizarValorItem();
        });
    }

    private void configurarToggleTipoItem() {
        ToggleGroup grupoTipo = new ToggleGroup();
        rbTipoPersonalizado.setToggleGroup(grupoTipo);
        rbTipoPrePronto.setToggleGroup(grupoTipo);

        rbTipoPersonalizado.setSelected(true);
        alternarVisibilidadeTipo(false);

        grupoTipo.selectedToggleProperty().addListener((obs, ant, novo) -> {
            boolean isPrePronto = novo == rbTipoPrePronto;
            alternarVisibilidadeTipo(isPrePronto);
            setStatus("", false);
            atualizarValorItem();
        });
    }

    private void alternarVisibilidadeTipo(boolean isPrePronto) {
        secaoPrePronto.setVisible(isPrePronto);
        secaoPrePronto.setManaged(isPrePronto);

        secaoPersonalizado.setVisible(!isPrePronto);
        secaoPersonalizado.setManaged(!isPrePronto);
    }

    private void configurarTogglePreco() {
        ToggleGroup grupo = new ToggleGroup();
        rbPrecoMateriais.setToggleGroup(grupo);
        rbPrecoManual.setToggleGroup(grupo);
        rbPrecoMateriais.setSelected(true);
        campoValorManual.setDisable(true);

        grupo.selectedToggleProperty().addListener((obs, ant, novo) -> {
            boolean manual = novo == rbPrecoManual;
            campoValorManual.setDisable(!manual);
            secaoMateriais.setDisable(manual);
            atualizarValorItem();
        });
    }

    private void carregarMateriais() {
        comboMaterial.setItems(FXCollections.observableArrayList(materialService.listarTodos()));
        comboMaterial.setConverter(new StringConverter<>() {
            @Override public String toString(Material m) {
                return m == null ? "" : m.getNome() + " (R$ " + m.getPrecoUnidade() + "/" + m.getUnidadeMedida() + ")";
            }
            @Override public Material fromString(String s) { return null; }
        });
    }

    private void carregarProdutosPreProntos() {
        comboProdutoPrePronto.setItems(FXCollections.observableArrayList(produtoService.listarEstoque()));
        comboProdutoPrePronto.setConverter(new StringConverter<>() {
            @Override public String toString(Produto p) {
                if (p == null) return "";
                int est = p.getQuantidadeEstoque() != null ? p.getQuantidadeEstoque() : 0;
                return p.getNome() + " — R$ " + String.format("%.2f", p.getValor()) + " (Estoque: " + est + ")";
            }
            @Override public Produto fromString(String s) { return null; }
        });
    }

    private void atualizarInfoPrePronto(Produto p) {
        if (p == null) {
            labelInfoPrePronto.setText("Selecione um produto para ver detalhes e estoque.");
            return;
        }
        int est = p.getQuantidadeEstoque() != null ? p.getQuantidadeEstoque() : 0;
        String desc = (p.getDescricao() != null && !p.getDescricao().isBlank()) ? p.getDescricao() : "Sem descrição";
        labelInfoPrePronto.setText("Código: " + p.getCodigo() + " | Estoque disponível: " + est + " un. | " + desc);
    }

    // ── Item em montagem ────────────────────────────────────────────────

    private BigDecimal calcularValorItemAtual() {
        if (rbTipoPrePronto.isSelected()) {
            Produto p = comboProdutoPrePronto.getValue();
            if (p == null || p.getValor() == null) return null;
            try {
                int qtd = Integer.parseInt(campoQtdPrePronto.getText().trim());
                if (qtd <= 0) return null;
                return p.getValor().multiply(BigDecimal.valueOf(qtd));
            } catch (NumberFormatException e) {
                return null;
            }
        }

        if (rbPrecoManual.isSelected()) {
            try {
                BigDecimal v = new BigDecimal(campoValorManual.getText().replace(",", "."));
                return v.signum() > 0 ? v : null;
            } catch (NumberFormatException e) {
                return null;
            }
        }

        BigDecimal soma = BigDecimal.ZERO;
        for (int i = 0; i < materiaisSelecionados.size(); i++)
            soma = soma.add(materiaisSelecionados.get(i).getPrecoUnidade().multiply(quantidades.get(i)));
        return soma.signum() == 0 ? null : soma;
    }

    private void atualizarValorItem() {
        BigDecimal v = calcularValorItemAtual();
        labelValorItem.setText(v == null ? "Valor do item: R$ —"
                : "Valor do item: R$ " + String.format("%.2f", v));
    }

    @FXML
    public void adicionarMaterial() {
        Material material = comboMaterial.getValue();
        if (material == null) { setStatus("Selecione um material.", true); return; }
        BigDecimal qtd;
        try {
            qtd = new BigDecimal(campoQtdMaterial.getText().replace(",", "."));
            if (qtd.signum() <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            setStatus("Informe uma quantidade válida.", true); return;
        }
        materiaisSelecionados.add(material);
        quantidades.add(qtd);
        adicionarTagMaterial(material.getNome() + " × " + qtd.stripTrailingZeros().toPlainString(), material);
        comboMaterial.setValue(null);
        campoQtdMaterial.clear();
        setStatus("", false);
        atualizarValorItem();
    }

    private void adicionarTagMaterial(String texto, Material material) {
        HBox tag = new HBox(4);
        tag.getStyleClass().add("material-tag-removivel");
        Label label = new Label(texto);
        label.setStyle("-fx-text-fill: #8F031E; -fx-font-size: 15px; -fx-font-weight: bold;");
        Button remover = new Button("×");
        remover.setStyle("-fx-background-color: transparent; -fx-text-fill: #8F031E; " +
                "-fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 0 2 0 4;");
        remover.setOnAction(e -> {
            int idx = materiaisSelecionados.lastIndexOf(material);
            if (idx >= 0) { materiaisSelecionados.remove(idx); quantidades.remove(idx); }
            listaMateriais.getChildren().remove(tag);
            atualizarValorItem();
        });
        tag.getChildren().addAll(label, remover);
        listaMateriais.getChildren().add(tag);
    }

    @FXML
    public void adicionarItem() {
        if (rbTipoPrePronto.isSelected()) {
            adicionarItemPrePronto();
        } else {
            adicionarItemPersonalizado();
        }
    }

    private void adicionarItemPrePronto() {
        Produto produto = comboProdutoPrePronto.getValue();
        if (produto == null) {
            setStatus("Selecione um produto pré-pronto.", true);
            return;
        }

        int qtd;
        try {
            qtd = Integer.parseInt(campoQtdPrePronto.getText().trim());
            if (qtd <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            setStatus("Informe uma quantidade válida (número inteiro positivo).", true);
            return;
        }

        int estoqueDisponivel = produto.getQuantidadeEstoque() != null ? produto.getQuantidadeEstoque() : 0;
        if (qtd > estoqueDisponivel) {
            setStatus("Quantidade maior que o estoque disponível (" + estoqueDisponivel + " un.).", true);
            return;
        }

        BigDecimal valorTotal = produto.getValor().multiply(BigDecimal.valueOf(qtd));
        String nomeItem = qtd > 1 ? produto.getNome() + " (" + qtd + "x)" : produto.getNome();

        itens.add(new ItemEncomendaInput(
                nomeItem,
                produto.getDescricao(),
                produto.getPrazoProducaoDias(),
                "Produto pré-pronto (Cód: " + produto.getCodigo() + ")",
                true,
                valorTotal,
                List.of(),
                produto.getIdProduto(),
                qtd
        ));
        valoresItens.add(valorTotal);

        limparCamposItem();
        setStatus("", false);
        renderizarItens();
    }

    private void adicionarItemPersonalizado() {
        if (campoNome.getText().isBlank()) { setStatus("Informe o nome do item.", true); return; }

        Integer prazo = null;
        if (!campoPrazo.getText().isBlank()) {
            try {
                prazo = Integer.parseInt(campoPrazo.getText().trim());
                if (prazo < 0) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                setStatus("Prazo deve ser um número positivo.", true); return;
            }
        }

        boolean manual = rbPrecoManual.isSelected();
        BigDecimal valor = calcularValorItemAtual();
        if (valor == null) {
            setStatus(manual ? "Informe um valor válido para o item."
                    : "Adicione ao menos um material ao item, ou escolha 'Informar valor'.", true);
            return;
        }

        List<ItemMaterialInput> materiais = new ArrayList<>();
        for (int i = 0; i < materiaisSelecionados.size(); i++)
            materiais.add(new ItemMaterialInput(materiaisSelecionados.get(i).getIdMaterial(), quantidades.get(i)));

        itens.add(new ItemEncomendaInput(
                campoNome.getText().trim(),
                vazioParaNull(campoDescricao.getText()),
                prazo,
                vazioParaNull(campoObservacoes.getText()),
                manual,
                manual ? valor : null,
                materiais));
        valoresItens.add(valor);

        limparCamposItem();
        setStatus("", false);
        renderizarItens();
    }

    private void limparCamposItem() {
        comboProdutoPrePronto.setValue(null);
        campoQtdPrePronto.setText("1");
        labelInfoPrePronto.setText("Selecione um produto para ver detalhes e estoque.");

        campoNome.clear(); campoDescricao.clear(); campoPrazo.clear();
        campoObservacoes.clear(); campoValorManual.clear();
        comboMaterial.setValue(null); campoQtdMaterial.clear();
        materiaisSelecionados.clear(); quantidades.clear();
        listaMateriais.getChildren().clear();

        rbPrecoMateriais.setSelected(true);
        campoValorManual.setDisable(true);
        secaoMateriais.setDisable(false);
        atualizarValorItem();
    }

    // ── Itens da encomenda ──────────────────────────────────────────────

    private void renderizarItens() {
        listaItens.getChildren().clear();
        previewItens.getChildren().clear();

        for (int i = 0; i < itens.size(); i++) {
            final int idx = i;
            String texto = itens.get(i).nome() + " — R$ " + String.format("%.2f", valoresItens.get(i));

            HBox card = new HBox(8);
            card.getStyleClass().add("material-tag-removivel");
            Label label = new Label(texto);
            label.setStyle("-fx-text-fill: #8F031E; -fx-font-size: 15px; -fx-font-weight: bold;");
            Button remover = new Button("×");
            remover.setStyle("-fx-background-color: transparent; -fx-text-fill: #8F031E; " +
                    "-fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 0 2 0 4;");
            remover.setOnAction(e -> { itens.remove(idx); valoresItens.remove(idx); renderizarItens(); });
            card.getChildren().addAll(label, remover);
            listaItens.getChildren().add(card);

            Label linha = new Label("• " + texto);
            linha.setWrapText(true);
            linha.getStyleClass().add("detalhe-secao-texto");
            previewItens.getChildren().add(linha);
        }
        atualizarResumo();
    }

    private void atualizarResumo() {
        if (itens.isEmpty()) {
            previewSubtotal.setText("R$ —");
            previewTotal.setText("R$ —");
            previewEntrega.setText("—");
            return;
        }
        BigDecimal subtotal = valoresItens.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        previewSubtotal.setText("R$ " + String.format("%.2f", subtotal));
        previewTotal.setText("R$ " + String.format("%.2f", encomendaService.aplicarMargemLucro(subtotal)));

        Integer maiorPrazo = itens.stream()
                .map(ItemEncomendaInput::prazoProducaoDias)
                .filter(Objects::nonNull)
                .max(Integer::compare)
                .orElse(null);
        previewEntrega.setText(maiorPrazo == null ? "—"
                : LocalDate.now().plusDays(maiorPrazo).format(FORMATO_DATA));
    }

    // ── Salvar ──────────────────────────────────────────────────────────

    @FXML
    public void salvar() {
        if (salvando) return;
        if (campoCliente.getText().isBlank()) { setStatus("Informe o nome do cliente.", true); return; }
        if ((rbTipoPersonalizado.isSelected() && !campoNome.getText().isBlank())
                || (rbTipoPrePronto.isSelected() && comboProdutoPrePronto.getValue() != null)) {
            setStatus("Há um item selecionado que ainda não foi adicionado. Clique em \"+ Adicionar item à encomenda\".", true);
            return;
        }
        if (itens.isEmpty()) { setStatus("Adicione ao menos um item à encomenda.", true); return; }

        salvando = true;
        try {
            Encomenda salva = encomendaService.cadastrar(
                    campoCliente.getText().trim(),
                    vazioParaNull(campoContato.getText()),
                    LocalDate.now(),
                    null,
                    new ArrayList<>(itens));
            setStatus("", false);
            mostrarBannerSucesso("Encomenda de " + salva.getNomeCliente() + " cadastrada com sucesso!");
            limpar();
        } catch (IllegalArgumentException e) {
            setStatus(e.getMessage(), true);
        } catch (Exception e) {
            setStatus("Erro inesperado: " + e.getMessage(), true);
        } finally {
            salvando = false;
        }
    }

    @FXML
    public void voltar() {
        javafx.scene.Scene scene = campoCliente.getScene();
        if (scene == null) return;
        javafx.scene.Node sp = scene.lookup("#contentArea");
        if (sp instanceof javafx.scene.layout.StackPane stackPane
                && stackPane.getUserData() instanceof MainLayoutController main)
            main.navEncomendas();
    }

    @FXML
    public void limpar() {
        campoCliente.clear();
        campoContato.clear();
        rbTipoPersonalizado.setSelected(true);
        limparCamposItem();
        itens.clear();
        valoresItens.clear();
        renderizarItens();
        labelStatus.setText("");
    }

    private void mostrarBannerSucesso(String mensagem) {
        bannerMensagem.setText(mensagem);
        bannerSucesso.setVisible(true);
        bannerSucesso.setManaged(true);
        new Thread(() -> {
            try { Thread.sleep(4000); } catch (InterruptedException ignored) {}
            javafx.application.Platform.runLater(() -> {
                bannerSucesso.setVisible(false);
                bannerSucesso.setManaged(false);
            });
        }).start();
    }

    private void setStatus(String msg, boolean erro) {
        labelStatus.setText(msg);
        labelStatus.setStyle(erro ? "-fx-text-fill: #D31946; -fx-font-size: 15px;"
                : "-fx-text-fill: #2e7d32; -fx-font-size: 15px;");
    }

    private String vazioParaNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }
}