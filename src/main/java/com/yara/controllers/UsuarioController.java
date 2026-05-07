package com.yara.controllers;

import com.yara.dtos.ChangePasswordRequest;
import com.yara.dtos.UpdateProfileRequest;
import com.yara.dtos.UserProfileResponse;
import com.yara.dtos.UsuarioResponseDTO;
import com.yara.services.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // 🔹 LISTAR
    @GetMapping
    public ResponseEntity<List<UsuarioResponseDTO>> listarUsuarios() {

        return ResponseEntity.ok(
                usuarioService.listarUsuarios()
        );
    }

    // 🔹 PERFIL
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getProfile(
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                usuarioService.getProfileByEmail(
                        authentication.getName()
                )
        );
    }

    // 🔹 ACTUALIZAR PERFIL
    @PutMapping("/me")
    public ResponseEntity<String> updateProfile(
            Authentication authentication,
            @RequestBody UpdateProfileRequest req
    ) {

        usuarioService.updateProfileByEmail(
                authentication.getName(),
                req
        );

        return ResponseEntity.ok("Perfil actualizado");
    }

    // 🔹 CAMBIAR PASSWORD
    @PutMapping("/me/password")
    public ResponseEntity<String> changePassword(
            Authentication authentication,
            @RequestBody ChangePasswordRequest req
    ) {

        usuarioService.changePasswordByEmail(
                authentication.getName(),
                req
        );

        return ResponseEntity.ok("Password actualizada");
    }
}