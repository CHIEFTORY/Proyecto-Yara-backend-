package com.yara.controllers;

import com.yara.dtos.LoginRequest;
import com.yara.dtos.RegisterDTO;
import com.yara.entities.Rol;
import com.yara.entities.Usuario;
import com.yara.entities.UsuarioRol;
import com.yara.entities.UsuarioRolId;
import com.yara.repositories.RolRepository;
import com.yara.repositories.UsuarioRepository;
import com.yara.repositories.UsuarioRolRepository;
import com.yara.security.JwtService;

import lombok.Builder;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController

@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final RolRepository rolRepository;
    private final UsuarioRolRepository usuarioRolRepository;


    public AuthController(
            AuthenticationManager authenticationManager,
            JwtService jwtService
            , UsuarioRepository usuarioRepository
            , PasswordEncoder passwordEncoder
            , RolRepository rolRepository
            , UsuarioRolRepository usuarioRolRepository
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.rolRepository = rolRepository;
        this.usuarioRolRepository = usuarioRolRepository;
    }

    @PostMapping("/login")
    public Map<String, String> login(
            @RequestBody LoginRequest request
    ) {

        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.getEmail(),
                                request.getPassword()
                        )
                );

        if (authentication.isAuthenticated()) {

            String token =
                    jwtService.generateToken(request.getEmail());

            return Map.of(
                    "token",
                    token
            );
        }

        throw new RuntimeException("Credenciales incorrectas");
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterDTO dto) {

        if (usuarioRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new RuntimeException("El email ya está registrado");
        }

        // 🔥 1. CREAR USUARIO
        Usuario usuario = Usuario.builder()
                .nombre(dto.getNombre())
                .email(dto.getEmail())
                .telefono(dto.getTelefono())
                .passwordHash(passwordEncoder.encode(dto.getPassword()))
                .estado("ACTIVO")
                .fechaCreacion(LocalDateTime.now())
                .build();

        Usuario usuarioGuardado = usuarioRepository.save(usuario);

        // 🔥 2. ASIGNAR ROL MIEMBRO (AQUÍ VA LO TUYO)
        Rol rolMiembro = rolRepository.findByNombre("MIEMBRO")
                .orElseThrow(() -> new RuntimeException("Rol no encontrado"));

        UsuarioRol ur = UsuarioRol.builder()
                .id(new UsuarioRolId(usuarioGuardado.getId(), rolMiembro.getId()))
                .usuario(usuarioGuardado)
                .rol(rolMiembro)
                .build();

        usuarioRolRepository.save(ur);

        return ResponseEntity.ok("Usuario registrado correctamente");
    }
}