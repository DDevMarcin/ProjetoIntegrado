package com.managementsystem.demo1.repository;

import com.managementsystem.demo1.model.Material;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MaterialRepository extends JpaRepository<Material, Long> {

    List<Material> findByNomeContainingIgnoreCase(String nome);

    List<Material> findByUnidadeMedidaIgnoreCase(String unidadeMedida);

    List<Material> findByNomeContainingIgnoreCaseAndUnidadeMedidaIgnoreCase(String nome, String unidadeMedida);
}