package com.managementsystem.demo1.service;

import com.managementsystem.demo1.model.Material;
import com.managementsystem.demo1.model.MovimentacaoMaterial;
import com.managementsystem.demo1.model.TipoMovimentacaoMaterial;
import com.managementsystem.demo1.repository.MaterialRepository;
import com.managementsystem.demo1.repository.MovimentacaoMaterialRepository;
import com.managementsystem.demo1.repository.ProdutoMaterialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class MaterialService {

    private final MaterialRepository materialRepository;
    private final ProdutoMaterialRepository produtoMaterialRepository;
    private final MovimentacaoMaterialRepository movimentacaoMaterialRepository;

    public MaterialService(MaterialRepository materialRepository,
                           ProdutoMaterialRepository produtoMaterialRepository) {
        this(materialRepository, produtoMaterialRepository, null);
    }

    @Autowired
    public MaterialService(MaterialRepository materialRepository,
                           ProdutoMaterialRepository produtoMaterialRepository,
                           MovimentacaoMaterialRepository movimentacaoMaterialRepository) {
        this.materialRepository = materialRepository;
        this.produtoMaterialRepository = produtoMaterialRepository;
        this.movimentacaoMaterialRepository = movimentacaoMaterialRepository;
    }

    @Transactional
    public Material salvar(Material material) {
        validar(material);

        String nomeTrim = material.getNome().trim();
        String corTrim = material.getCor().trim();

        if (material.getIdMaterial() != null) {
            if (!materialRepository.existsById(material.getIdMaterial())) {
                throw new NoSuchElementException(
                        "Material não encontrado: id " + material.getIdMaterial()
                );
            }

            if (existeOutroMaterialComMesmoNome(nomeTrim, material.getIdMaterial())) {
                throw new IllegalArgumentException(
                        "Já existe outro material cadastrado com o nome informado: "
                                + nomeTrim
                );
            }
        } else {
            if (existeMaterialComMesmoNome(nomeTrim)) {
                throw new IllegalArgumentException(
                        "Já existe um material cadastrado com o nome informado: "
                                + nomeTrim
                );
            }
        }

        material.setNome(nomeTrim);
        material.setCor(corTrim);
        material.setUnidadeMedida(material.getUnidadeMedida().trim());

        if (material.getQuantidadeEstoque() == null) {
            material.setQuantidadeEstoque(BigDecimal.ZERO);
        }

        return materialRepository.save(material);
    }

    @Transactional
    public Material salvar(String nome,
                           String cor,
                           BigDecimal precoUnidade,
                           String unidadeMedida) {

        Material material = new Material(
                nome,
                cor,
                precoUnidade,
                unidadeMedida
        );

        return salvar(material);
    }

    @Transactional
    public Material salvar(String nome,
                           String cor,
                           BigDecimal precoUnidade,
                           String unidadeMedida,
                           BigDecimal quantidadeEstoque) {

        Material material = new Material(
                nome,
                cor,
                precoUnidade,
                unidadeMedida,
                quantidadeEstoque
        );

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

        String nomeFiltrado =
                (nome != null && !nome.isBlank())
                        ? nome.trim()
                        : null;

        String unidadeFiltrada =
                (unidadeMedida != null && !unidadeMedida.isBlank())
                        ? unidadeMedida.trim()
                        : null;

        if (nomeFiltrado != null && unidadeFiltrada != null) {

            return materialRepository
                    .findByNomeContainingIgnoreCaseAndUnidadeMedidaIgnoreCase(
                            nomeFiltrado,
                            unidadeFiltrada
                    );

        } else if (nomeFiltrado != null) {

            return materialRepository
                    .findByNomeContainingIgnoreCase(nomeFiltrado);

        } else if (unidadeFiltrada != null) {

            return materialRepository
                    .findByUnidadeMedidaIgnoreCase(unidadeFiltrada);

        } else {

            return listarTodos();
        }
    }

    @Transactional
    public Material atualizar(Long id,
                              String nome,
                              String cor,
                              BigDecimal precoUnidade,
                              String unidadeMedida) {

        if (id == null) {
            throw new IllegalArgumentException(
                    "ID do material é obrigatório."
            );
        }

        Material material = buscarPorId(id)
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "Material não encontrado: id " + id
                        )
                );

        Material dados = new Material(
                nome,
                cor,
                precoUnidade,
                unidadeMedida
        );

        validar(dados);

        String nomeTrim = dados.getNome().trim();

        if (existeOutroMaterialComMesmoNome(nomeTrim, id)) {
            throw new IllegalArgumentException(
                    "Já existe outro material cadastrado com o nome informado: "
                            + nomeTrim
            );
        }

        material.setNome(nomeTrim);
        material.setCor(dados.getCor().trim());
        material.setPrecoUnidade(dados.getPrecoUnidade());
        material.setUnidadeMedida(dados.getUnidadeMedida().trim());

        return materialRepository.save(material);
    }

    @Transactional
    public Material atualizar(Long id, Material dadosAtualizados) {

        if (dadosAtualizados == null) {
            throw new IllegalArgumentException(
                    "Material não pode ser nulo."
            );
        }

        return atualizar(
                id,
                dadosAtualizados.getNome(),
                dadosAtualizados.getCor(),
                dadosAtualizados.getPrecoUnidade(),
                dadosAtualizados.getUnidadeMedida()
        );
    }

    @Transactional
    public void excluir(Long id) {

        if (id == null) {
            throw new IllegalArgumentException(
                    "ID do material é obrigatório."
            );
        }

        if (!materialRepository.existsById(id)) {
            throw new NoSuchElementException(
                    "Material não encontrado: id " + id
            );
        }

        if (produtoMaterialRepository != null
                && produtoMaterialRepository.existsByMaterial_IdMaterial(id)) {

            throw new IllegalStateException(
                    "Não é possível excluir o material pois ele está "
                            + "vinculado a um ou mais produtos."
            );
        }

        if (movimentacaoMaterialRepository != null) {
            movimentacaoMaterialRepository
                    .deleteByMaterial_IdMaterial(id);
        }

        materialRepository.deleteById(id);
    }

    /**
     * Registra uma movimentação de estoque para um material
     * (ENTRADA, SAIDA ou PERDA), atualizando o saldo do material
     * e gravando o histórico de forma atômica.
     */
    @Transactional
    public MovimentacaoMaterial registrarMovimentacao(
            Long idMaterial,
            TipoMovimentacaoMaterial tipo,
            BigDecimal quantidade,
            String motivo,
            String observacoes) {

        if (idMaterial == null) {
            throw new IllegalArgumentException(
                    "ID do material é obrigatório."
            );
        }

        if (tipo == null) {
            throw new IllegalArgumentException(
                    "Tipo de movimentação é obrigatório."
            );
        }

        if (quantidade == null || quantidade.signum() <= 0) {
            throw new IllegalArgumentException(
                    "Quantidade movimentada deve ser maior que zero."
            );
        }

        String motivoTrim = validarMotivo(tipo, motivo);
        String observacoesTrim = validarObservacoes(observacoes);

        Material material = materialRepository.findById(idMaterial)
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "Material não encontrado: id " + idMaterial
                        )
                );

        BigDecimal saldoAnterior =
                material.getQuantidadeEstoque() != null
                        ? material.getQuantidadeEstoque()
                        : BigDecimal.ZERO;

        BigDecimal saldoAtual;

        if (tipo == TipoMovimentacaoMaterial.ENTRADA) {

            saldoAtual = saldoAnterior.add(quantidade);

        } else {

            saldoAtual = saldoAnterior.subtract(quantidade);

            if (saldoAtual.signum() < 0) {

                if (tipo == TipoMovimentacaoMaterial.PERDA) {
                    throw new IllegalArgumentException(
                            "Saldo insuficiente em estoque para registrar "
                                    + "a perda do material."
                    );
                }

                throw new IllegalArgumentException(
                        "Saldo insuficiente em estoque para realizar "
                                + "a saída do material."
                );
            }
        }

        material.setQuantidadeEstoque(saldoAtual);

        Material materialAtualizado =
                materialRepository.save(material);

        MovimentacaoMaterial movimentacao =
                new MovimentacaoMaterial(
                        materialAtualizado,
                        tipo,
                        quantidade,
                        saldoAnterior,
                        saldoAtual,
                        motivoTrim,
                        observacoesTrim,
                        LocalDateTime.now()
                );

        return movimentacaoMaterialRepository != null
                ? movimentacaoMaterialRepository.save(movimentacao)
                : movimentacao;
    }

    /**
     * Registra uma perda de material no estoque, reutilizando
     * a mesma lógica transacional de atualização de saldo e
     * persistência do histórico.
     */
    @Transactional
    public MovimentacaoMaterial registrarPerda(
            Long idMaterial,
            BigDecimal quantidade,
            String motivo,
            String observacoes) {

        if (idMaterial == null) {
            throw new IllegalArgumentException(
                    "ID do material é obrigatório."
            );
        }

        if (quantidade == null || quantidade.signum() <= 0) {
            throw new IllegalArgumentException(
                    "Quantidade perdida deve ser maior que zero."
            );
        }

        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException(
                    "Motivo da perda é obrigatório."
            );
        }

        return registrarMovimentacao(
                idMaterial,
                TipoMovimentacaoMaterial.PERDA,
                quantidade,
                motivo,
                observacoes
        );
    }

    public List<MovimentacaoMaterial> listarMovimentacoes() {

        if (movimentacaoMaterialRepository == null) {
            return List.of();
        }

        return movimentacaoMaterialRepository.findAll();
    }

    public List<MovimentacaoMaterial> listarMovimentacoesPorMaterial(
            Long idMaterial) {

        if (idMaterial == null) {
            throw new IllegalArgumentException(
                    "ID do material é obrigatório."
            );
        }

        if (!materialRepository.existsById(idMaterial)) {
            throw new NoSuchElementException(
                    "Material não encontrado: id " + idMaterial
            );
        }

        if (movimentacaoMaterialRepository == null) {
            return List.of();
        }

        return movimentacaoMaterialRepository
                .findByMaterial_IdMaterialOrderByIdMovimentacaoAsc(idMaterial);
    }

    public List<MovimentacaoMaterial> listarMovimentacoesPorTipo(
            TipoMovimentacaoMaterial tipo) {

        if (tipo == null) {
            return listarMovimentacoes();
        }

        if (movimentacaoMaterialRepository == null) {
            return List.of();
        }

        return movimentacaoMaterialRepository
                .findByTipoOrderByIdMovimentacaoAsc(tipo);
    }

    public List<MovimentacaoMaterial> listarPerdas() {
        return listarMovimentacoesPorTipo(
                TipoMovimentacaoMaterial.PERDA
        );
    }

    public List<MovimentacaoMaterial> listarPerdasPorMaterial(
            Long idMaterial) {

        if (idMaterial == null) {
            throw new IllegalArgumentException(
                    "ID do material é obrigatório."
            );
        }

        if (!materialRepository.existsById(idMaterial)) {
            throw new NoSuchElementException(
                    "Material não encontrado: id " + idMaterial
            );
        }

        if (movimentacaoMaterialRepository == null) {
            return List.of();
        }

        return movimentacaoMaterialRepository
                .findByMaterial_IdMaterialAndTipoOrderByIdMovimentacaoAsc(
                        idMaterial,
                        TipoMovimentacaoMaterial.PERDA
                );
    }

    private String validarMotivo(
            TipoMovimentacaoMaterial tipo,
            String motivo) {

        if (tipo == TipoMovimentacaoMaterial.PERDA
                && (motivo == null || motivo.isBlank())) {

            throw new IllegalArgumentException(
                    "Motivo da perda é obrigatório."
            );
        }

        if (motivo == null || motivo.isBlank()) {
            return null;
        }

        String motivoTrim = motivo.trim();

        if (motivoTrim.length() > 255) {
            throw new IllegalArgumentException(
                    "Motivo não pode ultrapassar 255 caracteres."
            );
        }

        return motivoTrim;
    }

    private String validarObservacoes(String observacoes) {

        if (observacoes == null || observacoes.isBlank()) {
            return null;
        }

        String obsTrim = observacoes.trim();

        if (obsTrim.length() > 500) {
            throw new IllegalArgumentException(
                    "Observações não podem ultrapassar 500 caracteres."
            );
        }

        return obsTrim;
    }

    private void validar(Material material) {

        if (material == null) {
            throw new IllegalArgumentException(
                    "Material não pode ser nulo."
            );
        }

        if (material.getNome() == null || material.getNome().isBlank()) {
            throw new IllegalArgumentException(
                    "Nome do material é obrigatório."
            );
        }

        if (material.getNome().trim().length() > 255) {
            throw new IllegalArgumentException(
                    "Nome do material não pode ultrapassar 255 caracteres."
            );
        }

        if (material.getCor() == null || material.getCor().isBlank()) {
            throw new IllegalArgumentException(
                    "Cor do material é obrigatória."
            );
        }

        if (material.getCor().trim().length() > 50) {
            throw new IllegalArgumentException(
                    "Cor do material não pode ultrapassar 50 caracteres."
            );
        }

        if (material.getPrecoUnidade() == null
                || material.getPrecoUnidade().signum() < 0) {

            throw new IllegalArgumentException(
                    "Preço do material inválido."
            );
        }

        if (material.getUnidadeMedida() == null
                || material.getUnidadeMedida().isBlank()) {

            throw new IllegalArgumentException(
                    "Unidade de medida é obrigatória."
            );
        }

        if (material.getUnidadeMedida().trim().length() > 10) {
            throw new IllegalArgumentException(
                    "Unidade de medida não pode ultrapassar 10 caracteres."
            );
        }

        if (material.getQuantidadeEstoque() != null
                && material.getQuantidadeEstoque().signum() < 0) {

            throw new IllegalArgumentException(
                    "A quantidade em estoque do material não pode ser negativa."
            );
        }
    }

    private boolean existeMaterialComMesmoNome(String nomeTrim) {

        return materialRepository.existsByNomeIgnoreCase(nomeTrim)
                || materialRepository.findAll().stream()
                .anyMatch(m ->
                        m.getNome() != null
                                && m.getNome()
                                .trim()
                                .equalsIgnoreCase(nomeTrim)
                );
    }

    private boolean existeOutroMaterialComMesmoNome(
            String nomeTrim,
            Long idIgnorado) {

        return materialRepository
                .existsByNomeIgnoreCaseAndIdMaterialNot(
                        nomeTrim,
                        idIgnorado
                )
                || materialRepository.findAll().stream()
                .anyMatch(m ->
                        m.getIdMaterial() != null
                                && !m.getIdMaterial().equals(idIgnorado)
                                && m.getNome() != null
                                && m.getNome()
                                .trim()
                                .equalsIgnoreCase(nomeTrim)
                );
    }
}