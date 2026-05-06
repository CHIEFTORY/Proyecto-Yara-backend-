package com.yara.controllers;

import com.yara.entities.Notificacion;
import com.yara.entities.UsuarioNotificacion;
import com.yara.entities.UsuarioNotificacionId;
import com.yara.repositories.UsuarioNotificacionRepository;
import com.yara.services.NotificacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notificaciones")
@RequiredArgsConstructor
public class NotificacionController {

    private final NotificacionService notificacionService;
    private final UsuarioNotificacionRepository usuarioNotificacionRepository;

    @GetMapping("/{usuarioId}")
    public List<UsuarioNotificacion> listar(@PathVariable Integer usuarioId) {
        return notificacionService.listarPorUsuario(usuarioId);
    }

    @PutMapping("/leer")
    public ResponseEntity<String> marcarComoLeido(@RequestParam Integer usuarioId,
                                                  @RequestParam Integer notificacionId) {

        UsuarioNotificacionId id =
                new UsuarioNotificacionId(usuarioId, notificacionId);

        UsuarioNotificacion un = usuarioNotificacionRepository
                .findById(id)
                .orElseThrow(() -> new RuntimeException("Notificación no encontrada"));

        if (!un.getUsuario().getId().equals(usuarioId)) {
            throw new RuntimeException("No autorizado");
        }

        if (Boolean.TRUE.equals(un.getLeido())) {
            return ResponseEntity.ok("Ya estaba leída");
        }

        un.setLeido(true);
        usuarioNotificacionRepository.save(un);

        return ResponseEntity.ok("Notificación marcada como leída");
    }

    @PutMapping("/marcar-todas/{usuarioId}")
    public ResponseEntity<String> marcarTodas(@PathVariable Integer usuarioId) {

        List<UsuarioNotificacion> lista =
                usuarioNotificacionRepository.findByUsuario_Id(usuarioId);

        lista.forEach(n -> n.setLeido(true));

        usuarioNotificacionRepository.saveAll(lista);

        return ResponseEntity.ok("Todas las notificaciones leídas");
    }
}