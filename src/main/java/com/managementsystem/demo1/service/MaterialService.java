package com.managementsystem.demo1.service;

import com.managementsystem.demo1.model.Material;
import com.managementsystem.demo1.repository.MaterialRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class MaterialService {

    private final MaterialRepository materialRepository;

    public MaterialService(MaterialRepository materialRepository) {
        this.materialRepository = materialRepository;
    }

    @Transactional
    public Material salvar(Material material) {
        validar(material);
        return materialRepository.save(material);
    }

    public List<Material> listarTodos() {
        return materialRepository.findAll();
    }

    public Optional<Material> buscarPorId(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return materialRepository.findById(id);
    }

    public List<Material> buscarPorNome(String nome) {
        if (nome == null || nome.isBlank()) {
            return listarTodos();
        }
        return materialRepository.findByNomeContainingIgnoreCase(nome.trim());
    }

    public List<Material> consultar(String nome, String unidadeMedida) {
        boolean temNome = nome != null && !nome.isBlank();
        boolean temUnidade = unidadeMedida != null && !unidadeMedida.isBlank();

        if (temNome && temUnidade) {
            return materialRepository.findByNomeContainingIgnoreCaseAndUnidadeMedidaIgnoreCase(
                    nome.trim(), unidadeMedida.trim()
            );
        } else if (temNome) {
            return materialRepository.findByNomeContainingIgnoreCase(nome.trim());
        } else if (temUnidade) {
            return materialRepository.findByUnidadeMedidaIgnoreCase(unidadeMedida.trim());
        } else {
            return listarTodos();
        }
    }

    private void validar(Material material) {
        if (material.getNome() == null || material.getNome().isBlank()) {
            throw new IllegalArgumentException("Nome do material é obrigatório.");
        }
        if (material.getPrecoUnidade() == null || material.getPrecoUnidade().signum() < 0) {
            throw new IllegalArgumentException("Preço do material inválido.");
        }
        if (material.getUnidadeMedida() == null || material.getUnidadeMedida().isBlank()) {
            throw new IllegalArgumentException("Unidade de medida é obrigatória.");
        }
    }
}