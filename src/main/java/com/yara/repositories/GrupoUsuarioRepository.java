package com.yara.repositories;

import com.yara.entities.GrupoUsuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GrupoUsuarioRepository
        extends JpaRepository<GrupoUsuario, Integer> {
    boolean existsByGrupoIdAndUsuarioId(Integer grupoId, Integer usuarioId);
    Optional<GrupoUsuario> findByGrupo_IdAndUsuario_Id(Integer grupoId, Integer usuarioId);
    List<GrupoUsuario> findByGrupo_Id(Integer grupoId);
    List<GrupoUsuario> findByUsuario_Id(Integer usuarioId);
    long countByGrupo_IdAndRolGrupo(Integer grupoId, String rolGrupo);

    List<GrupoUsuario> findByGrupoId(
            Integer grupoId
    );

    List<GrupoUsuario> findByUsuarioId(
            Integer usuarioId
    );
}