package com.yara.repositories;

import com.yara.entities.MetodoPago;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MetodoPagoRepository
        extends JpaRepository<MetodoPago, Integer> {
}