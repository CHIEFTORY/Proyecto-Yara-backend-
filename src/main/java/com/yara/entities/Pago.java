package com.yara.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "pago")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "grupo_id")
    private Grupo grupo;

    @ManyToOne
    @JoinColumn(name = "deudor_id")
    private Usuario deudor;

    @ManyToOne
    @JoinColumn(name = "acreedor_id")
    private Usuario acreedor;

    private BigDecimal monto;

    private String estado;

    private LocalDateTime fecha;

    @OneToMany(mappedBy = "pago")
    private Set<PagoDetalle> pagoDetalles;
}