package com.managementsystem.demo1.controller;

import com.managementsystem.demo1.model.Material;
import com.managementsystem.demo1.service.MaterialService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

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
    public ResponseEntity<?> cadastrar(@RequestBody NovoMaterialDTO dto) {
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

    // GET /materiais
    @GetMapping
    public List<Material> listar() {
        return materialService.listarTodos();
    }

    // GET /materiais/{id}
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        return materialService.buscarPorId(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}