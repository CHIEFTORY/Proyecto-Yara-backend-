package com.yara.entities;

import jakarta.persistence.*;
import lombok.*;
import jakarta.persistence.Id;

import java.math.BigDecimal;

@Entity
@Table(name = "gasto_categoria_rel")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GastoCategoriaRel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "gasto_id")
    private Gasto gasto;

    @ManyToOne
    @JoinColumn(name = "categoria_id")
    private GastoCategoria categoria;

    // 🔥 ESTE ES EL NUEVO CAMPO
    private BigDecimal monto;
}