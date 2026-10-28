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
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

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

        String codigoTrim = validarCodigoObrigatorio(codigo);
        if (produtoRepository.existsByCodigo(codigoTrim)) {
            throw new IllegalArgumentException("Já existe um produto com o código informado: " + codigoTrim);
        }

        String nomeTrim = validarNomeObrigatorio(nome);
        if (existeProdutoComMesmoNome(nomeTrim)) {
            throw new IllegalArgumentException("Já existe um produto cadastrado com o nome informado: " + nomeTrim);
        }

        String descricaoTrim = validarDescricaoOpcional(descricao);

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
        produto.setCodigo(codigoTrim);
        produto.setNome(nomeTrim);
        produto.setDescricao(descricaoTrim);
        produto.setTipo(tipo);
        produto.setValorManual(calcularManualmente);

        if (tipo == TipoProduto.PRE_PRONTO) {
            if (quantidadeEstoque != null && quantidadeEstoque < 0) {
                throw new IllegalArgumentException("A quantidade em estoque não pode ser negativa.");
            }
            produto.setQuantidadeEstoque(quantidadeEstoque == null ? 0 : quantidadeEstoque);
        } else {
            if (prazoProducaoDias != null && prazoProducaoDias < 0) {
                throw new IllegalArgumentException("O prazo de produção não pode ser negativo.");
            }
            String observacoesTrim = validarObservacoesOpcional(observacoes);
            produto.setPrazoProducaoDias(prazoProducaoDias);
            produto.setObservacoes(observacoesTrim);
        }

        BigDecimal valorCalculado = BigDecimal.ZERO;
        List<ProdutoMaterial> vinculos = new ArrayList<>();

        if (itensMaterial != null) {
            Set<Long> materiaisVistos = new HashSet<>();
            for (ItemMaterialInput item : itensMaterial) {
                if (item == null || item.idMaterial() == null) {
                    throw new IllegalArgumentException("ID do material é obrigatório.");
                }
                Long idMaterial = item.idMaterial();
                if (!materiaisVistos.add(idMaterial)) {
                    throw new IllegalArgumentException("Material duplicado na composição do produto: id " + idMaterial);
                }
                if (item.quantidadeUtilizada() == null || item.quantidadeUtilizada().signum() <= 0) {
                    throw new IllegalArgumentException("Quantidade utilizada do material deve ser maior que zero.");
                }
                Material material = materialRepository.findById(idMaterial)
                        .orElseThrow(() -> new NoSuchElementException(
                                "Material não encontrado: id " + idMaterial));

                valorCalculado = valorCalculado.add(material.getPrecoUnidade().multiply(item.quantidadeUtilizada()));
                vinculos.add(new ProdutoMaterial(produto, material, item.quantidadeUtilizada()));
            }
        }

        produto.setValor(calcularManualmente ? valorManual : valorCalculado);
        produto.setMateriaisUtilizados(vinculos);

        return produtoRepository.save(produto);
    }

    public List<Produto> listarTodos() {
        return produtoRepository.findAll();
    }

    public Produto buscarPorId(Long id) {
        if (id == null) {
            throw new NoSuchElementException("ID do produto não informado.");
        }
        return produtoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Produto não encontrado: id " + id));
    }

    public List<Produto> buscarPorNome(String nome) {
        if (nome == null || nome.isBlank()) {
            return listarTodos();
        }
        return produtoRepository.findByNomeContainingIgnoreCase(nome.trim());
    }

    public List<Produto> listarEstoque() {
        return produtoRepository.findByTipo(TipoProduto.PRE_PRONTO);
    }

    public List<Produto> buscarEstoquePorNome(String nome) {
        if (nome == null || nome.isBlank()) {
            return listarEstoque();
        }
        return produtoRepository.findByTipoAndNomeContainingIgnoreCase(TipoProduto.PRE_PRONTO, nome.trim());
    }

    public Optional<Produto> buscarEstoquePorId(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return produtoRepository.findByIdProdutoAndTipo(id, TipoProduto.PRE_PRONTO);
    }

    /** Soma a quantidade em estoque de todos os produtos PRE_PRONTO (rodapé "Total de Produtos: X" do protótipo). */
    public int totalEmEstoque() {
        Long total = produtoRepository.sumQuantidadeEstoque();
        return total != null ? total.intValue() : 0;
    }

    @Transactional
    public Produto atualizar(Long id, String codigo, String nome, String descricao, Integer quantidadeEstoque,
                             Integer prazoProducaoDias, String observacoes) {
        if (id == null) {
            throw new IllegalArgumentException("ID do produto é obrigatório.");
        }
        Produto produto = buscarPorId(id);

        String codigoTrim = validarCodigoObrigatorio(codigo);
        if (produtoRepository.existsByCodigoAndIdProdutoNot(codigoTrim, id)) {
            throw new IllegalArgumentException("Já existe outro produto cadastrado com o código informado: " + codigoTrim);
        }

        String nomeTrim = validarNomeObrigatorio(nome);
        if (existeOutroProdutoComMesmoNome(nomeTrim, id)) {
            throw new IllegalArgumentException("Já existe outro produto cadastrado com o nome informado: " + nomeTrim);
        }

        String descricaoTrim = validarDescricaoOpcional(descricao);

        produto.setCodigo(codigoTrim);
        produto.setNome(nomeTrim);
        produto.setDescricao(descricaoTrim);

        if (produto.getTipo() == TipoProduto.PRE_PRONTO) {
            if (quantidadeEstoque != null && quantidadeEstoque < 0) {
                throw new IllegalArgumentException("A quantidade em estoque não pode ser negativa.");
            }
            produto.setQuantidadeEstoque(quantidadeEstoque == null ? 0 : quantidadeEstoque);
        } else {
            if (prazoProducaoDias != null && prazoProducaoDias < 0) {
                throw new IllegalArgumentException("O prazo de produção não pode ser negativo.");
            }
            String observacoesTrim = validarObservacoesOpcional(observacoes);
            produto.setPrazoProducaoDias(prazoProducaoDias);
            produto.setObservacoes(observacoesTrim);
        }

        return produtoRepository.save(produto);
    }

    @Transactional
    public void excluir(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID do produto é obrigatório.");
        }
        if (!produtoRepository.existsById(id)) {
            throw new NoSuchElementException("Produto não encontrado: id " + id);
        }
        produtoRepository.deleteById(id);
    }

    private String validarCodigoObrigatorio(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("Código do produto é obrigatório.");
        }
        String codigoTrim = codigo.trim();
        if (codigoTrim.length() > 50) {
            throw new IllegalArgumentException("Código do produto não pode ultrapassar 50 caracteres.");
        }
        return codigoTrim;
    }

    private String validarNomeObrigatorio(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome do produto é obrigatório.");
        }
        String nomeTrim = nome.trim();
        if (nomeTrim.length() > 255) {
            throw new IllegalArgumentException("Nome do produto não pode ultrapassar 255 caracteres.");
        }
        return nomeTrim;
    }

    private String validarDescricaoOpcional(String descricao) {
        if (descricao == null) {
            return null;
        }
        String descricaoTrim = descricao.trim();
        if (descricaoTrim.length() > 255) {
            throw new IllegalArgumentException("Descrição do produto não pode ultrapassar 255 caracteres.");
        }
        return descricaoTrim;
    }

    private String validarObservacoesOpcional(String observacoes) {
        if (observacoes == null) {
            return null;
        }
        String observacoesTrim = observacoes.trim();
        if (observacoesTrim.length() > 500) {
            throw new IllegalArgumentException("Observações do produto não podem ultrapassar 500 caracteres.");
        }
        return observacoesTrim;
    }

    private boolean existeProdutoComMesmoNome(String nomeTrim) {
        return produtoRepository.existsByNomeIgnoreCase(nomeTrim)
                || produtoRepository.findAll().stream()
                        .anyMatch(p -> p.getNome() != null && p.getNome().trim().equalsIgnoreCase(nomeTrim));
    }

    private boolean existeOutroProdutoComMesmoNome(String nomeTrim, Long idIgnorado) {
        return produtoRepository.existsByNomeIgnoreCaseAndIdProdutoNot(nomeTrim, idIgnorado)
                || produtoRepository.findAll().stream()
                        .anyMatch(p -> p.getIdProduto() != null
                                && !p.getIdProduto().equals(idIgnorado)
                                && p.getNome() != null
                                && p.getNome().trim().equalsIgnoreCase(nomeTrim));
    }
}
