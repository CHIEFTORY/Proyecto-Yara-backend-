package com.yara.repositories;

import com.yara.entities.Pago;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PagoRepository
        extends JpaRepository<Pago, Integer> {
    List<Pago> findByGrupoId(Integer grupoId);
    List<Pago> findByGrupoIdOrderByFechaDesc(Integer grupoId);
}