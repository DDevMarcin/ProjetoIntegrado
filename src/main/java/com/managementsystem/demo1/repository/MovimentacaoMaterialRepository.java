package com.managementsystem.demo1.repository;

import com.managementsystem.demo1.model.MovimentacaoMaterial;
import com.managementsystem.demo1.model.TipoMovimentacaoMaterial;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovimentacaoMaterialRepository extends JpaRepository<MovimentacaoMaterial, Long> {

    List<MovimentacaoMaterial> findByMaterial_IdMaterialOrderByIdMovimentacaoAsc(Long idMaterial);

    List<MovimentacaoMaterial> findByTipoOrderByIdMovimentacaoAsc(TipoMovimentacaoMaterial tipo);

    List<MovimentacaoMaterial> findByMaterial_IdMaterialAndTipoOrderByIdMovimentacaoAsc(Long idMaterial, TipoMovimentacaoMaterial tipo);

    void deleteByMaterial_IdMaterial(Long idMaterial);
}

