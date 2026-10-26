package com.managementsystem.demo1.controller;

import com.managementsystem.demo1.model.Produto;
import com.managementsystem.demo1.service.ProdutoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller REST para o recurso de Controle de Estoque.
 * Permite consultar produtos cadastrados no contexto de estoque (produtos do tipo PRE_PRONTO),
 * replicando os filtros, busca por nome, busca por ID e total em estoque da consulta original.
 */
@RestController
@RequestMapping({"/estoque", "/produtos/estoque"})
public class EstoqueRestController {

    private final ProdutoService produtoService;

    public EstoqueRestController(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    // GET /estoque  ou  GET /estoque?nome=xxx
    // Também atende rotas compatíveis /estoque/produtos e /produtos/estoque
    @GetMapping({"", "/produtos"})
    public List<Produto> listar(
            @RequestParam(value = "nome", required = false) String nome
    ) {
        if (nome != null && !nome.isBlank()) {
            return produtoService.buscarEstoquePorNome(nome);
        }
        return produtoService.listarEstoque();
    }

    // GET /estoque/buscar?nome=xxx
    @GetMapping({"/buscar", "/produtos/buscar"})
    public List<Produto> buscarPorNome(
            @RequestParam(value = "nome", required = false) String nome
    ) {
        return produtoService.buscarEstoquePorNome(nome);
    }

    // GET /estoque/{id}
    @GetMapping({"/{id}", "/produtos/{id}"})
    public ResponseEntity<?> buscarPorId(@PathVariable("id") Long id) {
        return produtoService.buscarEstoquePorId(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // GET /estoque/total  ou  GET /estoque/total-estoque
    @GetMapping({"/total", "/total-estoque"})
    public Map<String, Integer> totalEmEstoque() {
        return Map.of("total", produtoService.totalEmEstoque());
    }
}
