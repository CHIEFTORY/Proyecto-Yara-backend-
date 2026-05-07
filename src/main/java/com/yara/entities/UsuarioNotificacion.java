package com.yara.entities;

import com.yara.entities.authYuser.Usuario;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "usuario_notificacion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioNotificacion {

    @EmbeddedId
    private UsuarioNotificacionId id;

    @ManyToOne
    @MapsId("usuarioId")
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToOne
    @MapsId("notificacionId")
    @JoinColumn(name = "notificacion_id")
    private Notificacion notificacion;

    private Boolean leido;
}