package com.managementsystem.demo1.service;

import com.managementsystem.demo1.model.*;
import com.managementsystem.demo1.repository.EncomendaRepository;
import com.managementsystem.demo1.repository.MaterialRepository;
import com.managementsystem.demo1.repository.ProdutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;

@Service
public class EncomendaService {

    public static final BigDecimal MARGEM_LUCRO = new BigDecimal("1.20");

    private final EncomendaRepository encomendaRepository;
    private final MaterialRepository materialRepository;
    private final MaterialService materialService;
    private final ProdutoRepository produtoRepository;

    public EncomendaService(EncomendaRepository encomendaRepository,
                            MaterialRepository materialRepository) {
        this(encomendaRepository, materialRepository, null, null);
    }

    @Autowired
    public EncomendaService(EncomendaRepository encomendaRepository,
                            MaterialRepository materialRepository,
                            MaterialService materialService,
                            ProdutoRepository produtoRepository) {
        this.encomendaRepository = encomendaRepository;
        this.materialRepository = materialRepository;
        this.materialService = materialService;
        this.produtoRepository = produtoRepository;
    }

    @Transactional
    public Encomenda cadastrar(String nomeCliente, String contatoCliente, LocalDate dataPedido,
                               String observacoes, List<ItemEncomendaInput> itens) {
        if (nomeCliente == null || nomeCliente.isBlank()) {
            throw new IllegalArgumentException("Nome do cliente é obrigatório.");
        }
        if (nomeCliente.trim().length() > 255) {
            throw new IllegalArgumentException("Nome do cliente não pode ultrapassar 255 caracteres.");
        }
        if (contatoCliente != null && contatoCliente.trim().length() > 50) {
            throw new IllegalArgumentException("Contato do cliente não pode ultrapassar 50 caracteres.");
        }
        if (observacoes != null && observacoes.trim().length() > 500) {
            throw new IllegalArgumentException("Observações não podem ultrapassar 500 caracteres.");
        }
        if (itens == null || itens.isEmpty()) {
            throw new IllegalArgumentException("Adicione ao menos um item à encomenda.");
        }

        Encomenda encomenda = new Encomenda();
        encomenda.setNomeCliente(nomeCliente.trim());
        encomenda.setContatoCliente(vazioParaNull(contatoCliente));
        encomenda.setObservacoes(vazioParaNull(observacoes));
        encomenda.setDataPedido(dataPedido != null ? dataPedido : LocalDate.now());
        encomenda.setSituacao(SituacaoEncomenda.PENDENTE);

        BigDecimal subtotal = BigDecimal.ZERO;
        for (ItemEncomendaInput input : itens) {
            ItemEncomenda item = montarItem(input, encomenda.getNomeCliente());
            encomenda.adicionarItem(item);
            subtotal = subtotal.add(item.getValor());
        }

        encomenda.setSubtotal(subtotal.setScale(2, RoundingMode.HALF_UP));
        encomenda.setValorTotal(aplicarMargemLucro(subtotal));

        Integer maiorPrazo = itens.stream()
                .map(ItemEncomendaInput::prazoProducaoDias)
                .filter(Objects::nonNull)
                .max(Integer::compare)
                .orElse(null);
        encomenda.setDataEntregaPrevista(
                maiorPrazo == null ? null : encomenda.getDataPedido().plusDays(maiorPrazo));

        return encomendaRepository.save(encomenda);
    }

    private ItemEncomenda montarItem(ItemEncomendaInput input, String nomeCliente) {
        if (input == null || input.nome() == null || input.nome().isBlank()) {
            throw new IllegalArgumentException("Nome do item é obrigatório.");
        }
        if (input.nome().trim().length() > 255) {
            throw new IllegalArgumentException("Nome do item não pode ultrapassar 255 caracteres.");
        }
        if (input.descricao() != null && input.descricao().trim().length() > 255) {
            throw new IllegalArgumentException("Descrição do item não podem ultrapassar 255 caracteres.");
        }
        if (input.observacoes() != null && input.observacoes().trim().length() > 500) {
            throw new IllegalArgumentException("Observações do item não podem ultrapassar 500 caracteres.");
        }
        if (input.prazoProducaoDias() != null && input.prazoProducaoDias() < 0) {
            throw new IllegalArgumentException("O prazo de produção não pode ser negativo.");
        }

        ItemEncomenda item = new ItemEncomenda();
        item.setNome(input.nome().trim());
        item.setDescricao(vazioParaNull(input.descricao()));
        item.setObservacoes(vazioParaNull(input.observacoes()));
        item.setPrazoProducaoDias(input.prazoProducaoDias());
        item.setValorManual(input.valorManual());
        item.setIdProduto(input.idProduto());
        item.setQuantidadeProduto(input.quantidadeProduto());

        // Caso seja produto PRÉ-PRONTO, dá baixa no estoque do produto
        if (input.idProduto() != null && produtoRepository != null) {
            int qtdSolicitada = (input.quantidadeProduto() != null && input.quantidadeProduto() > 0)
                    ? input.quantidadeProduto() : 1;

            Produto produto = produtoRepository.findById(input.idProduto())
                    .orElseThrow(() -> new NoSuchElementException("Produto pré-pronto não encontrado: id " + input.idProduto()));

            int estoqueAtual = produto.getQuantidadeEstoque() != null ? produto.getQuantidadeEstoque() : 0;
            if (estoqueAtual < qtdSolicitada) {
                throw new IllegalArgumentException(
                        "Estoque insuficiente para o produto \"" + produto.getNome() + "\". Disponível: " + estoqueAtual);
            }

            produto.setQuantidadeEstoque(estoqueAtual - qtdSolicitada);
            produtoRepository.save(produto);
        }

        BigDecimal valor = BigDecimal.ZERO;
        List<ItemMaterialInput> materiais = input.materiais();

        if (input.valorManual()) {
            if (input.valorInformado() == null || input.valorInformado().signum() <= 0) {
                throw new IllegalArgumentException("Informe um valor válido para o item \"" + input.nome().trim() + "\".");
            }
            valor = input.valorInformado();
        } else if (materiais == null || materiais.isEmpty()) {
            throw new IllegalArgumentException(
                    "O item \"" + input.nome().trim() + "\" precisa de ao menos um material, ou de valor manual.");
        }

        if (materiais != null) {
            Set<Long> vistos = new HashSet<>();
            for (ItemMaterialInput m : materiais) {
                if (m == null || m.idMaterial() == null) {
                    throw new IllegalArgumentException("ID do material é obrigatório.");
                }
                if (!vistos.add(m.idMaterial())) {
                    throw new IllegalArgumentException("Material repetido no item: id " + m.idMaterial());
                }
                if (m.quantidadeUtilizada() == null || m.quantidadeUtilizada().signum() <= 0) {
                    throw new IllegalArgumentException("Quantidade utilizada deve ser maior que zero.");
                }

                Material material = materialRepository.findById(m.idMaterial())
                        .orElseThrow(() -> new NoSuchElementException("Material não encontrado: id " + m.idMaterial()));

                // Dá baixa no estoque e registra movimentação de SAIDA
                if (materialService != null) {
                    materialService.registrarMovimentacao(
                            material.getIdMaterial(),
                            TipoMovimentacaoMaterial.SAIDA,
                            m.quantidadeUtilizada(),
                            "Encomenda - " + nomeCliente,
                            "Item: " + item.getNome()
                    );
                } else {
                    BigDecimal saldoAtual = material.getQuantidadeEstoque() != null
                            ? material.getQuantidadeEstoque() : BigDecimal.ZERO;
                    if (saldoAtual.compareTo(m.quantidadeUtilizada()) < 0) {
                        throw new IllegalArgumentException(
                                "Saldo insuficiente em estoque para o material: " + material.getNome());
                    }
                    material.setQuantidadeEstoque(saldoAtual.subtract(m.quantidadeUtilizada()));
                    materialRepository.save(material);
                }

                item.adicionarMaterial(new ItemEncomendaMaterial(material, m.quantidadeUtilizada()));
                if (!input.valorManual()) {
                    valor = valor.add(material.getPrecoUnidade().multiply(m.quantidadeUtilizada()));
                }
            }
        }

        item.setValor(valor.setScale(2, RoundingMode.HALF_UP));
        return item;
    }

    @Transactional
    public Encomenda alterarSituacao(Long id, SituacaoEncomenda nova) {
        if (id == null || nova == null) {
            throw new IllegalArgumentException("Encomenda e nova situação são obrigatórias.");
        }
        Encomenda encomenda = obterPorId(id);
        SituacaoEncomenda atual = encomenda.getSituacao();

        if (atual == nova) {
            throw new IllegalArgumentException("A encomenda já está " + atual + ".");
        }
        if (!transicaoPermitida(atual, nova)) {
            throw new IllegalArgumentException("Não é possível mudar de " + atual + " para " + nova + ".");
        }

        // Se a encomenda for cancelada, devolve materiais e produtos pré-prontos ao estoque
        if (nova == SituacaoEncomenda.CANCELADA) {
            estornarMateriais(encomenda);
            estornarProdutosPreProntos(encomenda);
        }

        encomenda.setSituacao(nova);
        return encomendaRepository.save(encomenda);
    }

    private void estornarProdutosPreProntos(Encomenda encomenda) {
        if (encomenda.getItens() == null || produtoRepository == null) return;

        for (ItemEncomenda item : encomenda.getItens()) {
            if (item.getIdProduto() != null) {
                int qtdEstornar = (item.getQuantidadeProduto() != null && item.getQuantidadeProduto() > 0)
                        ? item.getQuantidadeProduto() : 1;
                produtoRepository.findById(item.getIdProduto()).ifPresent(produto -> {
                    int atual = produto.getQuantidadeEstoque() != null ? produto.getQuantidadeEstoque() : 0;
                    produto.setQuantidadeEstoque(atual + qtdEstornar);
                    produtoRepository.save(produto);
                });
            }
        }
    }

    private void estornarMateriais(Encomenda encomenda) {
        if (encomenda.getItens() == null) return;

        for (ItemEncomenda item : encomenda.getItens()) {
            if (item.getMateriais() == null) continue;
            for (ItemEncomendaMaterial iem : item.getMateriais()) {
                Material mat = iem.getMaterial();
                if (mat == null || iem.getQuantidadeUtilizada() == null) continue;

                if (materialService != null) {
                    materialService.registrarMovimentacao(
                            mat.getIdMaterial(),
                            TipoMovimentacaoMaterial.ENTRADA,
                            iem.getQuantidadeUtilizada(),
                            "Estorno cancelamento encomenda - " + encomenda.getNomeCliente(),
                            "Item: " + item.getNome()
                    );
                } else {
                    BigDecimal saldoAtual = mat.getQuantidadeEstoque() != null
                            ? mat.getQuantidadeEstoque() : BigDecimal.ZERO;
                    mat.setQuantidadeEstoque(saldoAtual.add(iem.getQuantidadeUtilizada()));
                    materialRepository.save(mat);
                }
            }
        }
    }

    private boolean transicaoPermitida(SituacaoEncomenda de, SituacaoEncomenda para) {
        return switch (de) {
            case PENDENTE -> para == SituacaoEncomenda.EM_PRODUCAO || para == SituacaoEncomenda.CANCELADA;
            case EM_PRODUCAO -> para == SituacaoEncomenda.PRONTA || para == SituacaoEncomenda.CANCELADA;
            case PRONTA -> para == SituacaoEncomenda.ENTREGUE || para == SituacaoEncomenda.CANCELADA;
            case ENTREGUE, CANCELADA -> false;
        };
    }

    @Transactional(readOnly = true)
    public List<Encomenda> listarTodas() {
        return encomendaRepository.findAllByOrderByDataPedidoDescIdEncomendaDesc();
    }

    @Transactional(readOnly = true)
    public Encomenda obterPorId(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID da encomenda é obrigatório.");
        }
        return encomendaRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Encomenda não encontrada: id " + id));
    }

    @Transactional
    public void excluir(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID da encomenda é obrigatório.");
        }
        if (!encomendaRepository.existsById(id)) {
            throw new NoSuchElementException("Encomenda não encontrada: id " + id);
        }
        encomendaRepository.deleteById(id);
    }

    public BigDecimal aplicarMargemLucro(BigDecimal valor) {
        return valor.multiply(MARGEM_LUCRO).setScale(2, RoundingMode.HALF_UP);
    }

    private String vazioParaNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }
}