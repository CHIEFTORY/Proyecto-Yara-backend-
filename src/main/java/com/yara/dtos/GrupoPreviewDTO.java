package com.yara.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GrupoPreviewDTO {

    private Integer id;

    private String nombre;

    private Integer cantidadMiembros;
}
