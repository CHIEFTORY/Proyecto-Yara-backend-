package com.yara.repositories;

import com.yara.entities.authYuser.RolPermiso;
import com.yara.entities.authYuser.RolPermisoId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RolPermisoRepository
        extends JpaRepository<RolPermiso, RolPermisoId> {

    List<RolPermiso> findByRol_Id(Integer rolId);
}
