package com.yara.repositories;

import com.yara.entities.MetodoPago;
import com.yara.enums.EstadoMetodoPago;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MetodoPagoRepository
        extends JpaRepository<MetodoPago, Integer> {

    List<MetodoPago>
    findByUsuarioId(Integer usuarioId);
    Optional<MetodoPago>
    findByCulqiCardId(String culqiCardId);

    List<MetodoPago>
    findByUsuarioIdAndEstado(
            Integer usuarioId,
            EstadoMetodoPago estado
    );

    Optional<MetodoPago>
    findByIdAndEstado(
            Integer id,
            EstadoMetodoPago estado
    );

    Optional<MetodoPago>
    findByIdAndUsuarioIdAndEstado(
            Integer id,
            Integer usuarioId,
            EstadoMetodoPago estado
    );


}