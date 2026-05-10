package com.yara.controllers;

import com.yara.dtos.GuardarMetodoPagoDTO;
import com.yara.dtos.MetodoPagoDTO;
import com.yara.services.MetodoPagoService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/metodos-pago")
public class MetodoPagoController {

    private final MetodoPagoService
            metodoPagoService;

    public MetodoPagoController(
            MetodoPagoService metodoPagoService
    ) {

        this.metodoPagoService =
                metodoPagoService;
    }

    @GetMapping
    public List<MetodoPagoDTO>
    listar() {

        return metodoPagoService
                .listarMisMetodos();
    }

    @PostMapping
    public MetodoPagoDTO guardar(
            @RequestBody
            GuardarMetodoPagoDTO dto
    ) {

        return metodoPagoService
                .guardarMetodoPago(dto);
    }

    @DeleteMapping("/{id}")
    public void eliminarMetodoPago(
            @PathVariable Integer id
    ) {

        metodoPagoService
                .eliminarMetodoPago(id);
    }
}
