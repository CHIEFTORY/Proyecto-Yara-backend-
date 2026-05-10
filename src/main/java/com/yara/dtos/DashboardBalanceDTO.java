package com.yara.dtos;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardBalanceDTO {

    private BigDecimal totalDebes;

    private BigDecimal totalTeDeben;

    private BigDecimal balanceGeneral;
}