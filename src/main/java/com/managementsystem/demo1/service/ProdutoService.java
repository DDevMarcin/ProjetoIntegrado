package com.managementsystem.demo1.service;

import com.managementsystem.demo1.model.Material;
import com.managementsystem.demo1.model.Produto;
import com.managementsystem.demo1.model.ProdutoMaterial;
import com.managementsystem.demo1.model.TipoProduto;
import com.managementsystem.demo1.repository.MaterialRepository;
import com.managementsystem.demo1.repository.ProdutoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final MaterialRepository materialRepository;

    public ProdutoService(ProdutoRepository produtoRepository, MaterialRepository materialRepository) {
        this.produtoRepository = produtoRepository;
        this.materialRepository = materialRepository;
    }

    /**
     * @param calcularManualmente se true, usa valorManual direto (artesã digitou o preço).
     *                            Se false, calcula a partir dos materiais (itensMaterial obrigatório).
     * @param valorManual         obrigatório se calcularManualmente = true.
     * @param itensMaterial       obrigatório (não vazio) se calcularManualmente = false;
     *                            opcional (pode ser vazio) se calcularManualmente = true
     *                            (a artesã ainda pode registrar materiais usados, mesmo
     *                            definindo o preço na mão).
     */
    @Transactional
    public Produto cadastrarComMateriais(String codigo, String nome, String descricao, TipoProduto tipo,
                                         Integer quantidadeEstoque, Integer prazoProducaoDias,
                                         String observacoes, boolean calcularManualmente,
                                         BigDecimal valorManual, List<ItemMaterialInput> itensMaterial) {

        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("Código do produto é obrigatório.");
        }
        
        // Verifica se o código já existe
        produtoRepository.findByCodigo(codigo).ifPresent(produtoExistente -> {
            throw new IllegalArgumentException(
                "O código \"" + codigo + "\" já pertence ao produto \"" + produtoExistente.getNome() + "\"."
            );
        });
        
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome do produto é obrigatório.");
        }
        if (tipo == null) {
            throw new IllegalArgumentException("Tipo do produto é obrigatório.");
        }
        if (calcularManualmente && (valorManual == null || valorManual.signum() <= 0)) {
            throw new IllegalArgumentException("Informe um valor de venda válido.");
        }
        if (!calcularManualmente && (itensMaterial == null || itensMaterial.isEmpty())) {
            throw new IllegalArgumentException("Informe ao menos um material usado no produto, ou marque 'Calcular manualmente'.");
        }

        Produto produto = new Produto();
        produto.setCodigo(codigo);
        produto.setNome(nome);
        produto.setDescricao(descricao);
        produto.setTipo(tipo);
        produto.setValorManual(calcularManualmente);

        if (tipo == TipoProduto.PRE_PRONTO) {
            produto.setQuantidadeEstoque(quantidadeEstoque == null ? 0 : quantidadeEstoque);
        } else {
            produto.setPrazoProducaoDias(prazoProducaoDias);
            produto.setObservacoes(observacoes);
        }

        BigDecimal valorCalculado = BigDecimal.ZERO;
        List<ProdutoMaterial> vinculos = new ArrayList<>();

        if (itensMaterial != null) {
            for (ItemMaterialInput item : itensMaterial) {
                if (item.quantidadeUtilizada() == null || item.quantidadeUtilizada().signum() <= 0) {
                    throw new IllegalArgumentException("Quantidade utilizada do material deve ser maior que zero.");
                }
                Material material = materialRepository.findById(item.idMaterial())
                        .orElseThrow(() -> new NoSuchElementException(
                                "Material não encontrado: id " + item.idMaterial()));

                valorCalculado = valorCalculado.add(material.getPrecoUnidade().multiply(item.quantidadeUtilizada()));
                vinculos.add(new ProdutoMaterial(produto, material, item.quantidadeUtilizada()));
            }
        }

        produto.setValor(calcularManualmente ? valorManual : valorCalculado);
        produto.setMateriaisUtilizados(vinculos);

        return produtoRepository.save(produto);
    }

    @Transactional(readOnly = true)
    public List<Produto> listarTodos() {
        return produtoRepository.findAllWithMateriais();
    }

    @Transactional(readOnly = true)
    public Produto buscarPorId(Long id) {
        return produtoRepository.findByIdWithMateriais(id)
                .orElseThrow(() -> new NoSuchElementException("Produto não encontrado: id " + id));
    }

    @Transactional(readOnly = true)
    public List<Produto> buscarPorNome(String nome) {
        return produtoRepository.findAllWithMateriais().stream()
                .filter(p -> p.getNome().toLowerCase().contains(nome.toLowerCase()))
                .toList();
    }

    /** Soma a quantidade em estoque de todos os produtos PRE_PRONTO (rodapé "Total de Produtos: X" do protótipo). */
    public int totalEmEstoque() {
        return produtoRepository.findAll().stream()
                .filter(p -> p.getQuantidadeEstoque() != null)
                .mapToInt(Produto::getQuantidadeEstoque)
                .sum();
    }

    @Transactional
    public Produto atualizar(Long id, String codigo, String nome, String descricao, Integer quantidadeEstoque,
                             Integer prazoProducaoDias, String observacoes) {
        Produto produto = buscarPorId(id);

        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome do produto é obrigatório.");
        }
        if (codigo != null && !codigo.isBlank()) {
            // Verifica duplicidade de código, ignorando o próprio produto
            produtoRepository.findByCodigo(codigo).ifPresent(existente -> {
                if (!existente.getIdProduto().equals(id)) {
                    throw new IllegalArgumentException(
                        "O código \"" + codigo + "\" já pertence ao produto \"" + existente.getNome() + "\"."
                    );
                }
            });
            produto.setCodigo(codigo);
        }
        produto.setNome(nome);
        produto.setDescricao(descricao);

        if (produto.getTipo() == TipoProduto.PRE_PRONTO) {
            produto.setQuantidadeEstoque(quantidadeEstoque);
        } else {
            produto.setPrazoProducaoDias(prazoProducaoDias);
            produto.setObservacoes(observacoes);
        }

        return produtoRepository.save(produto);
    }

    @Transactional
    public void excluir(Long id) {
        produtoRepository.deleteById(id);
    }
}