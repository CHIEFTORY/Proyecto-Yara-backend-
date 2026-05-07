package com.yara.dtos;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuditoriaResponseDTO {

    private Integer id;
    private Integer usuarioId;
    private String accion;
    private String entidad;
    private Integer entidadId;
    private String detalle;
}