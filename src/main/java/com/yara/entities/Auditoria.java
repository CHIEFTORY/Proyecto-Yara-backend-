package com.yara.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "auditoria")
@Getter
@Setter
public class Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "usuario_id")
    private Integer usuarioId;

    private String accion;

    private String entidad;

    @Column(name = "entidad_id")
    private Integer entidadId;

    // 🔥 IMPORTANTE: la maneja la BD
    @Column(name = "fecha", insertable = false, updatable = false)
    private LocalDateTime fecha;

    @Column(columnDefinition = "json")
    private String detalle;
}
