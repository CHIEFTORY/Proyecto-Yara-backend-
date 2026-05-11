package com.yara.entities;

import com.yara.entities.authYuser.Usuario;
import com.yara.enums.RolGrupo;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "grupo_usuario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GrupoUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "grupo_id")
    private Grupo grupo;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Enumerated(EnumType.STRING)

    @Column(name = "rol_grupo")
    private RolGrupo rolGrupo;

    @Column(name = "fecha_union")
    private LocalDateTime fechaUnion;
}