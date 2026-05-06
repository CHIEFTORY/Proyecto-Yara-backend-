package com.yara.repositories;

import com.yara.entities.GastoCategoriaRel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GastoCategoriaRelRepository extends JpaRepository<GastoCategoriaRel, Integer> {

    List<GastoCategoriaRel> findByGastoId(Integer gastoId);
    void deleteByGastoId(Integer gastoId);
}