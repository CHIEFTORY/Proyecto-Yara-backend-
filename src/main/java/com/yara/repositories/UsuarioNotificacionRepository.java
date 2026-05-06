package com.yara.repositories;

import com.yara.entities.UsuarioNotificacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UsuarioNotificacionRepository extends JpaRepository<UsuarioNotificacion, Integer> {

    List<UsuarioNotificacion> findByUsuarioIdOrderByNotificacionFechaDesc(Integer usuarioId);
}
