package com.yara.entities;

import com.yara.entities.authYuser.Usuario;
import com.yara.enums.EstadoGasto;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "gasto")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Gasto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String descripcion;

    @Column(name = "monto_total")
    private BigDecimal montoTotal;

    @ManyToOne
    @JoinColumn(name = "grupo_id")
    private Grupo grupo;

    @ManyToOne
    @JoinColumn(name = "pagado_por")
    private Usuario pagadoPor;

    @Column(name = "tipo_division")
    private String tipoDivision;

    @Enumerated(EnumType.STRING)
    private EstadoGasto estado;

    private LocalDateTime fecha;
}