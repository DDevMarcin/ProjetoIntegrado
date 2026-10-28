package com.managementsystem.demo1.service;

import com.managementsystem.demo1.model.Material;
import com.managementsystem.demo1.repository.MaterialRepository;
import com.managementsystem.demo1.repository.ProdutoMaterialRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class MaterialService {

    private final MaterialRepository materialRepository;
    private final ProdutoMaterialRepository produtoMaterialRepository;

    public MaterialService(MaterialRepository materialRepository, ProdutoMaterialRepository produtoMaterialRepository) {
        this.materialRepository = materialRepository;
        this.produtoMaterialRepository = produtoMaterialRepository;
    }

    @Transactional
    public Material salvar(Material material) {
        validar(material);
        if (material.getIdMaterial() != null && !materialRepository.existsById(material.getIdMaterial())) {
            throw new NoSuchElementException("Material não encontrado: id " + material.getIdMaterial());
        }
        material.setNome(material.getNome().trim());
        material.setUnidadeMedida(material.getUnidadeMedida().trim());
        return materialRepository.save(material);
    }

    @Transactional
    public Material salvar(String nome, BigDecimal precoUnidade, String unidadeMedida) {
        Material material = new Material(nome, precoUnidade, unidadeMedida);
        return salvar(material);
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

    @Transactional
    public Material atualizar(Long id, String nome, BigDecimal precoUnidade, String unidadeMedida) {
        if (id == null) {
            throw new IllegalArgumentException("ID do material é obrigatório.");
        }
        Material material = buscarPorId(id)
                .orElseThrow(() -> new NoSuchElementException("Material não encontrado: id " + id));

        Material dados = new Material(nome, precoUnidade, unidadeMedida);
        validar(dados);

        material.setNome(dados.getNome().trim());
        material.setPrecoUnidade(dados.getPrecoUnidade());
        material.setUnidadeMedida(dados.getUnidadeMedida().trim());

        return materialRepository.save(material);
    }

    @Transactional
    public Material atualizar(Long id, Material dadosAtualizados) {
        if (dadosAtualizados == null) {
            throw new IllegalArgumentException("Material não pode ser nulo.");
        }
        return atualizar(id, dadosAtualizados.getNome(), dadosAtualizados.getPrecoUnidade(), dadosAtualizados.getUnidadeMedida());
    }

    @Transactional
    public void excluir(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID do material é obrigatório.");
        }
        if (!materialRepository.existsById(id)) {
            throw new NoSuchElementException("Material não encontrado: id " + id);
        }
        if (produtoMaterialRepository != null && produtoMaterialRepository.existsByMaterial_IdMaterial(id)) {
            throw new IllegalStateException("Não é possível excluir o material pois ele está vinculado a um ou mais produtos.");
        }
        materialRepository.deleteById(id);
    }

    private void validar(Material material) {
        if (material == null) {
            throw new IllegalArgumentException("Material não pode ser nulo.");
        }
        if (material.getNome() == null || material.getNome().isBlank()) {
            throw new IllegalArgumentException("Nome do material é obrigatório.");
        }
        if (material.getNome().trim().length() > 255) {
            throw new IllegalArgumentException("Nome do material não pode ultrapassar 255 caracteres.");
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