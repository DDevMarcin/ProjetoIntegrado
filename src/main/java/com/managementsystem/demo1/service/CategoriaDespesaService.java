package com.managementsystem.demo1.service;

import com.managementsystem.demo1.model.CategoriaDespesa;
import com.managementsystem.demo1.model.MovimentacaoFinanceira;
import com.managementsystem.demo1.model.TipoMovimentacaoFinanceira;
import com.managementsystem.demo1.repository.CategoriaDespesaRepository;
import com.managementsystem.demo1.repository.MovimentacaoFinanceiraRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class CategoriaDespesaService {

    public static final Map<String, String> CATEGORIAS_PADRAO;

    static {
        Map<String, String> padrao = new LinkedHashMap<>();
        padrao.put("Insumos", "Compra de matérias-primas, contas, fios, entremeios e crucifixos");
        padrao.put("Embalagens", "Caixas, saquinhos de linho, tags e materiais de envio");
        padrao.put("Frete", "Despesas de envio pelos Correios e transportadoras");
        padrao.put("Logística", "Custos logísticos de transporte e deslocamento");
        padrao.put("Custos Fixos", "Energia, internet, água e despesas recorrentes do ateliê");
        padrao.put("Ferramentas e Equipamentos", "Alicates, organizadores e utensílios de produção");
        padrao.put("Marketing e Divulgação", "Anúncios, fotografia e materiais promocionais");
        padrao.put("Impostos e Taxas", "DAS MEI, taxas de meios de pagamento e tarifas bancárias");
        padrao.put("Outros", "Demais despesas gerais do ateliê");
        CATEGORIAS_PADRAO = Map.copyOf(padrao);
    }

    private final CategoriaDespesaRepository categoriaDespesaRepository;
    private final MovimentacaoFinanceiraRepository movimentacaoFinanceiraRepository;

    public CategoriaDespesaService(CategoriaDespesaRepository categoriaDespesaRepository) {
        this(categoriaDespesaRepository, null);
    }

    @Autowired
    public CategoriaDespesaService(CategoriaDespesaRepository categoriaDespesaRepository,
                                   MovimentacaoFinanceiraRepository movimentacaoFinanceiraRepository) {
        this.categoriaDespesaRepository = categoriaDespesaRepository;
        this.movimentacaoFinanceiraRepository = movimentacaoFinanceiraRepository;
    }

    @Transactional
    public void garantirCategoriasPadrao() {
        for (Map.Entry<String, String> entry : CATEGORIAS_PADRAO.entrySet()) {
            String nome = entry.getKey();
            if (!existeCategoriaComMesmoNome(nome)) {
                categoriaDespesaRepository.save(new CategoriaDespesa(nome, entry.getValue()));
            }
        }
    }

    @Transactional
    public CategoriaDespesa salvar(CategoriaDespesa categoria) {
        validar(categoria);

        String nomeTrim = categoria.getNome().trim();
        String descricaoTrim = limparCampoOpcional(categoria.getDescricao());

        if (categoria.getIdCategoria() != null) {
            if (!categoriaDespesaRepository.existsById(categoria.getIdCategoria())) {
                throw new NoSuchElementException(
                        "Categoria de despesa não encontrada: id " + categoria.getIdCategoria()
                );
            }

            if (existeOutraCategoriaComMesmoNome(nomeTrim, categoria.getIdCategoria())) {
                throw new IllegalArgumentException(
                        "Já existe outra categoria de despesa cadastrada com o nome informado: " + nomeTrim
                );
            }
        } else {
            if (existeCategoriaComMesmoNome(nomeTrim)) {
                throw new IllegalArgumentException(
                        "Já existe uma categoria de despesa cadastrada com o nome informado: " + nomeTrim
                );
            }
        }

        categoria.setNome(nomeTrim);
        categoria.setDescricao(descricaoTrim);

        return categoriaDespesaRepository.save(categoria);
    }

    @Transactional
    public CategoriaDespesa salvar(String nome) {
        return salvar(new CategoriaDespesa(nome));
    }

    @Transactional
    public CategoriaDespesa salvar(String nome, String descricao) {
        return salvar(new CategoriaDespesa(nome, descricao));
    }

    @Transactional(readOnly = true)
    public List<CategoriaDespesa> listarTodas() {
        return categoriaDespesaRepository.findAllByOrderByNomeAsc();
    }

    @Transactional(readOnly = true)
    public Optional<CategoriaDespesa> buscarPorId(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return categoriaDespesaRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public CategoriaDespesa obterPorId(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID da categoria de despesa é obrigatório.");
        }
        return buscarPorId(id)
                .orElseThrow(() -> new NoSuchElementException("Categoria de despesa não encontrada: id " + id));
    }

    @Transactional(readOnly = true)
    public List<CategoriaDespesa> buscarPorNome(String nome) {
        if (nome == null || nome.isBlank()) {
            return listarTodas();
        }
        return categoriaDespesaRepository.findByNomeContainingIgnoreCaseOrderByNomeAsc(nome.trim());
    }

    @Transactional(readOnly = true)
    public Optional<CategoriaDespesa> buscarPorNomeExato(String nome) {
        if (nome == null || nome.isBlank()) {
            return Optional.empty();
        }
        String nomeTrim = nome.trim();
        Optional<CategoriaDespesa> direta = categoriaDespesaRepository.findByNomeIgnoreCase(nomeTrim);
        if (direta.isPresent()) {
            return direta;
        }
        return categoriaDespesaRepository.findAll().stream()
                .filter(c -> c.getNome() != null && c.getNome().trim().equalsIgnoreCase(nomeTrim))
                .findFirst();
    }

    @Transactional
    public CategoriaDespesa atualizar(Long id, String nome, String descricao) {
        if (id == null) {
            throw new IllegalArgumentException("ID da categoria de despesa é obrigatório.");
        }

        CategoriaDespesa existente = buscarPorId(id)
                .orElseThrow(() -> new NoSuchElementException("Categoria de despesa não encontrada: id " + id));

        CategoriaDespesa dados = new CategoriaDespesa(nome, descricao);
        validar(dados);

        String nomeTrim = dados.getNome().trim();
        String descricaoTrim = limparCampoOpcional(dados.getDescricao());

        if (existeOutraCategoriaComMesmoNome(nomeTrim, id)) {
            throw new IllegalArgumentException(
                    "Já existe outra categoria de despesa cadastrada com o nome informado: " + nomeTrim
            );
        }

        String nomeAnterior = existente.getNome();
        existente.setNome(nomeTrim);
        existente.setDescricao(descricaoTrim);

        CategoriaDespesa atualizada = categoriaDespesaRepository.save(existente);

        if (movimentacaoFinanceiraRepository != null
                && nomeAnterior != null
                && !nomeAnterior.equals(nomeTrim)) {
            List<MovimentacaoFinanceira> vinculadas = movimentacaoFinanceiraRepository
                    .findByCategoriaDespesa_IdCategoriaOrderByDataMovimentacaoDescIdMovimentacaoDesc(id);
            for (MovimentacaoFinanceira mov : vinculadas) {
                mov.setCategoria(nomeTrim);
                movimentacaoFinanceiraRepository.save(mov);
            }
        }

        return atualizada;
    }

    @Transactional
    public CategoriaDespesa atualizar(Long id, CategoriaDespesa dadosAtualizados) {
        if (dadosAtualizados == null) {
            throw new IllegalArgumentException("Categoria de despesa não pode ser nula.");
        }
        return atualizar(id, dadosAtualizados.getNome(), dadosAtualizados.getDescricao());
    }

    @Transactional
    public void excluir(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID da categoria de despesa é obrigatório.");
        }

        CategoriaDespesa categoria = buscarPorId(id)
                .orElseThrow(() -> new NoSuchElementException("Categoria de despesa não encontrada: id " + id));

        if (movimentacaoFinanceiraRepository != null) {
            boolean possuiVinculoPorFk = movimentacaoFinanceiraRepository.existsByCategoriaDespesa_IdCategoria(id);
            boolean possuiVinculoPorNome = categoria.getNome() != null
                    && movimentacaoFinanceiraRepository.existsByTipoAndCategoriaIgnoreCase(
                    TipoMovimentacaoFinanceira.DESPESA,
                    categoria.getNome().trim()
            );

            if (possuiVinculoPorFk || possuiVinculoPorNome) {
                throw new IllegalStateException(
                        "Não é possível excluir a categoria pois ela está vinculada a uma ou mais despesas."
                );
            }
        }

        categoriaDespesaRepository.deleteById(id);
    }

    private void validar(CategoriaDespesa categoria) {
        if (categoria == null) {
            throw new IllegalArgumentException("Categoria de despesa não pode ser nula.");
        }

        if (categoria.getNome() == null || categoria.getNome().isBlank()) {
            throw new IllegalArgumentException("Nome da categoria de despesa é obrigatório.");
        }

        if (categoria.getNome().trim().length() > 100) {
            throw new IllegalArgumentException("Nome da categoria de despesa não pode ultrapassar 100 caracteres.");
        }

        if (categoria.getDescricao() != null
                && !categoria.getDescricao().isBlank()
                && categoria.getDescricao().trim().length() > 255) {
            throw new IllegalArgumentException("Descrição da categoria de despesa não pode ultrapassar 255 caracteres.");
        }
    }

    private String limparCampoOpcional(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return valor.trim();
    }

    private boolean existeCategoriaComMesmoNome(String nomeTrim) {
        return categoriaDespesaRepository.existsByNomeIgnoreCase(nomeTrim)
                || categoriaDespesaRepository.findAll().stream()
                .anyMatch(c -> c.getNome() != null && c.getNome().trim().equalsIgnoreCase(nomeTrim));
    }

    private boolean existeOutraCategoriaComMesmoNome(String nomeTrim, Long idIgnorado) {
        return categoriaDespesaRepository.existsByNomeIgnoreCaseAndIdCategoriaNot(nomeTrim, idIgnorado)
                || categoriaDespesaRepository.findAll().stream()
                .anyMatch(c -> c.getIdCategoria() != null
                        && !c.getIdCategoria().equals(idIgnorado)
                        && c.getNome() != null
                        && c.getNome().trim().equalsIgnoreCase(nomeTrim));
    }
}

