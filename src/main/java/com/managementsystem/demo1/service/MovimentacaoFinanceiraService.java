package com.managementsystem.demo1.service;

import com.managementsystem.demo1.model.CategoriaDespesa;
import com.managementsystem.demo1.model.MovimentacaoFinanceira;
import com.managementsystem.demo1.model.SituacaoMovimentacaoFinanceira;
import com.managementsystem.demo1.model.TipoMovimentacaoFinanceira;
import com.managementsystem.demo1.repository.CategoriaDespesaRepository;
import com.managementsystem.demo1.repository.MovimentacaoFinanceiraRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class MovimentacaoFinanceiraService {

    private static final Sort ORDENACAO_PADRAO = Sort.by(
            Sort.Order.desc("dataMovimentacao"),
            Sort.Order.desc("idMovimentacao")
    );

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
    public List<MovimentacaoFinanceira> listarPorCategoria(Long idCategoriaDespesa) {
        if (idCategoriaDespesa == null) {
            return listarMovimentacoes();
        }
        if (categoriaDespesaRepository != null && !categoriaDespesaRepository.existsById(idCategoriaDespesa)) {
            throw new NoSuchElementException("Categoria de despesa não encontrada: id " + idCategoriaDespesa);
        }
        return movimentacaoFinanceiraRepository
                .findByCategoriaDespesa_IdCategoriaOrderByDataMovimentacaoDescIdMovimentacaoDesc(idCategoriaDespesa);
    }

    @Transactional(readOnly = true)
    public List<MovimentacaoFinanceira> listarPorPeriodo(LocalDate dataInicio, LocalDate dataFim) {
        validarPeriodo(dataInicio, dataFim);

        if (dataInicio != null && dataFim != null) {
            return movimentacaoFinanceiraRepository
                    .findByDataMovimentacaoBetweenOrderByDataMovimentacaoDescIdMovimentacaoDesc(dataInicio, dataFim);
        }
        if (dataInicio != null) {
            return movimentacaoFinanceiraRepository
                    .findByDataMovimentacaoGreaterThanEqualOrderByDataMovimentacaoDescIdMovimentacaoDesc(dataInicio);
        }
        if (dataFim != null) {
            return movimentacaoFinanceiraRepository
                    .findByDataMovimentacaoLessThanEqualOrderByDataMovimentacaoDescIdMovimentacaoDesc(dataFim);
        }
        return listarMovimentacoes();
    }

    @Transactional(readOnly = true)
    public List<MovimentacaoFinanceira> listarReceitasPorPeriodo(LocalDate dataInicio, LocalDate dataFim) {
        validarPeriodo(dataInicio, dataFim);

        if (dataInicio != null && dataFim != null) {
            return movimentacaoFinanceiraRepository
                    .findByTipoAndDataMovimentacaoBetweenOrderByDataMovimentacaoDescIdMovimentacaoDesc(
                            TipoMovimentacaoFinanceira.RECEITA,
                            dataInicio,
                            dataFim
                    );
        }
        if (dataInicio != null) {
            return movimentacaoFinanceiraRepository
                    .findByTipoAndDataMovimentacaoGreaterThanEqualOrderByDataMovimentacaoDescIdMovimentacaoDesc(
                            TipoMovimentacaoFinanceira.RECEITA,
                            dataInicio
                    );
        }
        if (dataFim != null) {
            return movimentacaoFinanceiraRepository
                    .findByTipoAndDataMovimentacaoLessThanEqualOrderByDataMovimentacaoDescIdMovimentacaoDesc(
                            TipoMovimentacaoFinanceira.RECEITA,
                            dataFim
                    );
        }
        return listarReceitas();
    }

    @Transactional(readOnly = true)
    public BigDecimal calcularTotalReceitas() {
        return calcularTotalReceitasPorPeriodo(null, null, null);
    }

    @Transactional(readOnly = true)
    public BigDecimal calcularTotalReceitasPorPeriodo(LocalDate dataInicio, LocalDate dataFim) {
        return calcularTotalReceitasPorPeriodo(dataInicio, dataFim, null);
    }

    @Transactional(readOnly = true)
    public BigDecimal calcularTotalReceitasPorPeriodo(LocalDate dataInicio,
                                                      LocalDate dataFim,
                                                      SituacaoMovimentacaoFinanceira situacao) {
        validarPeriodo(dataInicio, dataFim);

        BigDecimal total;
        if (situacao != null) {
            if (dataInicio != null && dataFim != null) {
                total = movimentacaoFinanceiraRepository.somarValorPorTipoESituacaoEPeriodo(
                        TipoMovimentacaoFinanceira.RECEITA,
                        situacao,
                        dataInicio,
                        dataFim
                );
            } else if (dataInicio != null) {
                total = movimentacaoFinanceiraRepository.somarValorPorTipoESituacaoEDataMovimentacaoMaiorOuIgual(
                        TipoMovimentacaoFinanceira.RECEITA,
                        situacao,
                        dataInicio
                );
            } else if (dataFim != null) {
                total = movimentacaoFinanceiraRepository.somarValorPorTipoESituacaoEDataMovimentacaoMenorOuIgual(
                        TipoMovimentacaoFinanceira.RECEITA,
                        situacao,
                        dataFim
                );
            } else {
                total = movimentacaoFinanceiraRepository.somarValorPorTipoESituacao(
                        TipoMovimentacaoFinanceira.RECEITA,
                        situacao
                );
            }
        } else {
            if (dataInicio != null && dataFim != null) {
                total = movimentacaoFinanceiraRepository.somarValorPorTipoEPeriodo(
                        TipoMovimentacaoFinanceira.RECEITA,
                        dataInicio,
                        dataFim
                );
            } else if (dataInicio != null) {
                total = movimentacaoFinanceiraRepository.somarValorPorTipoEDataMovimentacaoMaiorOuIgual(
                        TipoMovimentacaoFinanceira.RECEITA,
                        dataInicio
                );
            } else if (dataFim != null) {
                total = movimentacaoFinanceiraRepository.somarValorPorTipoEDataMovimentacaoMenorOuIgual(
                        TipoMovimentacaoFinanceira.RECEITA,
                        dataFim
                );
            } else {
                total = movimentacaoFinanceiraRepository.somarValorPorTipo(
                        TipoMovimentacaoFinanceira.RECEITA
                );
            }
        }

        return normalizarValorMonetario(total);
    }

    @Transactional(readOnly = true)
    public BigDecimal totalReceitas() {
        return calcularTotalReceitas();
    }

    @Transactional(readOnly = true)
    public BigDecimal totalReceitasPorPeriodo(LocalDate dataInicio, LocalDate dataFim) {
        return calcularTotalReceitasPorPeriodo(dataInicio, dataFim);
    }

    @Transactional(readOnly = true)
    public List<MovimentacaoFinanceira> listarDespesasPorPeriodo(LocalDate dataInicio, LocalDate dataFim) {
        validarPeriodo(dataInicio, dataFim);

        if (dataInicio != null && dataFim != null) {
            return movimentacaoFinanceiraRepository
                    .findByTipoAndDataMovimentacaoBetweenOrderByDataMovimentacaoDescIdMovimentacaoDesc(
                            TipoMovimentacaoFinanceira.DESPESA,
                            dataInicio,
                            dataFim
                    );
        }
        if (dataInicio != null) {
            return movimentacaoFinanceiraRepository
                    .findByTipoAndDataMovimentacaoGreaterThanEqualOrderByDataMovimentacaoDescIdMovimentacaoDesc(
                            TipoMovimentacaoFinanceira.DESPESA,
                            dataInicio
                    );
        }
        if (dataFim != null) {
            return movimentacaoFinanceiraRepository
                    .findByTipoAndDataMovimentacaoLessThanEqualOrderByDataMovimentacaoDescIdMovimentacaoDesc(
                            TipoMovimentacaoFinanceira.DESPESA,
                            dataFim
                    );
        }
        return listarDespesas();
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
        return consultar(tipo, situacao, null, categoria, null, null, null);
    }

    @Transactional(readOnly = true)
    public List<MovimentacaoFinanceira> consultar(TipoMovimentacaoFinanceira tipo,
                                                  SituacaoMovimentacaoFinanceira situacao,
                                                  String categoria,
                                                  LocalDate dataInicio,
                                                  LocalDate dataFim) {
        return consultar(tipo, situacao, null, categoria, dataInicio, dataFim, null);
    }

    @Transactional(readOnly = true)
    public List<MovimentacaoFinanceira> consultar(TipoMovimentacaoFinanceira tipo,
                                                  SituacaoMovimentacaoFinanceira situacao,
                                                  String categoria,
                                                  LocalDate dataInicio,
                                                  LocalDate dataFim,
                                                  String descricao) {
        return consultar(tipo, situacao, null, categoria, dataInicio, dataFim, descricao);
    }

    @Transactional(readOnly = true)
    public List<MovimentacaoFinanceira> consultar(TipoMovimentacaoFinanceira tipo,
                                                  SituacaoMovimentacaoFinanceira situacao,
                                                  Long idCategoriaDespesa,
                                                  String categoria,
                                                  LocalDate dataInicio,
                                                  LocalDate dataFim,
                                                  String descricao) {
        validarPeriodo(dataInicio, dataFim);

        if (idCategoriaDespesa != null
                && categoriaDespesaRepository != null
                && !categoriaDespesaRepository.existsById(idCategoriaDespesa)) {
            throw new NoSuchElementException("Categoria de despesa não encontrada: id " + idCategoriaDespesa);
        }

        String categoriaFiltrada = limparCampoOpcional(categoria);
        String descricaoFiltrada = limparCampoOpcional(descricao);

        boolean semCategoria = idCategoriaDespesa == null && categoriaFiltrada == null;
        boolean semPeriodo = dataInicio == null && dataFim == null;
        boolean semDescricao = descricaoFiltrada == null;

        if (tipo == null && situacao == null && semCategoria && semPeriodo && semDescricao) {
            return listarMovimentacoes();
        }

        if (tipo != null && situacao == null && semCategoria && semPeriodo && semDescricao) {
            return movimentacaoFinanceiraRepository
                    .findByTipoOrderByDataMovimentacaoDescIdMovimentacaoDesc(tipo);
        }

        if (tipo == null && situacao != null && semCategoria && semPeriodo && semDescricao) {
            return movimentacaoFinanceiraRepository
                    .findBySituacaoOrderByDataMovimentacaoDescIdMovimentacaoDesc(situacao);
        }

        if (tipo == null && situacao == null && idCategoriaDespesa == null
                && categoriaFiltrada != null && semPeriodo && semDescricao) {
            return movimentacaoFinanceiraRepository
                    .findByCategoriaIgnoreCaseOrderByDataMovimentacaoDescIdMovimentacaoDesc(categoriaFiltrada);
        }

        if (tipo == null && situacao == null && idCategoriaDespesa != null
                && categoriaFiltrada == null && semPeriodo && semDescricao) {
            return movimentacaoFinanceiraRepository
                    .findByCategoriaDespesa_IdCategoriaOrderByDataMovimentacaoDescIdMovimentacaoDesc(idCategoriaDespesa);
        }

        if (tipo != null && situacao != null && semCategoria && semPeriodo && semDescricao) {
            return movimentacaoFinanceiraRepository
                    .findByTipoAndSituacaoOrderByDataMovimentacaoDescIdMovimentacaoDesc(tipo, situacao);
        }

        if (tipo != null && situacao == null && idCategoriaDespesa == null
                && categoriaFiltrada != null && semPeriodo && semDescricao) {
            return movimentacaoFinanceiraRepository
                    .findByTipoAndCategoriaIgnoreCaseOrderByDataMovimentacaoDescIdMovimentacaoDesc(tipo, categoriaFiltrada);
        }

        if (tipo != null && situacao == null && idCategoriaDespesa != null
                && categoriaFiltrada == null && semPeriodo && semDescricao) {
            return movimentacaoFinanceiraRepository
                    .findByTipoAndCategoriaDespesa_IdCategoriaOrderByDataMovimentacaoDescIdMovimentacaoDesc(tipo, idCategoriaDespesa);
        }

        if (tipo == null && situacao == null && semCategoria && !semPeriodo && semDescricao) {
            return listarPorPeriodo(dataInicio, dataFim);
        }

        if (tipo != null && situacao == null && semCategoria && !semPeriodo && semDescricao) {
            return tipo == TipoMovimentacaoFinanceira.RECEITA
                    ? listarReceitasPorPeriodo(dataInicio, dataFim)
                    : listarDespesasPorPeriodo(dataInicio, dataFim);
        }

        if (tipo == null && situacao == null && semCategoria && semPeriodo && descricaoFiltrada != null) {
            return movimentacaoFinanceiraRepository
                    .findByDescricaoContainingIgnoreCaseOrderByDataMovimentacaoDescIdMovimentacaoDesc(descricaoFiltrada);
        }

        Specification<MovimentacaoFinanceira> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (tipo != null) {
                predicates.add(cb.equal(root.get("tipo"), tipo));
            }
            if (situacao != null) {
                predicates.add(cb.equal(root.get("situacao"), situacao));
            }
            if (idCategoriaDespesa != null) {
                predicates.add(cb.equal(root.get("categoriaDespesa").get("idCategoria"), idCategoriaDespesa));
            }
            if (categoriaFiltrada != null) {
                predicates.add(cb.equal(
                        cb.lower(cb.trim(root.get("categoria"))),
                        categoriaFiltrada.toLowerCase(Locale.ROOT)
                ));
            }
            if (dataInicio != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("dataMovimentacao"), dataInicio));
            }
            if (dataFim != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("dataMovimentacao"), dataFim));
            }
            if (descricaoFiltrada != null) {
                predicates.add(cb.like(
                        cb.lower(root.get("descricao")),
                        "%" + descricaoFiltrada.toLowerCase(Locale.ROOT) + "%"
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return movimentacaoFinanceiraRepository.findAll(spec, ORDENACAO_PADRAO);
    }

    private void validarPeriodo(LocalDate dataInicio, LocalDate dataFim) {
        if (dataInicio != null && dataFim != null && dataFim.isBefore(dataInicio)) {
            throw new IllegalArgumentException("Data final não pode ser anterior à data inicial.");
        }
    }

    private BigDecimal normalizarValorMonetario(BigDecimal valor) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return valor.setScale(2, RoundingMode.HALF_UP);
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
