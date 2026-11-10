package com.managementsystem.demo1.service;

import com.managementsystem.demo1.model.MovimentacaoFinanceira;
import com.managementsystem.demo1.model.SituacaoMovimentacaoFinanceira;
import com.managementsystem.demo1.model.TipoMovimentacaoFinanceira;
import com.managementsystem.demo1.repository.MovimentacaoFinanceiraRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class MovimentacaoFinanceiraService {

    private final MovimentacaoFinanceiraRepository movimentacaoFinanceiraRepository;

    public MovimentacaoFinanceiraService(MovimentacaoFinanceiraRepository movimentacaoFinanceiraRepository) {
        this.movimentacaoFinanceiraRepository = movimentacaoFinanceiraRepository;
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

    @Transactional(readOnly = true)
    public List<MovimentacaoFinanceira> listarMovimentacoes() {
        return movimentacaoFinanceiraRepository.findAllByOrderByDataMovimentacaoDescIdMovimentacaoDesc();
    }

    @Transactional(readOnly = true)
    public List<MovimentacaoFinanceira> listarTodas() {
        return listarMovimentacoes();
    }

    @Transactional(readOnly = true)
    public Optional<MovimentacaoFinanceira> buscarPorId(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return movimentacaoFinanceiraRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<MovimentacaoFinanceira> buscarMovimentacaoPorId(Long id) {
        return buscarPorId(id);
    }

    @Transactional(readOnly = true)
    public MovimentacaoFinanceira obterPorId(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID da movimentação financeira é obrigatório.");
        }
        return buscarPorId(id)
                .orElseThrow(() -> new NoSuchElementException("Movimentação financeira não encontrada: id " + id));
    }

    @Transactional(readOnly = true)
    public MovimentacaoFinanceira obterMovimentacaoPorId(Long id) {
        return obterPorId(id);
    }

    @Transactional(readOnly = true)
    public List<MovimentacaoFinanceira> listarPorTipo(TipoMovimentacaoFinanceira tipo) {
        if (tipo == null) {
            return listarMovimentacoes();
        }
        return movimentacaoFinanceiraRepository
                .findByTipoOrderByDataMovimentacaoDescIdMovimentacaoDesc(tipo);
    }

    @Transactional(readOnly = true)
    public List<MovimentacaoFinanceira> listarPorSituacao(SituacaoMovimentacaoFinanceira situacao) {
        if (situacao == null) {
            return listarMovimentacoes();
        }
        return movimentacaoFinanceiraRepository
                .findBySituacaoOrderByDataMovimentacaoDescIdMovimentacaoDesc(situacao);
    }

    @Transactional(readOnly = true)
    public List<MovimentacaoFinanceira> listarPorCategoria(String categoria) {
        if (categoria == null || categoria.isBlank()) {
            return listarMovimentacoes();
        }
        return movimentacaoFinanceiraRepository
                .findByCategoriaIgnoreCaseOrderByDataMovimentacaoDescIdMovimentacaoDesc(categoria.trim());
    }

    @Transactional(readOnly = true)
    public List<MovimentacaoFinanceira> listarPorPeriodo(LocalDate dataInicio, LocalDate dataFim) {
        if (dataInicio == null || dataFim == null) {
            throw new IllegalArgumentException("Data inicial e data final são obrigatórias para consulta por período.");
        }
        if (dataFim.isBefore(dataInicio)) {
            throw new IllegalArgumentException("Data final não pode ser anterior à data inicial.");
        }
        return movimentacaoFinanceiraRepository
                .findByDataMovimentacaoBetweenOrderByDataMovimentacaoDescIdMovimentacaoDesc(dataInicio, dataFim);
    }

    @Transactional(readOnly = true)
    public List<MovimentacaoFinanceira> buscarPorDescricao(String descricao) {
        if (descricao == null || descricao.isBlank()) {
            return listarMovimentacoes();
        }
        return movimentacaoFinanceiraRepository
                .findByDescricaoContainingIgnoreCaseOrderByDataMovimentacaoDescIdMovimentacaoDesc(descricao.trim());
    }

    @Transactional(readOnly = true)
    public List<MovimentacaoFinanceira> consultar(TipoMovimentacaoFinanceira tipo,
                                                  SituacaoMovimentacaoFinanceira situacao,
                                                  String categoria) {
        return consultar(tipo, situacao, categoria, null, null, null);
    }

    @Transactional(readOnly = true)
    public List<MovimentacaoFinanceira> consultar(TipoMovimentacaoFinanceira tipo,
                                                  SituacaoMovimentacaoFinanceira situacao,
                                                  String categoria,
                                                  LocalDate dataInicio,
                                                  LocalDate dataFim) {
        return consultar(tipo, situacao, categoria, dataInicio, dataFim, null);
    }

    @Transactional(readOnly = true)
    public List<MovimentacaoFinanceira> consultar(TipoMovimentacaoFinanceira tipo,
                                                  SituacaoMovimentacaoFinanceira situacao,
                                                  String categoria,
                                                  LocalDate dataInicio,
                                                  LocalDate dataFim,
                                                  String descricao) {
        if (dataInicio != null && dataFim != null && dataFim.isBefore(dataInicio)) {
            throw new IllegalArgumentException("Data final não pode ser anterior à data inicial.");
        }

        String categoriaFiltrada = limparCampoOpcional(categoria);
        String descricaoFiltrada = limparCampoOpcional(descricao);

        boolean semPeriodo = dataInicio == null && dataFim == null;
        boolean semDescricao = descricaoFiltrada == null;

        if (tipo == null && situacao == null && categoriaFiltrada == null && semPeriodo && semDescricao) {
            return listarMovimentacoes();
        }

        if (tipo != null && situacao == null && categoriaFiltrada == null && semPeriodo && semDescricao) {
            return movimentacaoFinanceiraRepository
                    .findByTipoOrderByDataMovimentacaoDescIdMovimentacaoDesc(tipo);
        }

        if (tipo == null && situacao != null && categoriaFiltrada == null && semPeriodo && semDescricao) {
            return movimentacaoFinanceiraRepository
                    .findBySituacaoOrderByDataMovimentacaoDescIdMovimentacaoDesc(situacao);
        }

        if (tipo == null && situacao == null && categoriaFiltrada != null && semPeriodo && semDescricao) {
            return movimentacaoFinanceiraRepository
                    .findByCategoriaIgnoreCaseOrderByDataMovimentacaoDescIdMovimentacaoDesc(categoriaFiltrada);
        }

        if (tipo != null && situacao != null && categoriaFiltrada == null && semPeriodo && semDescricao) {
            return movimentacaoFinanceiraRepository
                    .findByTipoAndSituacaoOrderByDataMovimentacaoDescIdMovimentacaoDesc(tipo, situacao);
        }

        if (tipo != null && situacao == null && categoriaFiltrada != null && semPeriodo && semDescricao) {
            return movimentacaoFinanceiraRepository
                    .findByTipoAndCategoriaIgnoreCaseOrderByDataMovimentacaoDescIdMovimentacaoDesc(tipo, categoriaFiltrada);
        }

        if (tipo == null && situacao == null && categoriaFiltrada == null
                && dataInicio != null && dataFim != null && semDescricao) {
            return movimentacaoFinanceiraRepository
                    .findByDataMovimentacaoBetweenOrderByDataMovimentacaoDescIdMovimentacaoDesc(dataInicio, dataFim);
        }

        if (tipo == null && situacao == null && categoriaFiltrada == null && semPeriodo && descricaoFiltrada != null) {
            return movimentacaoFinanceiraRepository
                    .findByDescricaoContainingIgnoreCaseOrderByDataMovimentacaoDescIdMovimentacaoDesc(descricaoFiltrada);
        }

        String descricaoLower = descricaoFiltrada != null ? descricaoFiltrada.toLowerCase(Locale.ROOT) : null;

        return listarMovimentacoes().stream()
                .filter(m -> tipo == null || m.getTipo() == tipo)
                .filter(m -> situacao == null || m.getSituacao() == situacao)
                .filter(m -> categoriaFiltrada == null
                        || (m.getCategoria() != null && m.getCategoria().trim().equalsIgnoreCase(categoriaFiltrada)))
                .filter(m -> dataInicio == null
                        || (m.getDataMovimentacao() != null && !m.getDataMovimentacao().isBefore(dataInicio)))
                .filter(m -> dataFim == null
                        || (m.getDataMovimentacao() != null && !m.getDataMovimentacao().isAfter(dataFim)))
                .filter(m -> descricaoLower == null
                        || (m.getDescricao() != null && m.getDescricao().toLowerCase(Locale.ROOT).contains(descricaoLower)))
                .toList();
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

        if (exigirCategoria && (movimentacao.getCategoria() == null || movimentacao.getCategoria().isBlank())) {
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
