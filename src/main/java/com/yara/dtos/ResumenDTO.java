package com.yara.dtos;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class ResumenDTO {

    private List<BalanceDTO> balances;

    private List<DeudaDTO> deudas;
    private String nombre;
    private List<PagoResponseDTO> pagos;

    private BigDecimal totalGastos;
    private List<GastoResponseDTO> gastos;
}