package com.yara.repositories;

import com.yara.entities.Grupo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GrupoRepository
        extends JpaRepository<Grupo, Integer> {
}