package com.yara.controllers;

import com.yara.dtos.CrearPagoDTO;
import com.yara.dtos.PagoResponseDTO;
import com.yara.services.PagoService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pagos")
public class PagoController {

    private final PagoService pagoService;

    public PagoController(PagoService pagoService) {
        this.pagoService = pagoService;
    }

    @PostMapping
    public PagoResponseDTO registrarPago(
            @Valid @RequestBody CrearPagoDTO dto
    ) {
        return pagoService.registrarPago(dto);
    }

    @GetMapping("/grupo/{grupoId}")
    public List<PagoResponseDTO> listarPorGrupo(
            @PathVariable Integer grupoId
    ) {
        return pagoService.listarPorGrupo(grupoId);
    }

    @DeleteMapping("/{pagoId}")
    public void eliminar(@PathVariable Integer pagoId) {
        pagoService.eliminarPago(pagoId);
    }
}