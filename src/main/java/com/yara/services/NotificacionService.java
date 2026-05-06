package com.yara.services;

import com.yara.entities.Notificacion;
import com.yara.entities.Usuario;
import com.yara.entities.UsuarioNotificacion;
import com.yara.entities.UsuarioNotificacionId;
import com.yara.repositories.NotificacionRepository;
import com.yara.repositories.UsuarioNotificacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificacionService {

    private final NotificacionRepository notificacionRepository;
    private final UsuarioNotificacionRepository usuarioNotificacionRepository;

    public void notificarUsuarios(List<Usuario> usuarios, String mensaje, String tipo) {

        // 1. crear UNA notificación global
        Notificacion notificacion = Notificacion.builder()
                .mensaje(mensaje)
                .tipo(tipo)
                .fecha(LocalDateTime.now())
                .build();

        Notificacion guardada = notificacionRepository.save(notificacion);

        // 2. asignarla a cada usuario
        List<UsuarioNotificacion> relaciones = usuarios.stream()
                .map(u -> UsuarioNotificacion.builder()
                        .id(new UsuarioNotificacionId(u.getId(), guardada.getId()))
                        .usuario(u)
                        .notificacion(guardada)
                        .leido(false)
                        .build())
                .toList();

        usuarioNotificacionRepository.saveAll(relaciones);
    }

    public List<UsuarioNotificacion> listarPorUsuario(Integer usuarioId) {
        return usuarioNotificacionRepository
                .findByUsuarioIdOrderByNotificacionFechaDesc(usuarioId);
    }
}
