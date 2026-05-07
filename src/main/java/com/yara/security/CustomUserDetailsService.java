package com.yara.security;

import com.yara.entities.authYuser.Usuario;
import com.yara.repositories.UsuarioRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public CustomUserDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException("Credenciales inválidas"));

        // 🔥 VALIDAR ESTADO
        if (!"ACTIVO".equalsIgnoreCase(usuario.getEstado())) {
            throw new UsernameNotFoundException("Usuario inactivo");
        }

        return User.builder()
                .username(usuario.getEmail())
                .password(usuario.getPasswordHash())

                .authorities(
                        usuario.getUsuarioRoles()
                                .stream()
                                .map(usuarioRol ->
                                        "ROLE_" +
                                                usuarioRol.getRol().getNombre()
                                )
                                .toArray(String[]::new)
                )

                .build();
    }
}