package com.yara.dtos;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class MetodoPagoDTO {

    private Integer id;

    private String proveedor;

    private String cardBrand;

    private String cardLast4;

    private Boolean predeterminado;
}