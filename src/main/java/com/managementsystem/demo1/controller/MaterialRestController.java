package com.managementsystem.demo1.controller;

import com.managementsystem.demo1.model.Material;
import com.managementsystem.demo1.model.MovimentacaoMaterial;
import com.managementsystem.demo1.model.TipoMovimentacaoMaterial;
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

    public record NovoMaterialDTO(
            String nome,
            String cor,
            BigDecimal precoUnidade,
            String unidadeMedida,
            BigDecimal quantidadeEstoque
    ) {
        public NovoMaterialDTO(
                String nome,
                String cor,
                BigDecimal precoUnidade,
                String unidadeMedida
        ) {
            this(nome, cor, precoUnidade, unidadeMedida, BigDecimal.ZERO);
        }
    }

    public record MovimentacaoMaterialDTO(
            Long idMaterial,
            TipoMovimentacaoMaterial tipo,
            BigDecimal quantidade,
            String motivo,
            String observacoes
    ) {}

    public record PerdaMaterialDTO(
            Long idMaterial,
            BigDecimal quantidade,
            String motivo,
            String observacoes
    ) {}

    // POST /materiais
    @PostMapping
    public ResponseEntity<?> cadastrar(
            @RequestBody(required = false) NovoMaterialDTO dto
    ) {
        if (dto == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", "Dados do material são obrigatórios."));
        }

        try {
            Material material = new Material();

            material.setNome(dto.nome());
            material.setCor(dto.cor());
            material.setPrecoUnidade(dto.precoUnidade());
            material.setUnidadeMedida(dto.unidadeMedida());
            material.setQuantidadeEstoque(
                    dto.quantidadeEstoque() != null
                            ? dto.quantidadeEstoque()
                            : BigDecimal.ZERO
            );

            Material salvo = materialService.salvar(material);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(salvo);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", e.getMessage()));
        }
    }

    // GET /materiais
    // Suporta listagem total ou filtros opcionais por nome e unidadeMedida
    @GetMapping
    public List<Material> listar(
            @RequestParam(value = "nome", required = false) String nome,
            @RequestParam(value = "unidadeMedida", required = false) String unidadeMedida
    ) {
        if ((nome != null && !nome.isBlank())
                || (unidadeMedida != null && !unidadeMedida.isBlank())) {

            return materialService.consultar(nome, unidadeMedida);
        }

        return materialService.listarTodos();
    }

    // GET /materiais/buscar?nome=xxx
    @GetMapping("/buscar")
    public List<Material> buscarPorNome(
            @RequestParam(value = "nome", required = false) String nome
    ) {
        return materialService.buscarPorNome(nome);
    }

    // POST /materiais/movimentacoes
    @PostMapping("/movimentacoes")
    public ResponseEntity<?> registrarMovimentacao(
            @RequestBody(required = false) MovimentacaoMaterialDTO dto
    ) {
        if (dto == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", "Dados da movimentação são obrigatórios."));
        }

        try {
            MovimentacaoMaterial registrada =
                    materialService.registrarMovimentacao(
                            dto.idMaterial(),
                            dto.tipo(),
                            dto.quantidade(),
                            dto.motivo(),
                            dto.observacoes()
                    );

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(registrada);

        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", e.getMessage()));
        }
    }

    // POST /materiais/{id}/movimentacoes
    @PostMapping("/{id}/movimentacoes")
    public ResponseEntity<?> registrarMovimentacaoPorId(
            @PathVariable("id") Long id,
            @RequestBody(required = false) MovimentacaoMaterialDTO dto
    ) {
        if (dto == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", "Dados da movimentação são obrigatórios."));
        }

        if (dto.idMaterial() != null && !dto.idMaterial().equals(id)) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "erro",
                            "ID do material na rota diverge do ID informado no corpo da requisição."
                    ));
        }

        try {
            MovimentacaoMaterial registrada =
                    materialService.registrarMovimentacao(
                            id,
                            dto.tipo(),
                            dto.quantidade(),
                            dto.motivo(),
                            dto.observacoes()
                    );

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(registrada);

        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", e.getMessage()));
        }
    }

    // GET /materiais/movimentacoes
    @GetMapping("/movimentacoes")
    public ResponseEntity<?> listarMovimentacoes(
            @RequestParam(value = "idMaterial", required = false) Long idMaterial,
            @RequestParam(value = "tipo", required = false) TipoMovimentacaoMaterial tipo
    ) {
        try {
            if (idMaterial != null) {
                return ResponseEntity.ok(
                        materialService.listarMovimentacoesPorMaterial(idMaterial)
                );
            }

            if (tipo != null) {
                return ResponseEntity.ok(
                        materialService.listarMovimentacoesPorTipo(tipo)
                );
            }

            return ResponseEntity.ok(
                    materialService.listarMovimentacoes()
            );

        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // GET /materiais/{id}/movimentacoes
    @GetMapping("/{id}/movimentacoes")
    public ResponseEntity<?> listarMovimentacoesPorMaterial(
            @PathVariable("id") Long id
    ) {
        try {
            return ResponseEntity.ok(
                    materialService.listarMovimentacoesPorMaterial(id)
            );

        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // POST /materiais/perdas
    @PostMapping("/perdas")
    public ResponseEntity<?> registrarPerda(
            @RequestBody(required = false) PerdaMaterialDTO dto
    ) {
        if (dto == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", "Dados da perda são obrigatórios."));
        }

        try {
            MovimentacaoMaterial registrada =
                    materialService.registrarPerda(
                            dto.idMaterial(),
                            dto.quantidade(),
                            dto.motivo(),
                            dto.observacoes()
                    );

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(registrada);

        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", e.getMessage()));
        }
    }

    // POST /materiais/{id}/perdas
    @PostMapping("/{id}/perdas")
    public ResponseEntity<?> registrarPerdaPorId(
            @PathVariable("id") Long id,
            @RequestBody(required = false) PerdaMaterialDTO dto
    ) {
        if (dto == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", "Dados da perda são obrigatórios."));
        }

        if (dto.idMaterial() != null && !dto.idMaterial().equals(id)) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "erro",
                            "ID do material na rota diverge do ID informado no corpo da requisição."
                    ));
        }

        try {
            MovimentacaoMaterial registrada =
                    materialService.registrarPerda(
                            id,
                            dto.quantidade(),
                            dto.motivo(),
                            dto.observacoes()
                    );

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(registrada);

        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", e.getMessage()));
        }
    }

    // GET /materiais/perdas
    @GetMapping("/perdas")
    public List<MovimentacaoMaterial> listarPerdas() {
        return materialService.listarPerdas();
    }

    // GET /materiais/{id}/perdas
    @GetMapping("/{id}/perdas")
    public ResponseEntity<?> listarPerdasPorMaterial(
            @PathVariable("id") Long id
    ) {
        try {
            return ResponseEntity.ok(
                    materialService.listarPerdasPorMaterial(id)
            );

        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // GET /materiais/{id}
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(
            @PathVariable("id") Long id
    ) {
        return materialService.buscarPorId(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // PUT /materiais/{id}
    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(
            @PathVariable("id") Long id,
            @RequestBody(required = false) NovoMaterialDTO dto
    ) {
        if (dto == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", "Dados do material são obrigatórios."));
        }

        try {
            Material atualizado =
                    materialService.atualizar(
                            id,
                            dto.nome(),
                            dto.cor(),
                            dto.precoUnidade(),
                            dto.unidadeMedida()
                    );

            return ResponseEntity.ok(atualizado);

        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", e.getMessage()));
        }
    }

    // DELETE /materiais/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<?> excluir(
            @PathVariable("id") Long id
    ) {
        try {
            materialService.excluir(id);
            return ResponseEntity.noContent().build();

        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();

        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", e.getMessage()));
        }
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, String>> tratarIdInvalido(
            MethodArgumentTypeMismatchException e
    ) {
        return ResponseEntity.badRequest()
                .body(Map.of("erro", "Identificador de material inválido."));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> tratarJsonInvalido(
            HttpMessageNotReadableException e
    ) {
        return ResponseEntity.badRequest()
                .body(Map.of(
                        "erro",
                        "Formato de requisição inválido ou campos incompatíveis."
                ));
    }
}