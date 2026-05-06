package com.yara.entities;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PagoDetalleId implements Serializable {

    private Integer pagoId;

    private Integer metodoId;
}