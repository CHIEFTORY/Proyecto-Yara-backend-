package com.yara.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterDTO {
    private String nombre;
    private String email;
    private String telefono;
    private String password;
}
