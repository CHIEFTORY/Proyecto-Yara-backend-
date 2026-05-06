package com.yara.services;

import com.yara.dtos.CrearGrupoDTO;
import com.yara.entities.Grupo;
import com.yara.entities.GrupoUsuario;
import com.yara.entities.Usuario;
import com.yara.repositories.GrupoRepository;
import com.yara.repositories.GrupoUsuarioRepository;
import com.yara.repositories.UsuarioRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class GrupoService {

    private final GrupoRepository grupoRepository;
    private final UsuarioRepository usuarioRepository;
    private final GrupoUsuarioRepository grupoUsuarioRepository;

    public GrupoService(
            GrupoRepository grupoRepository,
            UsuarioRepository usuarioRepository,
            GrupoUsuarioRepository grupoUsuarioRepository
    ) {
        this.grupoRepository = grupoRepository;
        this.usuarioRepository = usuarioRepository;
        this.grupoUsuarioRepository = grupoUsuarioRepository;
    }

    public Grupo crearGrupo(CrearGrupoDTO dto) {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        Usuario usuario = usuarioRepository
                .findByEmail(email)
                .orElseThrow();

        Grupo grupo = Grupo.builder()
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .creadoPor(usuario)
                .estado("ACTIVO")
                .fechaCreacion(LocalDateTime.now())
                .build();

        Grupo grupoGuardado = grupoRepository.save(grupo);

        GrupoUsuario grupoUsuario = GrupoUsuario.builder()
                .grupo(grupoGuardado)
                .usuario(usuario)
                .rolGrupo("ADMIN")
                .fechaUnion(LocalDateTime.now())
                .build();

        grupoUsuarioRepository.save(grupoUsuario);

        return grupoGuardado;
    }
}