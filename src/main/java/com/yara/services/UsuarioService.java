package com.yara.services;

import com.yara.dtos.ChangePasswordRequest;
import com.yara.dtos.UpdateProfileRequest;
import com.yara.dtos.UserProfileResponse;
import com.yara.dtos.UsuarioResponseDTO;
import com.yara.entities.Usuario;
import com.yara.repositories.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final AuditoriaService auditoriaService;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            AuditoriaService auditoriaService,
            PasswordEncoder passwordEncoder
    ) {
        this.usuarioRepository = usuarioRepository;
        this.auditoriaService = auditoriaService;
        this.passwordEncoder = passwordEncoder;
    }

    // 🔹 LISTAR
    public List<UsuarioResponseDTO> listarUsuarios() {
        return usuarioRepository.findAll().stream()
                .map(usuario -> UsuarioResponseDTO.builder()
                        .id(usuario.getId())
                        .nombre(usuario.getNombre())
                        .email(usuario.getEmail())
                        .telefono(usuario.getTelefono())
                        .estado(usuario.getEstado())
                        .build())
                .toList();
    }

    // 🔹 PERFIL
    public UserProfileResponse getProfile(Integer userId) {
        Usuario user = usuarioRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return new UserProfileResponse(
                user.getId(),
                user.getNombre(),
                user.getEmail(),
                user.getTelefono()
        );
    }

    // 🔹 ACTUALIZAR PERFIL
    public void updateProfile(Integer userId, UpdateProfileRequest req) {
        Usuario user = usuarioRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        user.setNombre(req.getNombre());
        user.setTelefono(req.getTelefono());

        usuarioRepository.save(user);

        auditoriaService.registrar(
                userId,
                "UPDATE",
                "USUARIO",
                userId,
                "Actualizó perfil"
        );
    }

    // 🔹 CAMBIAR PASSWORD
    public void changePassword(Integer userId, ChangePasswordRequest req) {
        Usuario user = usuarioRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!passwordEncoder.matches(req.getPasswordActual(), user.getPasswordHash())) {
            throw new RuntimeException("Password incorrecta");
        }

        user.setPasswordHash(passwordEncoder.encode(req.getNuevaPassword()));

        usuarioRepository.save(user);

        auditoriaService.registrar(
                userId,
                "UPDATE",
                "USUARIO",
                userId,
                "Cambió contraseña"
        );
    }
}