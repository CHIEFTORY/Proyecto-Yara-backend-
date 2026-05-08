package com.yara.repositories;

import com.yara.entities.authYuser.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    Optional<Usuario> findByEmail(String email);
    List<Usuario> findTop10ByNombreContainingIgnoreCaseOrEmailContainingIgnoreCase(
            String nombre,
            String email
    );

}