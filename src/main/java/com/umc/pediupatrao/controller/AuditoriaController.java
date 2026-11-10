package com.umc.pediupatrao.controller;

import com.umc.pediupatrao.service.AuditoriaService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/auditoria")
@PreAuthorize("hasAnyRole('ADMIN','GERENTE')")
public class AuditoriaController {

    private final AuditoriaService auditoriaService;

    public AuditoriaController(AuditoriaService auditoriaService) {
        this.auditoriaService = auditoriaService;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) String entidadeId, Model model) {
        if (entidadeId != null && !entidadeId.isBlank()) {
            model.addAttribute("registros", auditoriaService.listarPorEntidade(entidadeId));
            model.addAttribute("filtro", entidadeId);
        } else {
            model.addAttribute("registros", auditoriaService.listarTodos());
        }
        model.addAttribute("content", "auditoria/lista :: content");
        return "layout";
    }
}
