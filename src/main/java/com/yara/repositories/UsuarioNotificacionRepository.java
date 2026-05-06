package com.yara.repositories;

import com.yara.entities.UsuarioNotificacion;
import com.yara.entities.UsuarioNotificacionId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UsuarioNotificacionRepository extends JpaRepository<UsuarioNotificacion, UsuarioNotificacionId> {

    List<UsuarioNotificacion> findByUsuarioIdOrderByNotificacionFechaDesc(Integer usuarioId);
    List<UsuarioNotificacion> findByUsuario_Id(Integer usuarioId);
}
