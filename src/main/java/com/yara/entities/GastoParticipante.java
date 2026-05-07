package com.yara.entities;

import com.yara.entities.authYuser.Usuario;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "gasto_participante")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GastoParticipante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "gasto_id")
    private Gasto gasto;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    private BigDecimal monto;

    private BigDecimal porcentaje;

    private String estado;
}