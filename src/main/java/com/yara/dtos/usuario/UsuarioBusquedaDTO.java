package com.yara.dtos.usuario;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioBusquedaDTO {

    private Integer id;
    private String nombre;
    private String email;
}