package com.managementsystem.demo1.controller;

import com.managementsystem.demo1.model.MovimentacaoFinanceira;
import com.managementsystem.demo1.model.SituacaoMovimentacaoFinanceira;
import com.managementsystem.demo1.model.TipoMovimentacaoFinanceira;
import com.managementsystem.demo1.service.MovimentacaoFinanceiraService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/despesas", "/financeiro/despesas", "/movimentacoes-financeiras/despesas"})
public class DespesaRestController {

    private final MovimentacaoFinanceiraService movimentacaoFinanceiraService;

    public DespesaRestController(MovimentacaoFinanceiraService movimentacaoFinanceiraService) {
        this.movimentacaoFinanceiraService = movimentacaoFinanceiraService;
    }

    public record NovaDespesaDTO(
            String descricao,
            BigDecimal valor,
            String categoria,
            SituacaoMovimentacaoFinanceira situacao,
            LocalDate dataMovimentacao,
            LocalDate dataPagamento,
            LocalDate dataVencimento,
            String observacoes,
            TipoMovimentacaoFinanceira tipo
    ) {
        public NovaDespesaDTO(
                String descricao,
                BigDecimal valor,
                String categoria,
                SituacaoMovimentacaoFinanceira situacao,
                LocalDate dataMovimentacao,
                LocalDate dataVencimento,
                String observacoes
        ) {
            this(descricao, valor, categoria, situacao, dataMovimentacao, null, dataVencimento, observacoes, null);
        }
    }

    // POST /despesas (também responde em /financeiro/despesas e /movimentacoes-financeiras/despesas)
    @PostMapping
    public ResponseEntity<?> cadastrar(@RequestBody(required = false) NovaDespesaDTO dto) {
        if (dto == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", "Dados da despesa são obrigatórios."));
        }

        try {
            if (dto.dataMovimentacao() != null
                    && dto.dataPagamento() != null
                    && !dto.dataMovimentacao().equals(dto.dataPagamento())) {
                return ResponseEntity.badRequest()
                        .body(Map.of("erro", "Data da movimentação e data de pagamento informadas divergem."));
            }

            LocalDate dataEfetiva = dto.dataMovimentacao() != null
                    ? dto.dataMovimentacao()
                    : dto.dataPagamento();

            MovimentacaoFinanceira despesa = new MovimentacaoFinanceira();
            despesa.setDescricao(dto.descricao());
            despesa.setValor(dto.valor());
            despesa.setCategoria(dto.categoria());
            despesa.setSituacao(dto.situacao());
            despesa.setDataMovimentacao(dataEfetiva);
            despesa.setDataVencimento(dto.dataVencimento());
            despesa.setObservacoes(dto.observacoes());
            if (dto.tipo() != null) {
                despesa.setTipo(dto.tipo());
            }

            MovimentacaoFinanceira salva = movimentacaoFinanceiraService.cadastrarDespesa(despesa);

            return ResponseEntity.status(HttpStatus.CREATED).body(salva);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", e.getMessage()));
        }
    }

    // GET /despesas
    @GetMapping
    public List<MovimentacaoFinanceira> listar() {
        return movimentacaoFinanceiraService.listarDespesas();
    }

    // GET /despesas/{id}
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable("id") Long id) {
        return movimentacaoFinanceiraService.buscarDespesaPorId(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, String>> tratarIdInvalido(MethodArgumentTypeMismatchException e) {
        return ResponseEntity.badRequest()
                .body(Map.of("erro", "Identificador de despesa inválido."));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> tratarJsonInvalido(HttpMessageNotReadableException e) {
        return ResponseEntity.badRequest()
                .body(Map.of("erro", "Formato de requisição inválido ou campos incompatíveis."));
    }
}

