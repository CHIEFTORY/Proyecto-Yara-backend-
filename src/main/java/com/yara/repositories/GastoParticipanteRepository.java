package com.yara.repositories;

import com.yara.entities.GastoParticipante;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GastoParticipanteRepository
        extends JpaRepository<GastoParticipante, Integer> {
    List<GastoParticipante> findByGastoGrupoId(Integer grupoId);
    void deleteByGastoId(Integer gastoId);
}