package com.yara.dtos;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class DeudaDTO {

    private String deudor;

    private String acreedor;

    private BigDecimal monto;
}