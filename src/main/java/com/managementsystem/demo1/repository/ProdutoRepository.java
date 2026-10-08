package com.managementsystem.demo1.repository;

import com.managementsystem.demo1.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;



public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    /**
     * Carrega todos os produtos já com os materiais inicializados (JOIN FETCH).
     * Evita LazyInitializationException fora de transação no JavaFX.
     */
    @Query("SELECT DISTINCT p FROM Produto p LEFT JOIN FETCH p.materiaisUtilizados pm LEFT JOIN FETCH pm.material")
    List<Produto> findAllWithMateriais();

    /**
     * Carrega um produto específico com materiais.
     */
    @Query("SELECT p FROM Produto p LEFT JOIN FETCH p.materiaisUtilizados pm LEFT JOIN FETCH pm.material WHERE p.idProduto = :id")
    Optional<Produto> findByIdWithMateriais(@Param("id") Long id);

    Optional<Produto> findByCodigo(String codigo);
}
