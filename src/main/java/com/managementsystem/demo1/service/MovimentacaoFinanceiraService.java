package com.managementsystem.demo1.service;

import com.managementsystem.demo1.model.CategoriaDespesa;
import com.managementsystem.demo1.model.MovimentacaoFinanceira;
import com.managementsystem.demo1.model.SituacaoMovimentacaoFinanceira;
import com.managementsystem.demo1.model.TipoMovimentacaoFinanceira;
import com.managementsystem.demo1.repository.CategoriaDespesaRepository;
import com.managementsystem.demo1.repository.MovimentacaoFinanceiraRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class MovimentacaoFinanceiraService {

    private final MovimentacaoFinanceiraRepository movimentacaoFinanceiraRepository;
    private final CategoriaDespesaRepository categoriaDespesaRepository;

    public MovimentacaoFinanceiraService(MovimentacaoFinanceiraRepository movimentacaoFinanceiraRepository) {
        this(movimentacaoFinanceiraRepository, null);
    }

    @Autowired
    public MovimentacaoFinanceiraService(MovimentacaoFinanceiraRepository movimentacaoFinanceiraRepository,
                                         CategoriaDespesaRepository categoriaDespesaRepository) {
        this.movimentacaoFinanceiraRepository = movimentacaoFinanceiraRepository;
        this.categoriaDespesaRepository = categoriaDespesaRepository;
    }

    @Transactional
    public MovimentacaoFinanceira cadastrarReceita(MovimentacaoFinanceira receita) {
        if (receita == null) {
            throw new IllegalArgumentException("Receita não pode ser nula.");
        }

        if (receita.getTipo() != null && receita.getTipo() != TipoMovimentacaoFinanceira.RECEITA) {
            throw new IllegalArgumentException(
                    "Não é permitido cadastrar uma movimentação do tipo " + receita.getTipo() + " como receita."
            );
        }

        receita.setTipo(TipoMovimentacaoFinanceira.RECEITA);
        receita.setCategoriaDespesa(null);

        validarDadosMovimentacao(receita, "receita", false);

        normalizarCamposComuns(receita);

        return movimentacaoFinanceiraRepository.save(receita);
    }

    @Transactional
    public MovimentacaoFinanceira cadastrarReceita(String descricao,
                                                   BigDecimal valor,
                                                   LocalDate dataMovimentacao) {
        MovimentacaoFinanceira receita = new MovimentacaoFinanceira(
                descricao,
                valor,
                TipoMovimentacaoFinanceira.RECEITA,
                dataMovimentacao
        );
        return cadastrarReceita(receita);
    }

    @Transactional
    public MovimentacaoFinanceira cadastrarReceita(String descricao,
                                                   BigDecimal valor,
                                                   String categoria,
                                                   SituacaoMovimentacaoFinanceira situacao,
                                                   LocalDate dataMovimentacao,
                                                   LocalDate dataVencimento,
                                                   String observacoes) {
        MovimentacaoFinanceira receita = new MovimentacaoFinanceira(
                descricao,
                valor,
                TipoMovimentacaoFinanceira.RECEITA,
                categoria,
                situacao,
                dataMovimentacao,
                dataVencimento,
                observacoes
        );
        return cadastrarReceita(receita);
    }

    @Transactional
    public MovimentacaoFinanceira cadastrarDespesa(MovimentacaoFinanceira despesa) {
        if (despesa == null) {
            throw new IllegalArgumentException("Despesa não pode ser nula.");
        }

        if (despesa.getTipo() != null && despesa.getTipo() != TipoMovimentacaoFinanceira.DESPESA) {
            throw new IllegalArgumentException(
                    "Não é permitido cadastrar uma movimentação do tipo " + despesa.getTipo() + " como despesa."
            );
        }

        despesa.setTipo(TipoMovimentacaoFinanceira.DESPESA);

        validarDadosMovimentacao(despesa, "despesa", true);

        vincularCategoriaDespesa(despesa);

        normalizarCamposComuns(despesa);

        return movimentacaoFinanceiraRepository.save(despesa);
    }

    @Transactional
    public MovimentacaoFinanceira cadastrarDespesa(String descricao,
                                                   BigDecimal valor,
                                                   String categoria,
                                                   LocalDate dataMovimentacao) {
        MovimentacaoFinanceira despesa = new MovimentacaoFinanceira(
                descricao,
                valor,
                TipoMovimentacaoFinanceira.DESPESA,
                categoria,
                SituacaoMovimentacaoFinanceira.PAGO,
                dataMovimentacao,
                null,
                null
        );
        return cadastrarDespesa(despesa);
    }

    @Transactional
    public MovimentacaoFinanceira cadastrarDespesa(String descricao,
                                                   BigDecimal valor,
                                                   String categoria,
                                                   SituacaoMovimentacaoFinanceira situacao,
                                                   LocalDate dataMovimentacao,
                                                   LocalDate dataVencimento,
                                                   String observacoes) {
        MovimentacaoFinanceira despesa = new MovimentacaoFinanceira(
                descricao,
                valor,
                TipoMovimentacaoFinanceira.DESPESA,
                categoria,
                situacao,
                dataMovimentacao,
                dataVencimento,
                observacoes
        );
        return cadastrarDespesa(despesa);
    }

    @Transactional
    public MovimentacaoFinanceira cadastrarDespesaPorCategoria(String descricao,
                                                               BigDecimal valor,
                                                               Long idCategoriaDespesa,
                                                               LocalDate dataMovimentacao) {
        return cadastrarDespesaPorCategoria(
                descricao,
                valor,
                idCategoriaDespesa,
                SituacaoMovimentacaoFinanceira.PAGO,
                dataMovimentacao,
                null,
                null
        );
    }

    @Transactional
    public MovimentacaoFinanceira cadastrarDespesaPorCategoria(String descricao,
                                                               BigDecimal valor,
                                                               Long idCategoriaDespesa,
                                                               SituacaoMovimentacaoFinanceira situacao,
                                                               LocalDate dataMovimentacao,
                                                               LocalDate dataVencimento,
                                                               String observacoes) {
        if (idCategoriaDespesa == null) {
            throw new IllegalArgumentException("Categoria da despesa é obrigatória.");
        }
        CategoriaDespesa referencia = new CategoriaDespesa();
        referencia.setIdCategoria(idCategoriaDespesa);
        return cadastrarDespesaPorCategoria(
                descricao,
                valor,
                referencia,
                situacao,
                dataMovimentacao,
                dataVencimento,
                observacoes
        );
    }

    @Transactional
    public MovimentacaoFinanceira cadastrarDespesaPorCategoria(String descricao,
                                                               BigDecimal valor,
                                                               CategoriaDespesa categoriaDespesa,
                                                               LocalDate dataMovimentacao) {
        return cadastrarDespesaPorCategoria(
                descricao,
                valor,
                categoriaDespesa,
                SituacaoMovimentacaoFinanceira.PAGO,
                dataMovimentacao,
                null,
                null
        );
    }

    @Transactional
    public MovimentacaoFinanceira cadastrarDespesaPorCategoria(String descricao,
                                                               BigDecimal valor,
                                                               CategoriaDespesa categoriaDespesa,
                                                               SituacaoMovimentacaoFinanceira situacao,
                                                               LocalDate dataMovimentacao,
                                                               LocalDate dataVencimento,
                                                               String observacoes) {
        MovimentacaoFinanceira despesa = new MovimentacaoFinanceira();
        despesa.setDescricao(descricao);
        despesa.setValor(valor);
        despesa.setTipo(TipoMovimentacaoFinanceira.DESPESA);
        despesa.setCategoriaDespesa(categoriaDespesa);
        despesa.setSituacao(situacao);
        despesa.setDataMovimentacao(dataMovimentacao);
        despesa.setDataVencimento(dataVencimento);
        despesa.setObservacoes(observacoes);
        return cadastrarDespesa(despesa);
    }

    @Transactional(readOnly = true)
    public List<MovimentacaoFinanceira> listarReceitas() {
        return movimentacaoFinanceiraRepository
                .findByTipoOrderByDataMovimentacaoDescIdMovimentacaoDesc(TipoMovimentacaoFinanceira.RECEITA);
    }

    @Transactional(readOnly = true)
    public Optional<MovimentacaoFinanceira> buscarReceitaPorId(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return movimentacaoFinanceiraRepository.findById(id)
                .filter(m -> m.getTipo() == TipoMovimentacaoFinanceira.RECEITA);
    }

    @Transactional(readOnly = true)
    public MovimentacaoFinanceira obterReceitaPorId(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID da receita é obrigatório.");
        }
        return buscarReceitaPorId(id)
                .orElseThrow(() -> new NoSuchElementException("Receita não encontrada: id " + id));
    }

    @Transactional(readOnly = true)
    public List<MovimentacaoFinanceira> listarDespesas() {
        return movimentacaoFinanceiraRepository
                .findByTipoOrderByDataMovimentacaoDescIdMovimentacaoDesc(TipoMovimentacaoFinanceira.DESPESA);
    }

    @Transactional(readOnly = true)
    public List<MovimentacaoFinanceira> listarDespesasPorCategoria(Long idCategoriaDespesa) {
        if (idCategoriaDespesa == null) {
            throw new IllegalArgumentException("ID da categoria de despesa é obrigatório.");
        }
        if (categoriaDespesaRepository != null && !categoriaDespesaRepository.existsById(idCategoriaDespesa)) {
            throw new NoSuchElementException("Categoria de despesa não encontrada: id " + idCategoriaDespesa);
        }
        return movimentacaoFinanceiraRepository
                .findByTipoAndCategoriaDespesa_IdCategoriaOrderByDataMovimentacaoDescIdMovimentacaoDesc(
                        TipoMovimentacaoFinanceira.DESPESA,
                        idCategoriaDespesa
                );
    }

    @Transactional(readOnly = true)
    public List<MovimentacaoFinanceira> listarDespesasPorCategoria(String nomeCategoria) {
        if (nomeCategoria == null || nomeCategoria.isBlank()) {
            return listarDespesas();
        }
        return movimentacaoFinanceiraRepository
                .findByTipoAndCategoriaIgnoreCaseOrderByDataMovimentacaoDescIdMovimentacaoDesc(
                        TipoMovimentacaoFinanceira.DESPESA,
                        nomeCategoria.trim()
                );
    }

    @Transactional(readOnly = true)
    public Optional<MovimentacaoFinanceira> buscarDespesaPorId(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return movimentacaoFinanceiraRepository.findById(id)
                .filter(m -> m.getTipo() == TipoMovimentacaoFinanceira.DESPESA);
    }

    @Transactional(readOnly = true)
    public MovimentacaoFinanceira obterDespesaPorId(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID da despesa é obrigatório.");
        }
        return buscarDespesaPorId(id)
                .orElseThrow(() -> new NoSuchElementException("Despesa não encontrada: id " + id));
    }

    private void validarDadosMovimentacao(MovimentacaoFinanceira movimentacao,
                                          String rotuloTipo,
                                          boolean exigirCategoria) {
        if (movimentacao.getDescricao() == null || movimentacao.getDescricao().isBlank()) {
            throw new IllegalArgumentException("Descrição da " + rotuloTipo + " é obrigatória.");
        }

        if (movimentacao.getDescricao().trim().length() > 255) {
            throw new IllegalArgumentException("Descrição da " + rotuloTipo + " não pode ultrapassar 255 caracteres.");
        }

        if (movimentacao.getValor() == null || movimentacao.getValor().signum() <= 0) {
            throw new IllegalArgumentException("Valor da " + rotuloTipo + " deve ser maior que zero.");
        }

        boolean possuiIdCategoria = movimentacao.getCategoriaDespesa() != null
                && movimentacao.getCategoriaDespesa().getIdCategoria() != null;

        if (exigirCategoria
                && !possuiIdCategoria
                && (movimentacao.getCategoria() == null || movimentacao.getCategoria().isBlank())) {
            throw new IllegalArgumentException("Categoria da " + rotuloTipo + " é obrigatória.");
        }

        if (movimentacao.getCategoria() != null
                && !movimentacao.getCategoria().isBlank()
                && movimentacao.getCategoria().trim().length() > 100) {
            throw new IllegalArgumentException("Categoria não pode ultrapassar 100 caracteres.");
        }

        if (movimentacao.getDataMovimentacao() == null) {
            throw new IllegalArgumentException("Data da movimentação é obrigatória.");
        }

        if (movimentacao.getDataVencimento() != null
                && movimentacao.getDataVencimento().isBefore(movimentacao.getDataMovimentacao())) {
            throw new IllegalArgumentException(
                    "Data de vencimento não pode ser anterior à data da movimentação."
            );
        }

        if (movimentacao.getObservacoes() != null
                && !movimentacao.getObservacoes().isBlank()
                && movimentacao.getObservacoes().trim().length() > 500) {
            throw new IllegalArgumentException("Observações não podem ultrapassar 500 caracteres.");
        }
    }

    private void vincularCategoriaDespesa(MovimentacaoFinanceira despesa) {
        if (categoriaDespesaRepository == null) {
            return;
        }

        String nomeInformado = limparCampoOpcional(despesa.getCategoria());
        Long idCategoriaInformado = despesa.getCategoriaDespesa() != null
                ? despesa.getCategoriaDespesa().getIdCategoria()
                : null;

        if (idCategoriaInformado != null) {
            CategoriaDespesa categoriaBanco = categoriaDespesaRepository.findById(idCategoriaInformado)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Categoria de despesa não encontrada para o ID informado: " + idCategoriaInformado
                    ));

            if (nomeInformado != null && !categoriaBanco.getNome().trim().equalsIgnoreCase(nomeInformado)) {
                throw new IllegalArgumentException(
                        "O nome da categoria informada diverge da categoria cadastrada para o ID "
                                + idCategoriaInformado + "."
                );
            }

            despesa.setCategoriaDespesa(categoriaBanco);
            despesa.setCategoria(categoriaBanco.getNome().trim());
            return;
        }

        if (nomeInformado != null) {
            CategoriaDespesa categoriaBanco = buscarCategoriaPorNome(nomeInformado)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Categoria de despesa não encontrada: " + nomeInformado
                    ));

            despesa.setCategoriaDespesa(categoriaBanco);
            despesa.setCategoria(categoriaBanco.getNome().trim());
        }
    }

    private Optional<CategoriaDespesa> buscarCategoriaPorNome(String nomeTrim) {
        Optional<CategoriaDespesa> direta = categoriaDespesaRepository.findByNomeIgnoreCase(nomeTrim);
        if (direta.isPresent()) {
            return direta;
        }
        return categoriaDespesaRepository.findAll().stream()
                .filter(c -> c.getNome() != null && c.getNome().trim().equalsIgnoreCase(nomeTrim))
                .findFirst();
    }

    private void normalizarCamposComuns(MovimentacaoFinanceira movimentacao) {
        movimentacao.setDescricao(movimentacao.getDescricao().trim());
        movimentacao.setCategoria(limparCampoOpcional(movimentacao.getCategoria()));
        movimentacao.setObservacoes(limparCampoOpcional(movimentacao.getObservacoes()));

        if (movimentacao.getSituacao() == null) {
            movimentacao.setSituacao(SituacaoMovimentacaoFinanceira.PAGO);
        }
    }

    private String limparCampoOpcional(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return valor.trim();
    }
}
