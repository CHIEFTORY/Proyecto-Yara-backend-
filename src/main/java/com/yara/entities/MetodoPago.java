package com.yara.entities;

import com.yara.entities.authYuser.Usuario;
import com.yara.enums.EstadoMetodoPago;
import jakarta.persistence.*;

import lombok.*;

import java.time.LocalDateTime;
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "metodo_pago")
public class MetodoPago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String proveedor;

    private String cardBrand;

    private String cardLast4;

    private String culqiCustomerId;

    private String culqiCardId;

    private Boolean predeterminado;

    @Enumerated(EnumType.STRING)
    private EstadoMetodoPago estado;

    private LocalDateTime creadoEn;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;
}