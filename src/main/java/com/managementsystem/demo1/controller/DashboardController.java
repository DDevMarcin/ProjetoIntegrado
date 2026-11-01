package com.managementsystem.demo1.controller;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class DashboardController {
    // Será expandido futuramente com cards de resumo (total de produtos, encomendas, etc.)
}
