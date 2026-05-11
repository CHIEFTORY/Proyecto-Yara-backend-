package com.yara.controllers;

import com.yara.dtos.*;
import com.yara.services.GastoService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/gastos")
public class GastoController {

    private final GastoService gastoService;

    public GastoController(GastoService gastoService) {
        this.gastoService = gastoService;
    }

    @PostMapping
    public GastoResponseDTO crearGasto(
            @Valid @RequestBody CrearGastoDTO dto
    ) {
        return gastoService.crearGasto(dto);
    }

    @GetMapping("/grupo/{grupoId}")
    public Page<GastoResponseDTO> listar(
            @PathVariable Integer grupoId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return gastoService.listarPorGrupo(grupoId, page, size);
    }

    @GetMapping("/grupo/{grupoId}/balances")
    public List<BalanceDTO> balances(
            @PathVariable Integer grupoId
    ) {
        return gastoService.obtenerBalances(grupoId);
    }

    @GetMapping("/grupo/{grupoId}/deudas")
    public List<DeudaDTO> deudas(
            @PathVariable Integer grupoId
    ) {
        return gastoService.simplificarDeudas(grupoId);
    }

    @DeleteMapping("/{gastoId}")
    public void eliminar(@PathVariable Integer gastoId) {
        gastoService.eliminarGasto(gastoId);
    }
    @PutMapping("/{gastoId}")
    public GastoResponseDTO editarGasto(
            @PathVariable Integer gastoId,
            @RequestBody @Valid CrearGastoDTO dto
    ) {
        return gastoService.editarGasto(gastoId, dto);
    }

    @GetMapping("/{gastoId}")
    public GastoResponseDTO obtenerPorId(
            @PathVariable Integer gastoId
    ) {
        return gastoService.obtenerPorId(gastoId);
    }

    @GetMapping("/chart")
    public ResponseEntity<List<ChartDTO>>
    obtenerChart() {

        return ResponseEntity.ok(
                gastoService.obtenerChartGastos()
        );
    }
}