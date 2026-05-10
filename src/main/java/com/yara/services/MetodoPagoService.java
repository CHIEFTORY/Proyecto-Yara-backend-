package com.yara.services;

import com.yara.dtos.GuardarMetodoPagoDTO;
import com.yara.dtos.MetodoPagoDTO;
import com.yara.entities.MetodoPago;
import com.yara.entities.authYuser.Usuario;
import com.yara.enums.EstadoGasto;
import com.yara.enums.EstadoMetodoPago;
import com.yara.exceptions.BusinessException;
import com.yara.repositories.MetodoPagoRepository;
import com.yara.repositories.UsuarioRepository;
import lombok.Builder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class MetodoPagoService {

    private final MetodoPagoRepository
            metodoPagoRepository;

    private final UsuarioRepository
            usuarioRepository;
    private final CulqiService
            culqiService;

    public MetodoPagoService(
            MetodoPagoRepository metodoPagoRepository,
            UsuarioRepository usuarioRepository,
            CulqiService culqiService
    ) {

        this.metodoPagoRepository =
                metodoPagoRepository;

        this.usuarioRepository =
                usuarioRepository;
        this.culqiService = culqiService;
    }

    public List<MetodoPagoDTO>
    listarMisMetodos() {

        Authentication auth =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        String email =
                auth.getName();

        Usuario usuario =
                usuarioRepository
                        .findByEmail(email)
                        .orElseThrow();

        return metodoPagoRepository
                .findByUsuarioIdAndEstado(
                        usuario.getId(),
                        EstadoMetodoPago.ACTIVO
                )
                .stream()
                .map(mp ->

                        MetodoPagoDTO.builder()

                                .id(mp.getId())

                                .proveedor(
                                        mp.getProveedor()
                                )

                                .cardBrand(
                                        mp.getCardBrand()
                                )

                                .cardLast4(
                                        mp.getCardLast4()
                                )

                                .predeterminado(
                                        mp.getPredeterminado()
                                )

                                .build()
                )
                .toList();
    }

    public MetodoPagoDTO
    guardarMetodoPago(

            GuardarMetodoPagoDTO dto
    ) {

        String email =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getName();

        Usuario usuario =
                usuarioRepository
                        .findByEmail(email)
                        .orElseThrow();

        String customerId;

        Map<String, Object> card;

        try {

            customerId =
                    usuario.getCulqiCustomerId();

            if (
                    customerId == null
                            || customerId.isBlank()
            ) {

                customerId =
                        culqiService.crearCustomer(
                                usuario.getEmail()
                        );

                usuario.setCulqiCustomerId(
                        customerId
                );

                usuarioRepository.save(usuario);
            }
            System.out.println("CUSTOMER ID:");
            System.out.println(customerId);

            System.out.println("TOKEN:");
            System.out.println(dto.getCulqiToken());

            card =
                    culqiService.crearCard(

                            customerId,

                            dto.getCulqiToken()
                    );
            System.out.println("CARD RESPONSE:");
            System.out.println(card);

        } catch (Exception e) {

            e.printStackTrace();

            throw new BusinessException(
                    "Error creando tarjeta en Culqi"
            );
        }
        Map<String, Object> source =
                (Map<String, Object>) card.get("source");

        String culqiCardId =
                card.get("id")
                        .toString();

        MetodoPago existente =
                metodoPagoRepository
                        .findByCulqiCardId(
                                culqiCardId
                        )
                        .orElse(null);

        if (existente != null) {

            return MetodoPagoDTO.builder()

                    .id(existente.getId())

                    .cardBrand(
                            existente.getCardBrand()
                    )

                    .cardLast4(
                            existente.getCardLast4()
                    )

                    .predeterminado(
                            existente.getPredeterminado()
                    )

                    .build();
        }

        Map<String, Object> iin =
                (Map<String, Object>) source.get("iin");

        List<MetodoPago> metodosActivos =
                metodoPagoRepository
                        .findByUsuarioIdAndEstado(
                                usuario.getId(),
                                EstadoMetodoPago.ACTIVO
                        );

        for (MetodoPago metodo : metodosActivos) {

            metodo.setPredeterminado(false);
        }

        metodoPagoRepository.saveAll(
                metodosActivos
        );

        MetodoPago metodoPago =
                MetodoPago.builder()

                        .usuario(usuario)

                        .proveedor("CULQI")

                        .cardBrand(
                                iin.get("card_brand")
                                        .toString()
                        )

                        .cardLast4(
                                source.get("last_four")
                                        .toString()
                        )

                        .culqiCustomerId(
                                customerId
                        )

                        .culqiCardId(
                                culqiCardId
                        )

                        .predeterminado(true)

                        .estado(
                                EstadoMetodoPago.ACTIVO
                        )

                        .creadoEn(LocalDateTime.now())

                        .build();
        MetodoPago guardado =
                metodoPagoRepository
                        .save(metodoPago);

        return MetodoPagoDTO.builder()

                .id(guardado.getId())

                .cardBrand(
                        guardado.getCardBrand()
                )

                .cardLast4(
                        guardado.getCardLast4()
                )

                .predeterminado(
                        guardado.getPredeterminado()
                )

                .build();
    }


    public void eliminarMetodoPago(
            Integer metodoPagoId
    ) {

        String email =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getName();

        Usuario usuario =
                usuarioRepository
                        .findByEmail(email)
                        .orElseThrow();

        MetodoPago metodo =
                metodoPagoRepository
                        .findByIdAndEstado(
                                metodoPagoId,
                                EstadoMetodoPago.ACTIVO
                        )
                        .orElseThrow(() ->
                                new BusinessException(
                                        "Tarjeta no encontrada"
                                )
                        );

        if (!metodo.getUsuario().getId()
                .equals(usuario.getId())) {

            throw new BusinessException(
                    "No puedes eliminar esta tarjeta"
            );
        }

        metodo.setEstado(
                EstadoMetodoPago.ELIMINADO
        );

        metodo.setPredeterminado(false);

        metodoPagoRepository.save(metodo);

        List<MetodoPago> restantes =
                metodoPagoRepository
                        .findByUsuarioIdAndEstado(
                                usuario.getId(),
                                EstadoMetodoPago.ACTIVO
                        );

        boolean existePrincipal =
                restantes.stream()
                        .anyMatch(
                                MetodoPago::getPredeterminado
                        );

        if (!existePrincipal
                && !restantes.isEmpty()) {

            MetodoPago nuevaPrincipal =
                    restantes.get(0);

            nuevaPrincipal
                    .setPredeterminado(true);

            metodoPagoRepository
                    .save(nuevaPrincipal);
        }
    }
}