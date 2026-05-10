package com.yara.entities.authYuser;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "usuario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false, unique = true)
    private String email;

    private String telefono;
    @JsonIgnore
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    private String estado;

    @Column(name = "mfa_enabled")
    private Boolean mfaEnabled;

    @Column(name = "otp_code")
    private String otpCode;

    @Column(name = "otp_expiration")
    private LocalDateTime otpExpiration;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;


    @Column(name = "culqi_customer_id")
    private String culqiCustomerId;

    @OneToMany(
            mappedBy = "usuario",
            fetch = FetchType.EAGER
    )


    private List<UsuarioRol> usuarioRoles;
}
