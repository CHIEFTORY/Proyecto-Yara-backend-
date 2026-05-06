package com.yara.services;

import com.yara.entities.RolPermiso;
import com.yara.entities.UsuarioRol;
import com.yara.repositories.RolPermisoRepository;
import com.yara.repositories.UsuarioRolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SeguridadService {

    private final UsuarioRolRepository usuarioRolRepository;
    private final RolPermisoRepository rolPermisoRepository;

    public boolean tienePermiso(Integer usuarioId, String permisoNombre) {

        List<UsuarioRol> roles = usuarioRolRepository.findByUsuario_Id(usuarioId);

        for (UsuarioRol ur : roles) {

            List<RolPermiso> permisos =
                    rolPermisoRepository.findByRol_Id(ur.getRol().getId());

            for (RolPermiso rp : permisos) {
                if (rp.getPermiso().getNombre().equalsIgnoreCase(permisoNombre)) {
                    return true;
                }
            }
        }

        return false;
    }
}
