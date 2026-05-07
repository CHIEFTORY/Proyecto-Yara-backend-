package com.yara.dtos.auth;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthUserDTO {

    private Integer id;

    private String nombre;

    private String email;
}
