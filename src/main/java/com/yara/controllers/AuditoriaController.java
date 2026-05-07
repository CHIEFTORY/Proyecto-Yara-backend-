package com.yara.controllers;

import com.yara.dtos.AuditoriaResponseDTO;
import com.yara.services.AuditoriaService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/auditoria")
public class AuditoriaController {

    private final AuditoriaService auditoriaService;

    public AuditoriaController(
            AuditoriaService auditoriaService
    ) {
        this.auditoriaService = auditoriaService;
    }

    // 🔹 TODAS
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<AuditoriaResponseDTO>>
    listarTodas() {

        return ResponseEntity.ok(
                auditoriaService.listarTodas()
        );
    }

    // 🔹 POR USUARIO
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<AuditoriaResponseDTO>>
    listarPorUsuario(
            @PathVariable Integer usuarioId
    ) {

        return ResponseEntity.ok(
                auditoriaService.listarPorUsuario(usuarioId)
        );
    }
}