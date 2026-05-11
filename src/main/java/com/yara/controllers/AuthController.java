package com.yara.controllers;

import com.yara.dtos.auth.AuthUserDTO;
import com.yara.dtos.auth.LoginRequest;
import com.yara.dtos.auth.RegisterDTO;
import com.yara.entities.authYuser.Rol;
import com.yara.entities.authYuser.Usuario;
import com.yara.entities.authYuser.UsuarioRol;
import com.yara.entities.authYuser.UsuarioRolId;
import com.yara.exceptions.BusinessException;
import com.yara.repositories.RolRepository;
import com.yara.repositories.UsuarioRepository;
import com.yara.repositories.UsuarioRolRepository;
import com.yara.security.JwtService;
import com.yara.security.RateLimitService;
import com.yara.services.EmailService;
import com.yara.dtos.auth.VerifyOTPDTO;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
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
    private final RateLimitService rateLimitService;
    private final EmailService emailService;

    public AuthController(
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            RolRepository rolRepository,
            UsuarioRolRepository usuarioRolRepository,
            RateLimitService rateLimitService,
            EmailService emailService
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.rolRepository = rolRepository;
        this.usuarioRolRepository = usuarioRolRepository;
        this.rateLimitService = rateLimitService;
        this.emailService = emailService;
    }

    @PostMapping("/login")
    public Map<String, String> login(
            @Valid @RequestBody LoginRequest request
    ) {

        // 🔥 RATE LIMIT
        if (!rateLimitService.allowAttempt(request.getEmail())) {
            throw new RuntimeException(
                    "Demasiados intentos. Intenta nuevamente en 1 minuto"
            );
        }

        try {

            Authentication authentication =
                    authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(
                                    request.getEmail(),
                                    request.getPassword()
                            )
                    );

            if (authentication.isAuthenticated()) {

                Usuario usuario = usuarioRepository
                        .findByEmail(request.getEmail())
                        .orElseThrow();

                // 🔥 MFA ACTIVADO
                if (Boolean.TRUE.equals(usuario.getMfaEnabled())) {

                    // 🔥 GENERAR OTP
                    String otp = generarOTP();

                    // 🔥 GUARDAR OTP
                    usuario.setOtpCode(otp);

                    usuario.setOtpExpiration(
                            LocalDateTime.now().plusMinutes(5)
                    );

                    usuarioRepository.save(usuario);

                    // 🔥 ENVIAR EMAIL
                    emailService.enviarOTP(
                            usuario.getEmail(),
                            otp
                    );

                    return Map.of(
                            "mfaRequired",
                            "true"
                    );
                }

                // 🔥 LOGIN NORMAL
                String token =
                        jwtService.generateToken(request.getEmail());

                return Map.of(
                        "token",
                        token
                );
            }

        } catch (AuthenticationException ex) {

            throw new RuntimeException("Credenciales inválidas");
        }

        throw new RuntimeException("Credenciales inválidas");
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(
            @Valid @RequestBody RegisterDTO dto
    ) {

        if (usuarioRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new BusinessException(
                    "Este correo ya está registrado"
            );
        }

        // 🔥 CREAR USUARIO
        Usuario usuario = Usuario.builder()
                .nombre(dto.getNombre())
                .email(dto.getEmail())
                .telefono(dto.getTelefono())
                .passwordHash(passwordEncoder.encode(dto.getPassword()))
                .estado("ACTIVO")

                // 🔥 MFA
                .mfaEnabled(false)

                .fechaCreacion(LocalDateTime.now())
                .build();

        Usuario usuarioGuardado = usuarioRepository.save(usuario);

        // 🔥 ASIGNAR ROL MIEMBRO
        Rol rolMiembro = rolRepository.findByNombre("MIEMBRO")
                .orElseThrow(() ->
                        new RuntimeException("Rol no encontrado"));

        UsuarioRol ur = UsuarioRol.builder()
                .id(new UsuarioRolId(
                        usuarioGuardado.getId(),
                        rolMiembro.getId()
                ))
                .usuario(usuarioGuardado)
                .rol(rolMiembro)
                .build();

        usuarioRolRepository.save(ur);

        return ResponseEntity.ok(
                "Usuario registrado correctamente"
        );
    }

    // 🔥 GENERAR OTP
    private String generarOTP() {

        int codigo =
                (int)(Math.random() * 900000) + 100000;

        return String.valueOf(codigo);
    }

    @PostMapping("/verify-otp")
    public Map<String, String> verifyOTP(
            @Valid @RequestBody VerifyOTPDTO dto
    ) {

        Usuario usuario = usuarioRepository
                .findByEmail(dto.getEmail())
                .orElseThrow(() ->
                        new RuntimeException("Usuario no encontrado"));

        // 🔥 VALIDAR OTP
        if (
                usuario.getOtpCode() == null ||
                        !usuario.getOtpCode().equals(dto.getOtp())
        ) {

            throw new RuntimeException("OTP inválido");
        }

        // 🔥 VALIDAR EXPIRACIÓN
        if (
                usuario.getOtpExpiration() == null ||
                        usuario.getOtpExpiration()
                                .isBefore(LocalDateTime.now())
        ) {

            throw new RuntimeException("OTP expirado");
        }

        // 🔥 LIMPIAR OTP
        usuario.setOtpCode(null);
        usuario.setOtpExpiration(null);

        usuarioRepository.save(usuario);

        // 🔥 GENERAR JWT
        String token =
                jwtService.generateToken(usuario.getEmail());

        return Map.of(
                "token",
                token
        );
    }

    @GetMapping("/me")
    public AuthUserDTO getMe(
            Principal principal
    ) {

        Usuario usuario = usuarioRepository
                .findByEmail(principal.getName())
                .orElseThrow(() ->
                        new RuntimeException("Usuario no encontrado"));

        return AuthUserDTO.builder()
                .id(usuario.getId())
                .nombre(usuario.getNombre())
                .email(usuario.getEmail())
                .build();
    }
}