package com.yara.dtos;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class BalanceDTO {

    private String usuario;

    private BigDecimal balance;
}