package com.managementsystem.demo1.controller;

import com.managementsystem.demo1.service.RelatorioFinanceiroService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/relatorios/financeiros")
public class RelatorioFinanceiroController {

    private final RelatorioFinanceiroService relatorioFinanceiroService;

    public RelatorioFinanceiroController(
            RelatorioFinanceiroService relatorioFinanceiroService
    ) {
        this.relatorioFinanceiroService = relatorioFinanceiroService;
    }

    // GET /relatorios/financeiros?dataInicio=AAAA-MM-DD&dataFim=AAAA-MM-DD
    @GetMapping(produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<?> gerarRelatorio(
            @RequestParam("dataInicio")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate dataInicio,

            @RequestParam("dataFim")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate dataFim
    ) {
        try {
            byte[] pdf = relatorioFinanceiroService.gerarRelatorio(
                    dataInicio,
                    dataFim
            );

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"relatorio-financeiro.pdf\""
                    )
                    .body(pdf);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", e.getMessage()));
        }
    }
}