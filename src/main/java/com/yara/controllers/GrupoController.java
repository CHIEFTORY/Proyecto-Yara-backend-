package com.yara.controllers;

import com.yara.dtos.CrearGrupoDTO;
import com.yara.dtos.ResumenDTO;
import com.yara.entities.Grupo;
import com.yara.services.GrupoService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/grupos")
public class GrupoController {

    private final GrupoService grupoService;

    public GrupoController(GrupoService grupoService) {
        this.grupoService = grupoService;
    }

    @PostMapping
    public Grupo crearGrupo(
            @RequestBody CrearGrupoDTO dto
    ) {
        return grupoService.crearGrupo(dto);
    }

    @GetMapping("/{grupoId}/resumen")
    public ResumenDTO resumen(
            @PathVariable Integer grupoId
    ) {
        return grupoService.obtenerResumen(grupoId);
    }
}