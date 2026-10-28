package com.managementsystem.demo1.controller;

import com.managementsystem.demo1.model.Material;
import com.managementsystem.demo1.model.MovimentacaoMaterial;
import com.managementsystem.demo1.model.TipoMovimentacaoMaterial;
import com.managementsystem.demo1.service.MaterialService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Component
public class GerenciarMaterialController {

    private static final DateTimeFormatter FORMATADOR_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    @FXML
    private Label labelTotalMateriais;

    @FXML
    private TextField campoIdSelecionado;

    @FXML
    private TextField campoNome;

    @FXML
    private TextField campoPrecoUnidade;

    @FXML
    private TextField campoUnidadeMedida;

    @FXML
    private TextField campoQuantidadeEstoqueInicial;

    @FXML
    private Button botaoAtualizar;

    @FXML
    private Button botaoExcluir;

    @FXML
    private TextField campoFiltroNome;

    @FXML
    private TextField campoFiltroUnidade;

    @FXML
    private TextField campoBuscaId;

    @FXML
    private Label labelStatus;

    @FXML
    private ListView<String> listaMateriais;

    @FXML
    private Label labelDetalhesId;

    @FXML
    private Label labelDetalhesNome;

    @FXML
    private Label labelDetalhesPreco;

    @FXML
    private Label labelDetalhesUnidade;

    @FXML
    private Label labelDetalhesEstoque;

    // --- Controles de Movimentação e Perda ---
    @FXML
    private ComboBox<TipoMovimentacaoMaterial> comboTipoMovimentacao;

    @FXML
    private TextField campoQuantidadeMovimentacao;

    @FXML
    private TextField campoMotivoMovimentacao;

    @FXML
    private TextField campoObservacoesMovimentacao;

    @FXML
    private Button botaoRegistrarMovimentacao;

    @FXML
    private Button botaoRegistrarPerda;

    @FXML
    private ListView<String> listaHistoricoMovimentacoes;

    private final MaterialService materialService;

    private List<Material> materiaisCarregados = List.of();
    private Material materialSelecionado = null;

    public GerenciarMaterialController(MaterialService materialService) {
        this.materialService = materialService;
    }

    @FXML
    public void initialize() {
        if (comboTipoMovimentacao != null) {
            comboTipoMovimentacao.setItems(FXCollections.observableArrayList(TipoMovimentacaoMaterial.values()));
            comboTipoMovimentacao.getSelectionModel().select(TipoMovimentacaoMaterial.ENTRADA);
        }
        configurarSelecaoLista();
        carregarTodos();
        carregarHistoricoGeral();
        atualizarEstadoBotoesEdicao();
    }

    private void configurarSelecaoLista() {
        listaMateriais.getSelectionModel().selectedIndexProperty().addListener((obs, antigo, novo) -> {
            int indice = novo.intValue();
            if (indice >= 0 && indice < materiaisCarregados.size()) {
                selecionarMaterial(materiaisCarregados.get(indice));
            }
        });
    }

    private void selecionarMaterial(Material material) {
        this.materialSelecionado = material;
        campoIdSelecionado.setText(String.valueOf(material.getIdMaterial()));
        campoNome.setText(material.getNome());
        campoPrecoUnidade.setText(material.getPrecoUnidade() != null ? material.getPrecoUnidade().toPlainString() : "");
        campoUnidadeMedida.setText(material.getUnidadeMedida());
        if (campoQuantidadeEstoqueInicial != null) {
            campoQuantidadeEstoqueInicial.setText(
                    material.getQuantidadeEstoque() != null ? material.getQuantidadeEstoque().toPlainString() : "0"
            );
            campoQuantidadeEstoqueInicial.setDisable(true);
        }

        labelDetalhesId.setText(String.valueOf(material.getIdMaterial()));
        labelDetalhesNome.setText(material.getNome());
        labelDetalhesPreco.setText("R$ " + material.getPrecoUnidade());
        labelDetalhesUnidade.setText(material.getUnidadeMedida());
        if (labelDetalhesEstoque != null) {
            labelDetalhesEstoque.setText(
                    (material.getQuantidadeEstoque() != null ? material.getQuantidadeEstoque() : BigDecimal.ZERO)
                            + " " + material.getUnidadeMedida()
            );
        }

        atualizarEstadoBotoesEdicao();
        carregarHistoricoDoSelecionado();
    }

    private void atualizarEstadoBotoesEdicao() {
        boolean temSelecao = (materialSelecionado != null && materialSelecionado.getIdMaterial() != null);
        if (botaoAtualizar != null) {
            botaoAtualizar.setDisable(!temSelecao);
        }
        if (botaoExcluir != null) {
            botaoExcluir.setDisable(!temSelecao);
        }
        if (botaoRegistrarMovimentacao != null) {
            botaoRegistrarMovimentacao.setDisable(!temSelecao);
        }
        if (botaoRegistrarPerda != null) {
            botaoRegistrarPerda.setDisable(!temSelecao);
        }
    }

    @FXML
    public void cadastrarNovo() {
        try {
            String nome = campoNome.getText();
            BigDecimal preco = lerDecimal(campoPrecoUnidade.getText(), "Informe um valor numérico válido para o preço por unidade.");
            String unidade = campoUnidadeMedida.getText();
            BigDecimal estoqueInicial = BigDecimal.ZERO;
            if (campoQuantidadeEstoqueInicial != null && campoQuantidadeEstoqueInicial.getText() != null && !campoQuantidadeEstoqueInicial.getText().isBlank()) {
                estoqueInicial = lerDecimal(campoQuantidadeEstoqueInicial.getText(), "Informe um valor numérico válido para o estoque inicial.");
            }

            Material salvo = materialService.salvar(nome, preco, unidade, estoqueInicial);

            limparFormulario();
            carregarTodos();
            exibirMensagemSucesso("Material cadastrado com sucesso! ID gerado: " + salvo.getIdMaterial()
                    + " | Estoque inicial: " + salvo.getQuantidadeEstoque() + " " + salvo.getUnidadeMedida());
        } catch (IllegalArgumentException e) {
            exibirMensagemErro("Erro de validação: " + e.getMessage());
        } catch (Exception e) {
            exibirMensagemErro("Erro inesperado ao cadastrar: " + e.getMessage());
        }
    }

    @FXML
    public void atualizarSelecionado() {
        if (materialSelecionado == null || materialSelecionado.getIdMaterial() == null) {
            exibirMensagemErro("Selecione um material na lista ou busque por ID antes de atualizar.");
            return;
        }

        try {
            Long id = materialSelecionado.getIdMaterial();
            String nome = campoNome.getText();
            BigDecimal preco = lerDecimal(campoPrecoUnidade.getText(), "Informe um valor numérico válido para o preço por unidade.");
            String unidade = campoUnidadeMedida.getText();

            Material atualizado = materialService.atualizar(id, nome, preco, unidade);

            carregarTodos();
            selecionarMaterialNaListaPorId(atualizado.getIdMaterial());
            exibirMensagemSucesso("Material ID " + atualizado.getIdMaterial() + " atualizado com sucesso!");
        } catch (NoSuchElementException e) {
            exibirMensagemErro("Não encontrado: " + e.getMessage());
            carregarTodos();
        } catch (IllegalArgumentException e) {
            exibirMensagemErro("Erro de validação: " + e.getMessage());
        } catch (Exception e) {
            exibirMensagemErro("Erro inesperado ao atualizar: " + e.getMessage());
        }
    }

    @FXML
    public void excluirSelecionado() {
        if (materialSelecionado == null || materialSelecionado.getIdMaterial() == null) {
            exibirMensagemErro("Selecione um material na lista ou busque por ID antes de excluir.");
            return;
        }

        Long id = materialSelecionado.getIdMaterial();
        String nome = materialSelecionado.getNome();

        try {
            materialService.excluir(id);
            limparFormulario();
            carregarTodos();
            carregarHistoricoGeral();
            exibirMensagemSucesso("Material [" + id + " - " + nome + "] excluído com sucesso!");
        } catch (NoSuchElementException e) {
            exibirMensagemErro("Não encontrado: " + e.getMessage());
            carregarTodos();
        } catch (IllegalStateException | IllegalArgumentException e) {
            exibirMensagemErro("Não foi possível excluir: " + e.getMessage());
        } catch (Exception e) {
            exibirMensagemErro("Erro inesperado ao excluir: " + e.getMessage());
        }
    }

    @FXML
    public void registrarMovimentacao() {
        if (materialSelecionado == null || materialSelecionado.getIdMaterial() == null) {
            exibirMensagemErro("Selecione um material na lista antes de registrar uma movimentação.");
            return;
        }

        try {
            Long idMaterial = materialSelecionado.getIdMaterial();
            TipoMovimentacaoMaterial tipo = comboTipoMovimentacao != null ? comboTipoMovimentacao.getValue() : null;
            BigDecimal quantidade = lerDecimal(campoQuantidadeMovimentacao.getText(), "Informe uma quantidade numérica válida para a movimentação.");
            String motivo = campoMotivoMovimentacao.getText();
            String observacoes = campoObservacoesMovimentacao.getText();

            MovimentacaoMaterial mov = materialService.registrarMovimentacao(idMaterial, tipo, quantidade, motivo, observacoes);

            limparCamposMovimentacao();
            carregarTodos();
            selecionarMaterialNaListaPorId(idMaterial);
            exibirMensagemSucesso(String.format("Movimentação (%s) registrada! Saldo: %s -> %s %s",
                    mov.getTipo(), mov.getSaldoAnterior(), mov.getSaldoAtual(), mov.getMaterial().getUnidadeMedida()));
        } catch (IllegalArgumentException | NoSuchElementException e) {
            exibirMensagemErro("Erro na movimentação: " + e.getMessage());
        } catch (Exception e) {
            exibirMensagemErro("Erro inesperado ao movimentar estoque: " + e.getMessage());
        }
    }

    @FXML
    public void registrarPerdaDireta() {
        if (materialSelecionado == null || materialSelecionado.getIdMaterial() == null) {
            exibirMensagemErro("Selecione um material na lista antes de registrar uma perda.");
            return;
        }

        try {
            Long idMaterial = materialSelecionado.getIdMaterial();
            BigDecimal quantidade = lerDecimal(campoQuantidadeMovimentacao.getText(), "Informe uma quantidade numérica válida para a perda.");
            String motivo = campoMotivoMovimentacao.getText();
            String observacoes = campoObservacoesMovimentacao.getText();

            MovimentacaoMaterial perda = materialService.registrarPerda(idMaterial, quantidade, motivo, observacoes);

            limparCamposMovimentacao();
            carregarTodos();
            selecionarMaterialNaListaPorId(idMaterial);
            exibirMensagemSucesso(String.format("Perda registrada com sucesso! Saldo reduzido: %s -> %s %s (Motivo: %s)",
                    perda.getSaldoAnterior(), perda.getSaldoAtual(), perda.getMaterial().getUnidadeMedida(), perda.getMotivo()));
        } catch (IllegalArgumentException | NoSuchElementException e) {
            exibirMensagemErro("Erro ao registrar perda: " + e.getMessage());
        } catch (Exception e) {
            exibirMensagemErro("Erro inesperado ao registrar perda: " + e.getMessage());
        }
    }

    @FXML
    public void carregarHistoricoDoSelecionado() {
        if (listaHistoricoMovimentacoes == null) {
            return;
        }
        if (materialSelecionado == null || materialSelecionado.getIdMaterial() == null) {
            carregarHistoricoGeral();
            return;
        }
        List<MovimentacaoMaterial> historico = materialService.listarMovimentacoesPorMaterial(materialSelecionado.getIdMaterial());
        atualizarListaHistorico(historico);
    }

    @FXML
    public void carregarHistoricoGeral() {
        if (listaHistoricoMovimentacoes == null) {
            return;
        }
        List<MovimentacaoMaterial> historico = materialService.listarMovimentacoes();
        atualizarListaHistorico(historico);
    }

    @FXML
    public void carregarApenasPerdas() {
        if (listaHistoricoMovimentacoes == null) {
            return;
        }
        List<MovimentacaoMaterial> perdas = (materialSelecionado != null && materialSelecionado.getIdMaterial() != null)
                ? materialService.listarPerdasPorMaterial(materialSelecionado.getIdMaterial())
                : materialService.listarPerdas();
        atualizarListaHistorico(perdas);
        exibirMensagemInfo("Exibindo histórico de perdas (" + perdas.size() + " registro(s)).");
    }

    @FXML
    public void carregarTodos() {
        materiaisCarregados = materialService.listarTodos();
        atualizarListaVisual(materiaisCarregados);
        atualizarContadorTotal();
        exibirMensagemInfo("Lista atualizada: " + materiaisCarregados.size() + " material(is) cadastrado(s).");
    }

    @FXML
    public void filtrarMateriais() {
        String nome = campoFiltroNome.getText();
        String unidade = campoFiltroUnidade.getText();

        materiaisCarregados = materialService.consultar(nome, unidade);
        atualizarListaVisual(materiaisCarregados);
        atualizarContadorTotal();

        if (materiaisCarregados.isEmpty()) {
            exibirMensagemInfo("Nenhum material encontrado para os filtros informados.");
        } else {
            exibirMensagemInfo("Filtro aplicado: " + materiaisCarregados.size() + " material(is) encontrado(s).");
        }
    }

    @FXML
    public void buscarPorId() {
        String textoId = campoBuscaId.getText();
        if (textoId == null || textoId.isBlank()) {
            exibirMensagemErro("Informe um ID numérico para buscar.");
            return;
        }

        try {
            Long id = Long.parseLong(textoId.trim());
            Optional<Material> encontrado = materialService.buscarPorId(id);

            if (encontrado.isPresent()) {
                materiaisCarregados = List.of(encontrado.get());
                atualizarListaVisual(materiaisCarregados);
                listaMateriais.getSelectionModel().select(0);
                exibirMensagemSucesso("Material encontrado com ID " + id + ".");
            } else {
                materiaisCarregados = List.of();
                atualizarListaVisual(materiaisCarregados);
                limparDetalhesESelecao();
                exibirMensagemErro("Material não encontrado para o ID " + id + " (404).");
            }
        } catch (NumberFormatException e) {
            exibirMensagemErro("Erro: O ID deve ser um número inteiro válido.");
        }
    }

    @FXML
    public void limparFormulario() {
        campoIdSelecionado.clear();
        campoNome.clear();
        campoPrecoUnidade.clear();
        campoUnidadeMedida.clear();
        if (campoQuantidadeEstoqueInicial != null) {
            campoQuantidadeEstoqueInicial.clear();
            campoQuantidadeEstoqueInicial.setDisable(false);
        }
        limparCamposMovimentacao();
        listaMateriais.getSelectionModel().clearSelection();
        limparDetalhesESelecao();
        carregarHistoricoGeral();
        exibirMensagemInfo("Formulário limpo. Pronto para novo cadastro.");
    }

    private void limparCamposMovimentacao() {
        if (campoQuantidadeMovimentacao != null) {
            campoQuantidadeMovimentacao.clear();
        }
        if (campoMotivoMovimentacao != null) {
            campoMotivoMovimentacao.clear();
        }
        if (campoObservacoesMovimentacao != null) {
            campoObservacoesMovimentacao.clear();
        }
        if (comboTipoMovimentacao != null) {
            comboTipoMovimentacao.getSelectionModel().select(TipoMovimentacaoMaterial.ENTRADA);
        }
    }

    @FXML
    public void limparFiltros() {
        campoFiltroNome.clear();
        campoFiltroUnidade.clear();
        campoBuscaId.clear();
        carregarTodos();
    }

    private void limparDetalhesESelecao() {
        materialSelecionado = null;
        labelDetalhesId.setText("—");
        labelDetalhesNome.setText("—");
        labelDetalhesPreco.setText("—");
        labelDetalhesUnidade.setText("—");
        if (labelDetalhesEstoque != null) {
            labelDetalhesEstoque.setText("—");
        }
        atualizarEstadoBotoesEdicao();
    }

    private BigDecimal lerDecimal(String texto, String mensagemErro) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(texto.trim().replace(",", "."));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(mensagemErro);
        }
    }

    private void selecionarMaterialNaListaPorId(Long id) {
        for (int i = 0; i < materiaisCarregados.size(); i++) {
            if (materiaisCarregados.get(i).getIdMaterial().equals(id)) {
                listaMateriais.getSelectionModel().select(i);
                break;
            }
        }
    }

    private void atualizarContadorTotal() {
        int totalBanco = materialService.listarTodos().size();
        labelTotalMateriais.setText("Total de Materiais Cadastrados: " + totalBanco);
    }

    private void atualizarListaVisual(List<Material> materiais) {
        listaMateriais.setItems(
                FXCollections.observableArrayList(
                        materiais.stream().map(this::formatarMaterial).toList()
                )
        );
    }

    private void atualizarListaHistorico(List<MovimentacaoMaterial> historico) {
        if (listaHistoricoMovimentacoes == null) {
            return;
        }
        listaHistoricoMovimentacoes.setItems(
                FXCollections.observableArrayList(
                        historico.stream().map(this::formatarMovimentacao).toList()
                )
        );
    }

    private String formatarMaterial(Material m) {
        BigDecimal estoque = m.getQuantidadeEstoque() != null ? m.getQuantidadeEstoque() : BigDecimal.ZERO;
        return String.format("ID: %d | %s | R$ %.2f / %s | Estoque: %s %s",
                m.getIdMaterial(),
                m.getNome(),
                m.getPrecoUnidade(),
                m.getUnidadeMedida(),
                estoque.toPlainString(),
                m.getUnidadeMedida());
    }

    private String formatarMovimentacao(MovimentacaoMaterial mov) {
        String dataStr = mov.getDataHora() != null ? mov.getDataHora().format(FORMATADOR_DATA) : "—";
        String nomeMat = mov.getMaterial() != null ? mov.getMaterial().getNome() : "Material";
        String un = mov.getMaterial() != null ? mov.getMaterial().getUnidadeMedida() : "";
        String motivoStr = (mov.getMotivo() != null && !mov.getMotivo().isBlank()) ? " | Motivo: " + mov.getMotivo() : "";
        String obsStr = (mov.getObservacoes() != null && !mov.getObservacoes().isBlank()) ? " (" + mov.getObservacoes() + ")" : "";
        return String.format("[#%d | %s] %s — %s: %s %s (Saldo: %s -> %s %s)%s%s",
                mov.getIdMovimentacao(),
                dataStr,
                nomeMat,
                mov.getTipo(),
                mov.getQuantidade(),
                un,
                mov.getSaldoAnterior(),
                mov.getSaldoAtual(),
                un,
                motivoStr,
                obsStr);
    }

    private void exibirMensagemSucesso(String msg) {
        labelStatus.setStyle("-fx-text-fill: #1b6e2d; -fx-font-weight: bold; -fx-padding: 4 0 4 0;");
        labelStatus.setText(msg);
    }

    private void exibirMensagemErro(String msg) {
        labelStatus.setStyle("-fx-text-fill: #b91c1c; -fx-font-weight: bold; -fx-padding: 4 0 4 0;");
        labelStatus.setText(msg);
    }

    private void exibirMensagemInfo(String msg) {
        labelStatus.setStyle("-fx-text-fill: #333333; -fx-font-style: italic; -fx-padding: 4 0 4 0;");
        labelStatus.setText(msg);
    }
}
