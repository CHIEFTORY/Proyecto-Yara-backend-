package com.yara.dtos.usuario;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UsuarioResponseDTO {

    private Integer id;
    private String nombre;
    private String email;
    private String telefono;
    private String estado;
}