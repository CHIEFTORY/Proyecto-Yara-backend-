package com.yara.repositories;

import com.yara.entities.GrupoUsuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GrupoUsuarioRepository
        extends JpaRepository<GrupoUsuario, Integer> {
}