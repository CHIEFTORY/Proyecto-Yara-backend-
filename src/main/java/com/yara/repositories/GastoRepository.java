package com.yara.repositories;

import com.yara.entities.Gasto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GastoRepository
        extends JpaRepository<Gasto, Integer> {
    List<Gasto> findByGrupoIdAndEstado(Integer grupoId, String estado);
    Page<Gasto> findByGrupoIdAndEstado(Integer grupoId, String estado, Pageable pageable);



}