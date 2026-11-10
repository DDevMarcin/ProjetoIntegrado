package com.managementsystem.demo1.repository;

import com.managementsystem.demo1.model.MovimentacaoFinanceira;
import com.managementsystem.demo1.model.SituacaoMovimentacaoFinanceira;
import com.managementsystem.demo1.model.TipoMovimentacaoFinanceira;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface MovimentacaoFinanceiraRepository extends JpaRepository<MovimentacaoFinanceira, Long> {

    List<MovimentacaoFinanceira> findAllByOrderByDataMovimentacaoDescIdMovimentacaoDesc();

    List<MovimentacaoFinanceira> findByTipoOrderByDataMovimentacaoDescIdMovimentacaoDesc(TipoMovimentacaoFinanceira tipo);

    List<MovimentacaoFinanceira> findBySituacaoOrderByDataMovimentacaoDescIdMovimentacaoDesc(SituacaoMovimentacaoFinanceira situacao);

    List<MovimentacaoFinanceira> findByCategoriaIgnoreCaseOrderByDataMovimentacaoDescIdMovimentacaoDesc(String categoria);

    List<MovimentacaoFinanceira> findByTipoAndSituacaoOrderByDataMovimentacaoDescIdMovimentacaoDesc(
            TipoMovimentacaoFinanceira tipo,
            SituacaoMovimentacaoFinanceira situacao
    );

    List<MovimentacaoFinanceira> findByTipoAndCategoriaIgnoreCaseOrderByDataMovimentacaoDescIdMovimentacaoDesc(
            TipoMovimentacaoFinanceira tipo,
            String categoria
    );

    List<MovimentacaoFinanceira> findByDataMovimentacaoBetweenOrderByDataMovimentacaoDescIdMovimentacaoDesc(LocalDate dataInicio, LocalDate dataFim);

    List<MovimentacaoFinanceira> findByDescricaoContainingIgnoreCaseOrderByDataMovimentacaoDescIdMovimentacaoDesc(String descricao);

    List<MovimentacaoFinanceira> findByCategoriaDespesa_IdCategoriaOrderByDataMovimentacaoDescIdMovimentacaoDesc(Long idCategoria);

    List<MovimentacaoFinanceira> findByTipoAndCategoriaDespesa_IdCategoriaOrderByDataMovimentacaoDescIdMovimentacaoDesc(
            TipoMovimentacaoFinanceira tipo,
            Long idCategoria
    );

    boolean existsByCategoriaDespesa_IdCategoria(Long idCategoria);

    boolean existsByTipoAndCategoriaIgnoreCase(TipoMovimentacaoFinanceira tipo, String categoria);
}

