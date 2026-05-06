package com.yara.repositories;

import com.yara.entities.UsuarioRol;
import com.yara.entities.UsuarioRolId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UsuarioRolRepository
        extends JpaRepository<UsuarioRol, UsuarioRolId> {

    List<UsuarioRol> findByUsuario_Id(Integer usuarioId);
}