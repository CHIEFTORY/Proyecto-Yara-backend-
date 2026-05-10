package com.yara.dtos;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GrupoPreviewDTO {

    private Integer id;

    private String nombre;

    private Integer cantidadMiembros;

    private BigDecimal miBalance;
}
