package com.managementsystem.demo1.repository;

import com.managementsystem.demo1.model.Produto;
import com.managementsystem.demo1.model.TipoProduto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    List<Produto> findByNomeContainingIgnoreCase(String nome);

    List<Produto> findByTipo(TipoProduto tipo);

    List<Produto> findByTipoAndNomeContainingIgnoreCase(TipoProduto tipo, String nome);

    Optional<Produto> findByIdProdutoAndTipo(Long idProduto, TipoProduto tipo);

    boolean existsByCodigo(String codigo);

    boolean existsByCodigoAndIdProdutoNot(String codigo, Long idProduto);

    boolean existsByNomeIgnoreCase(String nome);

    boolean existsByNomeIgnoreCaseAndIdProdutoNot(String nome, Long idProduto);

    @Query("SELECT COALESCE(SUM(p.quantidadeEstoque), 0) FROM Produto p WHERE p.quantidadeEstoque IS NOT NULL")
    Long sumQuantidadeEstoque();
}
