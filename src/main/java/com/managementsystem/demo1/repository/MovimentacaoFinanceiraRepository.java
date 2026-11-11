package com.managementsystem.demo1.repository;

import com.managementsystem.demo1.model.MovimentacaoFinanceira;
import com.managementsystem.demo1.model.SituacaoMovimentacaoFinanceira;
import com.managementsystem.demo1.model.TipoMovimentacaoFinanceira;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface MovimentacaoFinanceiraRepository
        extends JpaRepository<MovimentacaoFinanceira, Long>, JpaSpecificationExecutor<MovimentacaoFinanceira> {

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

    List<MovimentacaoFinanceira> findByDataMovimentacaoBetweenOrderByDataMovimentacaoDescIdMovimentacaoDesc(
            LocalDate dataInicio,
            LocalDate dataFim
    );

    List<MovimentacaoFinanceira> findByDataMovimentacaoGreaterThanEqualOrderByDataMovimentacaoDescIdMovimentacaoDesc(
            LocalDate dataInicio
    );

    List<MovimentacaoFinanceira> findByDataMovimentacaoLessThanEqualOrderByDataMovimentacaoDescIdMovimentacaoDesc(
            LocalDate dataFim
    );

    List<MovimentacaoFinanceira> findByTipoAndDataMovimentacaoBetweenOrderByDataMovimentacaoDescIdMovimentacaoDesc(
            TipoMovimentacaoFinanceira tipo,
            LocalDate dataInicio,
            LocalDate dataFim
    );

    List<MovimentacaoFinanceira> findByTipoAndDataMovimentacaoGreaterThanEqualOrderByDataMovimentacaoDescIdMovimentacaoDesc(
            TipoMovimentacaoFinanceira tipo,
            LocalDate dataInicio
    );

    List<MovimentacaoFinanceira> findByTipoAndDataMovimentacaoLessThanEqualOrderByDataMovimentacaoDescIdMovimentacaoDesc(
            TipoMovimentacaoFinanceira tipo,
            LocalDate dataFim
    );

    List<MovimentacaoFinanceira> findByDescricaoContainingIgnoreCaseOrderByDataMovimentacaoDescIdMovimentacaoDesc(String descricao);

    List<MovimentacaoFinanceira> findByCategoriaDespesa_IdCategoriaOrderByDataMovimentacaoDescIdMovimentacaoDesc(Long idCategoria);

    List<MovimentacaoFinanceira> findByTipoAndCategoriaDespesa_IdCategoriaOrderByDataMovimentacaoDescIdMovimentacaoDesc(
            TipoMovimentacaoFinanceira tipo,
            Long idCategoria
    );

    boolean existsByCategoriaDespesa_IdCategoria(Long idCategoria);

    boolean existsByTipoAndCategoriaIgnoreCase(TipoMovimentacaoFinanceira tipo, String categoria);

    @Query("SELECT COALESCE(SUM(m.valor), 0) FROM MovimentacaoFinanceira m WHERE m.tipo = :tipo")
    BigDecimal somarValorPorTipo(@Param("tipo") TipoMovimentacaoFinanceira tipo);

    @Query("SELECT COALESCE(SUM(m.valor), 0) FROM MovimentacaoFinanceira m WHERE m.tipo = :tipo AND m.dataMovimentacao BETWEEN :dataInicio AND :dataFim")
    BigDecimal somarValorPorTipoEPeriodo(
            @Param("tipo") TipoMovimentacaoFinanceira tipo,
            @Param("dataInicio") LocalDate dataInicio,
            @Param("dataFim") LocalDate dataFim
    );

    @Query("SELECT COALESCE(SUM(m.valor), 0) FROM MovimentacaoFinanceira m WHERE m.tipo = :tipo AND m.dataMovimentacao >= :dataInicio")
    BigDecimal somarValorPorTipoEDataMovimentacaoMaiorOuIgual(
            @Param("tipo") TipoMovimentacaoFinanceira tipo,
            @Param("dataInicio") LocalDate dataInicio
    );

    @Query("SELECT COALESCE(SUM(m.valor), 0) FROM MovimentacaoFinanceira m WHERE m.tipo = :tipo AND m.dataMovimentacao <= :dataFim")
    BigDecimal somarValorPorTipoEDataMovimentacaoMenorOuIgual(
            @Param("tipo") TipoMovimentacaoFinanceira tipo,
            @Param("dataFim") LocalDate dataFim
    );

    @Query("SELECT COALESCE(SUM(m.valor), 0) FROM MovimentacaoFinanceira m WHERE m.tipo = :tipo AND m.situacao = :situacao")
    BigDecimal somarValorPorTipoESituacao(
            @Param("tipo") TipoMovimentacaoFinanceira tipo,
            @Param("situacao") SituacaoMovimentacaoFinanceira situacao
    );

    @Query("SELECT COALESCE(SUM(m.valor), 0) FROM MovimentacaoFinanceira m WHERE m.tipo = :tipo AND m.situacao = :situacao AND m.dataMovimentacao BETWEEN :dataInicio AND :dataFim")
    BigDecimal somarValorPorTipoESituacaoEPeriodo(
            @Param("tipo") TipoMovimentacaoFinanceira tipo,
            @Param("situacao") SituacaoMovimentacaoFinanceira situacao,
            @Param("dataInicio") LocalDate dataInicio,
            @Param("dataFim") LocalDate dataFim
    );

    @Query("SELECT COALESCE(SUM(m.valor), 0) FROM MovimentacaoFinanceira m WHERE m.tipo = :tipo AND m.situacao = :situacao AND m.dataMovimentacao >= :dataInicio")
    BigDecimal somarValorPorTipoESituacaoEDataMovimentacaoMaiorOuIgual(
            @Param("tipo") TipoMovimentacaoFinanceira tipo,
            @Param("situacao") SituacaoMovimentacaoFinanceira situacao,
            @Param("dataInicio") LocalDate dataInicio
    );

    @Query("SELECT COALESCE(SUM(m.valor), 0) FROM MovimentacaoFinanceira m WHERE m.tipo = :tipo AND m.situacao = :situacao AND m.dataMovimentacao <= :dataFim")
    BigDecimal somarValorPorTipoESituacaoEDataMovimentacaoMenorOuIgual(
            @Param("tipo") TipoMovimentacaoFinanceira tipo,
            @Param("situacao") SituacaoMovimentacaoFinanceira situacao,
            @Param("dataFim") LocalDate dataFim
    );

    @Query("""
        SELECT
            m.categoria AS categoria,
            SUM(m.valor) AS total
        FROM MovimentacaoFinanceira m
        WHERE m.tipo = :tipo
        GROUP BY m.categoria
        ORDER BY m.categoria
    """)
    List<ResumoDespesaCategoriaProjection> resumirDespesasPorCategoria(
            @Param("tipo") TipoMovimentacaoFinanceira tipo
    );

    @Query("""
    SELECT
        m.categoria AS categoria,
        SUM(m.valor) AS total
    FROM MovimentacaoFinanceira m
    WHERE m.tipo = :tipo
      AND m.dataMovimentacao BETWEEN :dataInicio AND :dataFim
    GROUP BY m.categoria
    ORDER BY m.categoria
""")
    List<ResumoDespesaCategoriaProjection> resumirDespesasPorCategoriaEPeriodo(
            @Param("tipo") TipoMovimentacaoFinanceira tipo,
            @Param("dataInicio") LocalDate dataInicio,
            @Param("dataFim") LocalDate dataFim
    );

    List<MovimentacaoFinanceira> findByTipoAndSituacaoAndDataVencimentoBetweenOrderByDataVencimentoAscIdMovimentacaoAsc(
            TipoMovimentacaoFinanceira tipo,
            SituacaoMovimentacaoFinanceira situacao,
            LocalDate dataInicio,
            LocalDate dataFim
    );
}
