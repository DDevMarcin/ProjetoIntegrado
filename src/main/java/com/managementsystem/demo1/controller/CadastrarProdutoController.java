package com.managementsystem.demo1.controller;

import com.managementsystem.demo1.model.Material;
import com.managementsystem.demo1.model.Produto;
import com.managementsystem.demo1.model.TipoProduto;
import com.managementsystem.demo1.service.ItemMaterialInput;
import com.managementsystem.demo1.service.MaterialService;
import com.managementsystem.demo1.service.ProdutoService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
@Scope("prototype")
public class CadastrarProdutoController {

    @FXML
    private TextField campoCodigo;
    @FXML
    private TextField campoNome;
    @FXML
    private TextArea campoDescricao;
    @FXML
    private ComboBox<TipoProduto> comboTipo;
    @FXML
    private TextField campoQuantidade;
    @FXML
    private TextField campoPrazoProducao;
    @FXML
    private TextArea campoObservacoes;
    @FXML
    private CheckBox checkValorManual;
    @FXML
    private TextField campoValorManual;
    @FXML
    private ComboBox<Material> comboMaterial;
    @FXML
    private TextField campoQuantidadeMaterial;
    @FXML
    private ListView<String> listaMateriaisAdicionados;
    @FXML
    private Label labelStatus;
    @FXML
    private ListView<String> listaProdutos;

    private final ProdutoService produtoService;
    private final MaterialService materialService;

    private final List<Material> materiaisEscolhidos = new ArrayList<>();
    private final List<BigDecimal> quantidadesEscolhidas = new ArrayList<>();

    public CadastrarProdutoController(
            ProdutoService produtoService,
            MaterialService materialService
    ) {
        this.produtoService = produtoService;
        this.materialService = materialService;
    }

    @FXML
    public void initialize() {
        carregarTipos();
        carregarMateriaisDisponiveis();
        atualizarListaProdutos();

        campoPrazoProducao.setDisable(true);
        campoObservacoes.setDisable(true);
    }

    private void carregarTipos() {
        comboTipo.setItems(
                FXCollections.observableArrayList(TipoProduto.values())
        );

        comboTipo.getSelectionModel().select(TipoProduto.PRE_PRONTO);

        comboTipo.setOnAction(event -> atualizarCamposPorTipo());
    }

    private void atualizarCamposPorTipo() {

        TipoProduto tipo = comboTipo.getValue();

        if (tipo == TipoProduto.PRE_PRONTO) {

            campoQuantidade.setDisable(false);

            campoPrazoProducao.setDisable(true);
            campoObservacoes.setDisable(true);

            campoPrazoProducao.clear();
            campoObservacoes.clear();

        } else if (tipo == TipoProduto.PERSONALIZADO) {

            campoQuantidade.setDisable(true);

            campoPrazoProducao.setDisable(false);
            campoObservacoes.setDisable(false);

            campoQuantidade.clear();
        }
    }

    private void carregarMateriaisDisponiveis() {

        List<Material> materiais = materialService.listarTodos();

        comboMaterial.setItems(
                FXCollections.observableArrayList(materiais)
        );

        comboMaterial.setConverter(new StringConverter<>() {

            @Override
            public String toString(Material material) {
                if (material == null) {
                    return "";
                }

                return material.getNome()
                        + " (R$ "
                        + material.getPrecoUnidade()
                        + " / "
                        + material.getUnidadeMedida()
                        + ")";
            }

            @Override
            public Material fromString(String string) {
                return null;
            }
        });
    }

    @FXML
    public void adicionarMaterial() {

        Material material = comboMaterial.getValue();

        if (material == null) {
            labelStatus.setText(
                    "Selecione um material antes de adicionar."
            );
            return;
        }

        BigDecimal quantidade;

        try {

            quantidade = new BigDecimal(
                    campoQuantidadeMaterial
                            .getText()
                            .replace(",", ".")
            );

            if (quantidade.signum() <= 0) {
                throw new NumberFormatException();
            }

        } catch (NumberFormatException e) {

            labelStatus.setText(
                    "Quantidade do material inválida."
            );

            return;
        }

        materiaisEscolhidos.add(material);
        quantidadesEscolhidas.add(quantidade);

        listaMateriaisAdicionados.getItems().add(
                material.getNome()
                        + " — "
                        + quantidade
                        + " "
                        + material.getUnidadeMedida()
        );

        campoQuantidadeMaterial.clear();
        comboMaterial.setValue(null);

        labelStatus.setText("");
    }

    @FXML
    public void salvar() {

        try {

            String codigo = campoCodigo.getText();
            String nome = campoNome.getText();
            String descricao = campoDescricao.getText();

            TipoProduto tipo = comboTipo.getValue();

            if (tipo == null) {
                labelStatus.setText(
                        "Selecione o tipo do produto."
                );
                return;
            }

            /*
             * Quantidade em estoque:
             * usada somente para PRE_PRONTO.
             */
            Integer quantidadeEstoque = null;

            if (tipo == TipoProduto.PRE_PRONTO) {

                if (campoQuantidade.getText().isBlank()) {
                    labelStatus.setText(
                            "Informe a quantidade em estoque."
                    );
                    return;
                }

                quantidadeEstoque = Integer.parseInt(
                        campoQuantidade.getText()
                );

                if (quantidadeEstoque < 0) {
                    labelStatus.setText(
                            "A quantidade não pode ser negativa."
                    );
                    return;
                }
            }

            /*
             * Prazo de produção:
             * usado somente para PERSONALIZADO.
             */
            Integer prazoProducaoDias = null;

            if (tipo == TipoProduto.PERSONALIZADO
                    && !campoPrazoProducao.getText().isBlank()) {

                prazoProducaoDias = Integer.parseInt(
                        campoPrazoProducao.getText()
                );

                if (prazoProducaoDias < 0) {
                    labelStatus.setText(
                            "O prazo não pode ser negativo."
                    );
                    return;
                }
            }

            String observacoes = null;

            if (tipo == TipoProduto.PERSONALIZADO) {
                observacoes = campoObservacoes.getText();
            }

            /*
             * Preço:
             *
             * marcado -> usuário informa o preço
             * desmarcado -> preço calculado pelos materiais
             */
            boolean calcularManualmente =
                    checkValorManual.isSelected();

            BigDecimal valorManual = null;

            if (calcularManualmente) {

                if (campoValorManual.getText().isBlank()) {
                    labelStatus.setText(
                            "Informe o valor de venda."
                    );
                    return;
                }

                valorManual = new BigDecimal(
                        campoValorManual
                                .getText()
                                .replace(",", ".")
                );

                if (valorManual.signum() <= 0) {
                    labelStatus.setText(
                            "O valor de venda deve ser maior que zero."
                    );
                    return;
                }
            }

            /*
             * Se não for preço manual,
             * precisamos ter materiais para calcular o valor.
             */
            if (!calcularManualmente
                    && materiaisEscolhidos.isEmpty()) {

                labelStatus.setText(
                        "Adicione ao menos um material ou marque " +
                                "'Calcular manualmente'."
                );

                return;
            }

            /*
             * Monta os itens de material.
             */
            List<ItemMaterialInput> itens = new ArrayList<>();

            for (int i = 0; i < materiaisEscolhidos.size(); i++) {

                itens.add(
                        new ItemMaterialInput(
                                materiaisEscolhidos
                                        .get(i)
                                        .getIdMaterial(),

                                quantidadesEscolhidas.get(i)
                        )
                );
            }

            /*
             * Chama o service.
             */
            Produto produto =
                    produtoService.cadastrarComMateriais(
                            codigo,
                            nome,
                            descricao,
                            tipo,
                            quantidadeEstoque,
                            prazoProducaoDias,
                            observacoes,
                            calcularManualmente,
                            valorManual,
                            itens
                    );

            labelStatus.setText(
                    "PRODUTO SALVO! "
                            + "Valor: R$ "
                            + produto.getValor()
            );

            limparFormulario();
            atualizarListaProdutos();

        } catch (NumberFormatException e) {

            labelStatus.setText(
                    "Erro: informe valores numéricos válidos."
            );

        } catch (IllegalArgumentException e) {

            labelStatus.setText(
                    "Erro: " + e.getMessage()
            );

        } catch (Exception e) {

            labelStatus.setText(
                    "Erro inesperado: " + e.getMessage()
            );
        }
    }

    @FXML
    public void limpar() {
        limparFormulario();
        labelStatus.setText("");
    }

    private void limparFormulario() {

        campoCodigo.clear();
        campoNome.clear();
        campoDescricao.clear();

        comboTipo.getSelectionModel()
                .select(TipoProduto.PRE_PRONTO);

        campoQuantidade.clear();
        campoPrazoProducao.clear();
        campoObservacoes.clear();

        checkValorManual.setSelected(false);
        campoValorManual.clear();

        campoPrazoProducao.setDisable(true);
        campoObservacoes.setDisable(true);
        campoQuantidade.setDisable(false);
        campoValorManual.setDisable(true);

        comboMaterial.setValue(null);
        campoQuantidadeMaterial.clear();

        materiaisEscolhidos.clear();
        quantidadesEscolhidas.clear();

        listaMateriaisAdicionados
                .getItems()
                .clear();
    }

    @FXML
    public void atualizarCampoValorManual() {

        campoValorManual.setDisable(
                !checkValorManual.isSelected()
        );
    }

    @FXML
    public void listar() {
        atualizarListaProdutos();
        labelStatus.setText("Lista de produtos atualizada.");
    }

    private void atualizarListaProdutos() {

        listaProdutos.setItems(
                FXCollections.observableArrayList(

                        produtoService.listarTodos()
                                .stream()
                                .map(this::formatarProduto)
                                .toList()
                )
        );
    }

    private String formatarProduto(Produto produto) {

        StringBuilder texto = new StringBuilder();

        texto.append("[")
                .append(produto.getCodigo())
                .append("] ")
                .append(produto.getNome());

        texto.append(" | ")
                .append(produto.getTipo());

        texto.append(" | R$ ")
                .append(produto.getValor());

        if (produto.getTipo() == TipoProduto.PRE_PRONTO) {

            texto.append(" | estoque: ")
                    .append(produto.getQuantidadeEstoque());

        } else {

            texto.append(" | prazo: ")
                    .append(produto.getPrazoProducaoDias())
                    .append(" dias");
        }

        return texto.toString();
    }
}