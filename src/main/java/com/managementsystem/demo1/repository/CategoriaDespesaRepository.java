package com.managementsystem.demo1.repository;

import com.managementsystem.demo1.model.CategoriaDespesa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoriaDespesaRepository extends JpaRepository<CategoriaDespesa, Long> {

    Optional<CategoriaDespesa> findByNomeIgnoreCase(String nome);

    boolean existsByNomeIgnoreCase(String nome);

    boolean existsByNomeIgnoreCaseAndIdCategoriaNot(String nome, Long idCategoria);

    List<CategoriaDespesa> findByNomeContainingIgnoreCaseOrderByNomeAsc(String nome);

    List<CategoriaDespesa> findAllByOrderByNomeAsc();
}

