package com.yara.repositories;

import com.yara.entities.Auditoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditoriaRepository extends JpaRepository<Auditoria, Integer> {

    List<Auditoria> findByUsuarioId(Integer usuarioId);
}
