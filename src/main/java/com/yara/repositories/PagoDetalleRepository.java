package com.yara.repositories;

import com.yara.entities.PagoDetalle;
import com.yara.entities.PagoDetalleId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PagoDetalleRepository
        extends JpaRepository<PagoDetalle, PagoDetalleId> {
}