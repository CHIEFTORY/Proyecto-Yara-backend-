package com.yara.config;

import com.yara.entities.Usuario;
import com.yara.repositories.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {

        usuarioRepository.findAll().forEach(usuario -> {

            if (!usuario.getPasswordHash().startsWith("$2a$")) {

                usuario.setPasswordHash(
                        passwordEncoder.encode(usuario.getPasswordHash())
                );

                usuarioRepository.save(usuario);
            }
        });

        System.out.println("Passwords encriptadas correctamente.");
    }
}