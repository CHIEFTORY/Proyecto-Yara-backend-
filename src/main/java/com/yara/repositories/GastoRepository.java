package com.yara.repositories;

import com.yara.entities.Gasto;
import com.yara.enums.EstadoGasto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GastoRepository
        extends JpaRepository<Gasto, Integer> {
    List<Gasto> findByGrupoIdAndEstado(Integer grupoId, EstadoGasto estado);
    Page<Gasto> findByGrupoIdAndEstado(Integer grupoId, EstadoGasto estado, Pageable pageable);



}