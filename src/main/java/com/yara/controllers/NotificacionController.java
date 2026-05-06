package com.yara.controllers;

import com.yara.entities.Notificacion;
import com.yara.entities.UsuarioNotificacion;
import com.yara.services.NotificacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/notificaciones")
@RequiredArgsConstructor
public class NotificacionController {

    private final NotificacionService notificacionService;

    @GetMapping("/{usuarioId}")
    public List<UsuarioNotificacion> listar(@PathVariable Integer usuarioId) {
        return notificacionService.listarPorUsuario(usuarioId);
    }
}