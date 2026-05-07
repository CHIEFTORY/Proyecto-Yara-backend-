package com.yara.services;

import com.yara.entities.Auditoria;
import com.yara.repositories.AuditoriaRepository;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Getter
@Setter
public class AuditoriaService {

    @Autowired
    private AuditoriaRepository auditoriaRepository;

    public void registrar(Integer usuarioId, String accion, String entidad, Integer entidadId, String detalle) {

        Auditoria audit = new Auditoria();
        audit.setUsuarioId(usuarioId);
        audit.setAccion(accion);
        audit.setEntidad(entidad);
        audit.setEntidadId(entidadId);
        audit.setDetalle(detalle);

        auditoriaRepository.save(audit);
    }
}
