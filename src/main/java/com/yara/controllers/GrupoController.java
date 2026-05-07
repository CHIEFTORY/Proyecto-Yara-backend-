package com.yara.controllers;

import com.yara.dtos.CrearGrupoDTO;
import com.yara.dtos.ResumenDTO;
import com.yara.entities.Grupo;
import com.yara.services.GrupoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/grupos")
public class GrupoController {

    private final GrupoService grupoService;

    public GrupoController(GrupoService grupoService) {
        this.grupoService = grupoService;
    }

    @PostMapping
    public Grupo crearGrupo(
            @Valid @RequestBody CrearGrupoDTO dto
    ) {
        return grupoService.crearGrupo(dto);
    }

    @GetMapping("/{grupoId}/resumen")
    public ResumenDTO resumen(
            @PathVariable Integer grupoId
    ) {
        return grupoService.obtenerResumen(grupoId);
    }

    @PostMapping("/{grupoId}/usuarios/{usuarioId}")
    public ResponseEntity<String> agregarUsuario(
            @PathVariable Integer grupoId,
            @PathVariable Integer usuarioId
    ) {
        grupoService.agregarUsuarioAGrupo(grupoId, usuarioId);
        return ResponseEntity.ok("Usuario agregado al grupo");
    }

    @DeleteMapping("/{grupoId}/salir")
    public ResponseEntity<String> salir(@PathVariable Integer grupoId) {
        grupoService.salirDelGrupo(grupoId);
        return ResponseEntity.ok("Saliste del grupo");
    }

    @DeleteMapping("/{grupoId}/usuarios/{usuarioId}")
    public ResponseEntity<String> eliminarUsuario(
            @PathVariable Integer grupoId,
            @PathVariable Integer usuarioId
    ) {
        grupoService.eliminarUsuario(grupoId, usuarioId);
        return ResponseEntity.ok("Usuario eliminado del grupo");
    }

    @GetMapping("/{grupoId}/usuarios")
    public List<String> listarUsuarios(@PathVariable Integer grupoId) {
        return grupoService.listarUsuarios(grupoId);
    }

    @GetMapping("/mios")
    public List<String> misGrupos() {
        return grupoService.listarMisGrupos();
    }
}