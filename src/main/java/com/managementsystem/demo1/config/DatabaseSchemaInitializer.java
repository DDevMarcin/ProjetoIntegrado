package com.managementsystem.demo1.config;

import jakarta.annotation.PostConstruct;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Garante compatibilidade retroativa com bancos SQLite (artesa.db / artesa_test.db)
 * criados antes da adição da coluna quantidade_estoque na tabela material.
 */
@Component
public class DatabaseSchemaInitializer {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseSchemaInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void garantirColunaEstoqueMaterial() {
        List<Map<String, Object>> colunas = jdbcTemplate.queryForList("PRAGMA table_info(material)");
        boolean possuiColunaEstoque = colunas.stream()
                .anyMatch(col -> "quantidade_estoque".equalsIgnoreCase(String.valueOf(col.get("name"))));

        if (!possuiColunaEstoque) {
            jdbcTemplate.execute("ALTER TABLE material ADD COLUMN quantidade_estoque NUMERIC(10,2) NOT NULL DEFAULT 0");
        }
    }
}

