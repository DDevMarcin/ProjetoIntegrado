package com.managementsystem.demo1.controller;

import com.managementsystem.demo1.model.CategoriaDespesa;
import com.managementsystem.demo1.service.CategoriaDespesaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping({"/categorias-despesas", "/financeiro/categorias-despesas", "/despesas/categorias"})
public class CategoriaDespesaRestController {

    private final CategoriaDespesaService categoriaDespesaService;

    public CategoriaDespesaRestController(CategoriaDespesaService categoriaDespesaService) {
        this.categoriaDespesaService = categoriaDespesaService;
    }

    public record CategoriaDespesaDTO(
            String nome,
            String descricao
    ) {
    }

    // POST /categorias-despesas (também responde em /financeiro/categorias-despesas e /despesas/categorias)
    @PostMapping
    public ResponseEntity<?> cadastrar(@RequestBody(required = false) CategoriaDespesaDTO dto) {
        if (dto == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", "Dados da categoria de despesa são obrigatórios."));
        }

        try {
            CategoriaDespesa categoria = new CategoriaDespesa(dto.nome(), dto.descricao());
            CategoriaDespesa salva = categoriaDespesaService.salvar(categoria);
            return ResponseEntity.status(HttpStatus.CREATED).body(salva);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", e.getMessage()));
        }
    }

    // GET /categorias-despesas
    @GetMapping
    public List<CategoriaDespesa> listar(
            @RequestParam(value = "nome", required = false) String nome
    ) {
        if (nome != null && !nome.isBlank()) {
            return categoriaDespesaService.buscarPorNome(nome);
        }
        return categoriaDespesaService.listarTodas();
    }

    // GET /categorias-despesas/{id}
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable("id") Long id) {
        return categoriaDespesaService.buscarPorId(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // PUT /categorias-despesas/{id}
    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(
            @PathVariable("id") Long id,
            @RequestBody(required = false) CategoriaDespesaDTO dto
    ) {
        if (dto == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", "Dados da categoria de despesa são obrigatórios."));
        }

        try {
            CategoriaDespesa atualizada = categoriaDespesaService.atualizar(
                    id,
                    dto.nome(),
                    dto.descricao()
            );
            return ResponseEntity.ok(atualizada);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", e.getMessage()));
        }
    }

    // DELETE /categorias-despesas/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<?> excluir(@PathVariable("id") Long id) {
        try {
            categoriaDespesaService.excluir(id);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", e.getMessage()));
        }
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, String>> tratarIdInvalido(MethodArgumentTypeMismatchException e) {
        return ResponseEntity.badRequest()
                .body(Map.of("erro", "Identificador de categoria de despesa inválido."));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> tratarJsonInvalido(HttpMessageNotReadableException e) {
        return ResponseEntity.badRequest()
                .body(Map.of("erro", "Formato de requisição inválido ou campos incompatíveis."));
    }
}

