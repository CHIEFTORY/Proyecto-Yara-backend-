package com.yara.repositories;

import com.yara.entities.authYuser.UsuarioRol;
import com.yara.entities.authYuser.UsuarioRolId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UsuarioRolRepository
        extends JpaRepository<UsuarioRol, UsuarioRolId> {

    List<UsuarioRol> findByUsuario_Id(Integer usuarioId);
}