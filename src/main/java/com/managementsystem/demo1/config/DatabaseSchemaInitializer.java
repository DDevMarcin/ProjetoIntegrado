package com.managementsystem.demo1.config;

import com.managementsystem.demo1.model.CategoriaDespesa;
import com.managementsystem.demo1.model.Material;
import com.managementsystem.demo1.model.MovimentacaoFinanceira;
import com.managementsystem.demo1.model.Produto;
import com.managementsystem.demo1.model.TipoMovimentacaoFinanceira;
import com.managementsystem.demo1.repository.CategoriaDespesaRepository;
import com.managementsystem.demo1.repository.MaterialRepository;
import com.managementsystem.demo1.repository.MovimentacaoFinanceiraRepository;
import com.managementsystem.demo1.repository.ProdutoMaterialRepository;
import com.managementsystem.demo1.repository.ProdutoRepository;
import com.managementsystem.demo1.service.CategoriaDespesaService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.sql.init.dependency.DependsOnDatabaseInitialization;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Garante compatibilidade retroativa e integridade de unicidade nos bancos
 * SQLite
 * (artesa.db / artesa_test.db) criados antes das evoluções de schema.
 */
@Component
@DependsOnDatabaseInitialization
public class DatabaseSchemaInitializer {

    private final DataSource dataSource;
    private final MaterialRepository materialRepository;
    private final ProdutoRepository produtoRepository;
    private final ProdutoMaterialRepository produtoMaterialRepository;
    private final CategoriaDespesaRepository categoriaDespesaRepository;
    private final MovimentacaoFinanceiraRepository movimentacaoFinanceiraRepository;

    public DatabaseSchemaInitializer(DataSource dataSource,
            MaterialRepository materialRepository,
            ProdutoRepository produtoRepository,
            ProdutoMaterialRepository produtoMaterialRepository) {
        this(dataSource, materialRepository, produtoRepository, produtoMaterialRepository, null, null);
    }

    @Autowired
    public DatabaseSchemaInitializer(DataSource dataSource,
            MaterialRepository materialRepository,
            ProdutoRepository produtoRepository,
            ProdutoMaterialRepository produtoMaterialRepository,
            CategoriaDespesaRepository categoriaDespesaRepository,
            MovimentacaoFinanceiraRepository movimentacaoFinanceiraRepository) {
        this.dataSource = dataSource;
        this.materialRepository = materialRepository;
        this.produtoRepository = produtoRepository;
        this.produtoMaterialRepository = produtoMaterialRepository;
        this.categoriaDespesaRepository = categoriaDespesaRepository;
        this.movimentacaoFinanceiraRepository = movimentacaoFinanceiraRepository;
    }

    @PostConstruct
    public void inicializarSchema() {
        garantirColunaCorMaterial();
        garantirColunaEstoqueMaterial();
        garantirColunaCategoriaDespesaMovimentacaoFinanceira();
        garantirCategoriasDespesaPadraoEVinculos();
        garantirUnicidadeNomesExistentes();
    }

    private void garantirColunaCorMaterial() {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            boolean possuiColunaCor;
            try (ResultSet rs = metaData.getColumns(null, null, "material", "cor")) {
                possuiColunaCor = rs.next();
            }

            if (!possuiColunaCor) {
                String comandoAlter = String.join(" ",
                        "ALTER TABLE", "material",
                        "ADD COLUMN", "cor VARCHAR(50) NOT NULL DEFAULT ''");
                try (Statement stmt = connection.createStatement()) {
                    stmt.execute(comandoAlter);
                }
            }
        } catch (Exception ignored) {
        }
    }

    private void garantirColunaEstoqueMaterial() {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            boolean possuiColunaEstoque;
            try (ResultSet rs = metaData.getColumns(null, null, "material", "quantidade_estoque")) {
                possuiColunaEstoque = rs.next();
            }

            if (!possuiColunaEstoque) {
                String comandoAlter = String.join(" ",
                        "ALTER TABLE", "material",
                        "ADD COLUMN", "quantidade_estoque NUMERIC(10,2) NOT NULL DEFAULT 0");
                try (Statement stmt = connection.createStatement()) {
                    stmt.execute(comandoAlter);
                }
            }
        } catch (Exception ignored) {
        }
    }

    private void garantirColunaCategoriaDespesaMovimentacaoFinanceira() {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            boolean possuiColunaCategoriaDespesa;
            try (ResultSet rs = metaData.getColumns(null, null, "movimentacao_financeira", "id_categoria_despesa")) {
                possuiColunaCategoriaDespesa = rs.next();
            }

            if (!possuiColunaCategoriaDespesa) {
                String comandoAlter = String.join(" ",
                        "ALTER TABLE", "movimentacao_financeira",
                        "ADD COLUMN", "id_categoria_despesa BIGINT REFERENCES categoria_despesa(id_categoria)");
                try (Statement stmt = connection.createStatement()) {
                    stmt.execute(comandoAlter);
                }
            }
        } catch (Exception ignored) {
        }
    }

    private void garantirCategoriasDespesaPadraoEVinculos() {
        if (categoriaDespesaRepository == null) {
            return;
        }
        try {
            for (Map.Entry<String, String> entry : CategoriaDespesaService.CATEGORIAS_PADRAO.entrySet()) {
                String nome = entry.getKey();
                if (!categoriaDespesaRepository.existsByNomeIgnoreCase(nome)) {
                    categoriaDespesaRepository.save(new CategoriaDespesa(nome, entry.getValue()));
                }
            }

            if (movimentacaoFinanceiraRepository != null) {
                List<MovimentacaoFinanceira> despesas = movimentacaoFinanceiraRepository
                        .findByTipoOrderByDataMovimentacaoDescIdMovimentacaoDesc(TipoMovimentacaoFinanceira.DESPESA);
                for (MovimentacaoFinanceira despesa : despesas) {
                    if (despesa.getCategoriaDespesa() == null
                            && despesa.getCategoria() != null
                            && !despesa.getCategoria().isBlank()) {
                        String nomeCat = despesa.getCategoria().trim();
                        Optional<CategoriaDespesa> catOpt = categoriaDespesaRepository.findByNomeIgnoreCase(nomeCat);
                        if (catOpt.isEmpty()) {
                            CategoriaDespesa nova = categoriaDespesaRepository.save(new CategoriaDespesa(nomeCat));
                            despesa.setCategoriaDespesa(nova);
                        } else {
                            despesa.setCategoriaDespesa(catOpt.get());
                        }
                        movimentacaoFinanceiraRepository.save(despesa);
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }

    private void garantirUnicidadeNomesExistentes() {
        try {
            List<Material> materiais = materialRepository.findAll();
            Set<String> nomesMateriaisVistos = new HashSet<>();
            for (Material m : materiais) {
                if (m.getNome() == null) {
                    continue;
                }
                String nomeTrim = m.getNome().trim();
                String chave = nomeTrim.toLowerCase(Locale.ROOT);
                if (!nomesMateriaisVistos.add(chave)) {
                    if (m.getIdMaterial() != null
                            && !produtoMaterialRepository.existsByMaterial_IdMaterial(m.getIdMaterial())) {
                        materialRepository.deleteById(m.getIdMaterial());
                    }
                } else if (!nomeTrim.equals(m.getNome())) {
                    m.setNome(nomeTrim);
                    materialRepository.save(m);
                }
            }

            List<Produto> produtos = produtoRepository.findAll();
            Set<String> nomesProdutosVistos = new HashSet<>();
            for (Produto p : produtos) {
                if (p.getNome() == null) {
                    continue;
                }
                String nomeTrim = p.getNome().trim();
                String chave = nomeTrim.toLowerCase(Locale.ROOT);
                if (!nomesProdutosVistos.add(chave)) {
                    if (p.getIdProduto() != null) {
                        produtoRepository.deleteById(p.getIdProduto());
                    }
                } else if (!nomeTrim.equals(p.getNome())) {
                    p.setNome(nomeTrim);
                    produtoRepository.save(p);
                }
            }
        } catch (Exception ignored) {
        }
    }
}
