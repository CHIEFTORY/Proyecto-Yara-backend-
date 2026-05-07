package com.yara.controllers;

import com.yara.dtos.AuditoriaResponseDTO;
import com.yara.services.AuditoriaService;
import org.springframework.http.ResponseEntity;
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
    @GetMapping
    public ResponseEntity<List<AuditoriaResponseDTO>>
    listarTodas() {

        return ResponseEntity.ok(
                auditoriaService.listarTodas()
        );
    }

    // 🔹 POR USUARIO
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