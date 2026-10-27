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
        material.setNome(material.getNome().trim());
        material.setUnidadeMedida(material.getUnidadeMedida().trim());
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
        String nomeFiltrado = (nome != null && !nome.isBlank()) ? nome.trim() : null;
        String unidadeFiltrada = (unidadeMedida != null && !unidadeMedida.isBlank()) ? unidadeMedida.trim() : null;

        if (nomeFiltrado != null && unidadeFiltrada != null) {
            return materialRepository.findByNomeContainingIgnoreCaseAndUnidadeMedidaIgnoreCase(
                    nomeFiltrado, unidadeFiltrada
            );
        } else if (nomeFiltrado != null) {
            return materialRepository.findByNomeContainingIgnoreCase(nomeFiltrado);
        } else if (unidadeFiltrada != null) {
            return materialRepository.findByUnidadeMedidaIgnoreCase(unidadeFiltrada);
        } else {
            return listarTodos();
        }
    }

    private void validar(Material material) {
        if (material == null) {
            throw new IllegalArgumentException("Material não pode ser nulo.");
        }
        if (material.getNome() == null || material.getNome().isBlank()) {
            throw new IllegalArgumentException("Nome do material é obrigatório.");
        }
        if (material.getPrecoUnidade() == null || material.getPrecoUnidade().signum() < 0) {
            throw new IllegalArgumentException("Preço do material inválido.");
        }
        if (material.getUnidadeMedida() == null || material.getUnidadeMedida().isBlank()) {
            throw new IllegalArgumentException("Unidade de medida é obrigatória.");
        }
        if (material.getUnidadeMedida().trim().length() > 10) {
            throw new IllegalArgumentException("Unidade de medida não pode ultrapassar 10 caracteres.");
        }
    }
}