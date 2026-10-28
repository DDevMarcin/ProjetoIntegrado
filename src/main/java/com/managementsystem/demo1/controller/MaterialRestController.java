package com.managementsystem.demo1.controller;

import com.managementsystem.demo1.model.Material;
import com.managementsystem.demo1.service.MaterialService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/materiais")
public class MaterialRestController {

    private final MaterialService materialService;

    public MaterialRestController(MaterialService materialService) {
        this.materialService = materialService;
    }

    public record NovoMaterialDTO(String nome, BigDecimal precoUnidade, String unidadeMedida) {}

    // POST /materiais
    @PostMapping
    public ResponseEntity<?> cadastrar(@RequestBody(required = false) NovoMaterialDTO dto) {
        if (dto == null) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Dados do material são obrigatórios."));
        }
        try {
            Material material = new Material();
            material.setNome(dto.nome());
            material.setPrecoUnidade(dto.precoUnidade());
            material.setUnidadeMedida(dto.unidadeMedida());

            Material salvo = materialService.salvar(material);
            return ResponseEntity.status(HttpStatus.CREATED).body(salvo);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    // GET /materiais (suporta listagem total ou filtros opcionais por nome e unidadeMedida)
    @GetMapping
    public List<Material> listar(
            @RequestParam(value = "nome", required = false) String nome,
            @RequestParam(value = "unidadeMedida", required = false) String unidadeMedida
    ) {
        if ((nome != null && !nome.isBlank()) || (unidadeMedida != null && !unidadeMedida.isBlank())) {
            return materialService.consultar(nome, unidadeMedida);
        }
        return materialService.listarTodos();
    }

    // GET /materiais/buscar?nome=xxx
    @GetMapping("/buscar")
    public List<Material> buscarPorNome(@RequestParam(value = "nome", required = false) String nome) {
        return materialService.buscarPorNome(nome);
    }

    // GET /materiais/{id}
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable("id") Long id) {
        return materialService.buscarPorId(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // PUT /materiais/{id}
    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(@PathVariable("id") Long id, @RequestBody(required = false) NovoMaterialDTO dto) {
        if (dto == null) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Dados do material são obrigatórios."));
        }
        try {
            Material atualizado = materialService.atualizar(id, dto.nome(), dto.precoUnidade(), dto.unidadeMedida());
            return ResponseEntity.ok(atualizado);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    // DELETE /materiais/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<?> excluir(@PathVariable("id") Long id) {
        try {
            materialService.excluir(id);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, String>> tratarIdInvalido(MethodArgumentTypeMismatchException e) {
        return ResponseEntity.badRequest().body(Map.of("erro", "Identificador de material inválido."));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> tratarJsonInvalido(HttpMessageNotReadableException e) {
        return ResponseEntity.badRequest().body(Map.of("erro", "Formato de requisição inválido ou campos incompatíveis."));
    }
}
