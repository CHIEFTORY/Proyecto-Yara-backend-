package com.yara.dtos;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangePasswordRequest {
    private String passwordActual;
    private String nuevaPassword;
}
