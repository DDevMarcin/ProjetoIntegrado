package com.managementsystem.demo1.repository;

import com.managementsystem.demo1.model.Produto;
import com.managementsystem.demo1.model.TipoProduto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    // ── Buscas com JOIN FETCH (necessárias para a GUI JavaFX evitar LazyInitializationException) ──

    @Query("SELECT DISTINCT p FROM Produto p LEFT JOIN FETCH p.materiaisUtilizados pm LEFT JOIN FETCH pm.material")
    List<Produto> findAllWithMateriais();

    @Query("SELECT p FROM Produto p LEFT JOIN FETCH p.materiaisUtilizados pm LEFT JOIN FETCH pm.material WHERE p.idProduto = :id")
    Optional<Produto> findByIdWithMateriais(@Param("id") Long id);

    // ── Buscas padrão (usadas pelo backend / ControleEstoque) ──

    List<Produto> findByNomeContainingIgnoreCase(String nome);
    List<Produto> findByTipo(TipoProduto tipo);
    List<Produto> findByTipoAndNomeContainingIgnoreCase(TipoProduto tipo, String nome);
    Optional<Produto> findByIdProdutoAndTipo(Long idProduto, TipoProduto tipo);
    Optional<Produto> findByCodigo(String codigo);

    // ── Verificações de unicidade ──

    boolean existsByCodigo(String codigo);
    boolean existsByCodigoAndIdProdutoNot(String codigo, Long idProduto);
    boolean existsByNomeIgnoreCase(String nome);
    boolean existsByNomeIgnoreCaseAndIdProdutoNot(String nome, Long idProduto);

    // ── Agregações ──

    @Query("SELECT COALESCE(SUM(p.quantidadeEstoque), 0) FROM Produto p WHERE p.quantidadeEstoque IS NOT NULL")
    Long sumQuantidadeEstoque();
}
