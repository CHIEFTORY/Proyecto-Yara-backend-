package com.yara.dtos.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyOTPDTO {

    @NotBlank(message = "El email es obligatorio")
    private String email;

    @NotBlank(message = "El código OTP es obligatorio")
    private String otp;
}