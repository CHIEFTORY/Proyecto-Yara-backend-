package com.yara.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "pago_detalle")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagoDetalle {

    @EmbeddedId
    private PagoDetalleId id;

    @ManyToOne
    @MapsId("pagoId")
    @JoinColumn(name = "pago_id")
    private Pago pago;

    @ManyToOne
    @MapsId("metodoId")
    @JoinColumn(name = "metodo_id")
    private MetodoPago metodoPago;

    @Column(name = "comprobante_url")
    private String comprobanteUrl;
}