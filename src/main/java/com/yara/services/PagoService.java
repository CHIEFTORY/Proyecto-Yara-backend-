package com.yara.services;

import com.yara.dtos.CrearPagoDTO;
import com.yara.dtos.PagoResponseDTO;
import com.yara.entities.*;
import com.yara.entities.authYuser.Usuario;
import com.yara.enums.EstadoMetodoPago;
import com.yara.enums.EstadoPago;
import com.yara.exceptions.BusinessException;
import com.yara.repositories.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PagoService {

    private final PagoRepository pagoRepository;
    private final PagoDetalleRepository pagoDetalleRepository;
    private final MetodoPagoRepository metodoPagoRepository;
    private final UsuarioRepository usuarioRepository;
    private final GrupoRepository grupoRepository;
    private final GastoService gastoService;
    private final NotificacionService notificacionService;
    private final SeguridadService seguridadService;
    private final GrupoUsuarioRepository grupoUsuarioRepository;
    private final CulqiService culqiService ;
    public PagoService(
            PagoRepository pagoRepository,
            PagoDetalleRepository pagoDetalleRepository,
            MetodoPagoRepository metodoPagoRepository,
            UsuarioRepository usuarioRepository,
            GrupoRepository grupoRepository,
            GastoService gastoService
            , NotificacionService notificacionService
            , SeguridadService seguridadService,
            GrupoUsuarioRepository grupoUsuarioRepository
            , CulqiService culqiService

    ) {
        this.pagoRepository = pagoRepository;
        this.pagoDetalleRepository = pagoDetalleRepository;
        this.metodoPagoRepository = metodoPagoRepository;
        this.usuarioRepository = usuarioRepository;
        this.grupoRepository = grupoRepository;
        this.gastoService = gastoService;
        this.notificacionService = notificacionService;
        this.seguridadService = seguridadService;
        this.grupoUsuarioRepository = grupoUsuarioRepository;
        this.culqiService = culqiService;
    }
    @Transactional
    public PagoResponseDTO registrarPago(CrearPagoDTO dto) {

        // 🔥 1. Usuario logueado
        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        Usuario usuario = usuarioRepository
                .findByEmail(email)
                .orElseThrow(() -> new BusinessException("Usuario no encontrado"));

        // =========================
        // 🔐 OBTENER ENTIDADES
        // =========================

        Usuario deudor = usuarioRepository
                .findById(dto.getDeudorId())
                .orElseThrow(() -> new BusinessException("Deudor no encontrado"));

        Usuario acreedor = usuarioRepository
                .findById(dto.getAcreedorId())
                .orElseThrow(() -> new BusinessException("Acreedor no encontrado"));

        Grupo grupo = grupoRepository
                .findById(dto.getGrupoId())
                .orElseThrow(() -> new BusinessException("Grupo no encontrado"));

        MetodoPago metodoPago =
                metodoPagoRepository
                        .findByIdAndUsuarioIdAndEstado(

                                dto.getMetodoPagoId(),

                                usuario.getId(),

                                EstadoMetodoPago.ACTIVO
                        )
                        .orElseThrow(() ->
                                new BusinessException(
                                        "Método de pago inválido"
                                )
                        );

        // =========================
        // 🔐 SEGURIDAD
        // =========================

        // 🔥 SOLO EL DEUDOR PUEDE PAGAR
        if (!deudor.getEmail().equals(email)) {
            throw new BusinessException("No puedes pagar por otro usuario");
        }

        // 🔥 VALIDAR DEUDOR EN EL GRUPO
        grupoUsuarioRepository
                .findByGrupo_IdAndUsuario_Id(
                        dto.getGrupoId(),
                        deudor.getId()
                )
                .orElseThrow(() ->
                        new BusinessException("El deudor no pertenece al grupo"));

        // 🔥 VALIDAR ACREEDOR EN EL GRUPO
        grupoUsuarioRepository
                .findByGrupo_IdAndUsuario_Id(
                        dto.getGrupoId(),
                        acreedor.getId()
                )
                .orElseThrow(() ->
                        new BusinessException("El acreedor no pertenece al grupo"));

        // =========================
        // ❗ VALIDACIONES DE NEGOCIO
        // =========================

        // 🔥 NO PAGARSE A SÍ MISMO
        if (deudor.getId().equals(acreedor.getId())) {
            throw new BusinessException("No puedes pagarte a ti mismo");
        }

        // 🔥 VALIDAR DEUDA REAL
        BigDecimal balanceDeudor =
                gastoService
                        .obtenerBalanceUsuario(
                                dto.getGrupoId(),
                                deudor.getId()
                        );

        if (balanceDeudor.compareTo(BigDecimal.ZERO) >= 0) {
            throw new BusinessException("El usuario no tiene deuda");
        }

        BigDecimal deudaReal = balanceDeudor.abs();

        // 🔥 NO PAGAR MÁS DE LA DEUDA
        if (dto.getMonto().compareTo(deudaReal) > 0) {
            throw new BusinessException("El monto excede la deuda actual");
        }

        // =========================
        // 🧾 CREAR PAGO
        // =========================

        Pago pago = Pago.builder()
                .grupo(grupo)
                .deudor(deudor)
                .acreedor(acreedor)
                .monto(dto.getMonto())
                .estado(EstadoPago.PENDIENTE)
                .fecha(LocalDateTime.now())
                .build();

        Pago pagoGuardado =
                pagoRepository.save(pago);

        try {

            String chargeId =
                    culqiService.cobrarTarjeta(

                            metodoPago.getCulqiCardId(),

                            dto.getMonto()
                    );

            pagoGuardado.setEstado(
                    EstadoPago.CONFIRMADO
            );

            pagoGuardado.setCulqiChargeId(
                    chargeId
            );

            pagoRepository.save(
                    pagoGuardado
            );

        } catch (Exception e) {

            pagoGuardado.setEstado(
                    EstadoPago.FALLIDO
            );

            pagoGuardado.setErrorMensaje(
                    e.getMessage()
            );

            pagoRepository.save(
                    pagoGuardado
            );

            throw new BusinessException(
                    "No se pudo procesar el pago"
            );
        }

        // =========================
        // 📄 DETALLE DEL PAGO
        // =========================

        PagoDetalle detalle =
                PagoDetalle.builder()
                        .id(new PagoDetalleId(
                                pagoGuardado.getId(),
                                metodoPago.getId()
                        ))
                        .pago(pagoGuardado)
                        .metodoPago(metodoPago)
                        .comprobanteUrl(null)
                        .build();

        pagoDetalleRepository.save(detalle);

        // =========================
        // 🔔 NOTIFICACIONES
        // =========================

        notificacionService.notificarUsuarios(
                List.of(deudor, acreedor),
                deudor.getNombre() + " pagó S/" + dto.getMonto(),
                "PAGO"
        );

        // =========================
        // 📤 RESPONSE
        // =========================

        return PagoResponseDTO.builder()
                .id(pagoGuardado.getId())
                .deudor(deudor.getNombre())
                .acreedor(acreedor.getNombre())
                .monto(pagoGuardado.getMonto())
                .metodoPago(
                        metodoPago.getCardBrand()
                                + " **** "
                                + metodoPago.getCardLast4()
                )
                .estado(pagoGuardado.getEstado())
                .fecha(pagoGuardado.getFecha())
                .build();
    }

    public List<PagoResponseDTO> listarPorGrupo(Integer grupoId) {

        List<Pago> pagos = pagoRepository
                .findByGrupoIdOrderByFechaDesc(grupoId);

        // 🔥 FILTRO IMPORTANTE
        pagos = pagos.stream()
                .filter(p ->
                        p.getEstado() ==
                                EstadoPago.CONFIRMADO
                )
                .toList();

        return pagos.stream()
                .map(pago -> {

                    String metodo = "N/A";

                    if (pago.getPagoDetalles() != null && !pago.getPagoDetalles().isEmpty()) {
                        metodo = pago.getPagoDetalles()
                                .iterator()
                                .next()
                                .getMetodoPago()
                                .getCardBrand()

                                + " **** "

                                + pago.getPagoDetalles()
                                .iterator()
                                .next()
                                .getMetodoPago()
                                .getCardLast4();
                    }

                    return PagoResponseDTO.builder()
                            .id(pago.getId())
                            .deudor(pago.getDeudor().getNombre())
                            .acreedor(pago.getAcreedor().getNombre())
                            .monto(pago.getMonto())
                            .metodoPago(metodo)
                            .estado(pago.getEstado())
                            .fecha(pago.getFecha())
                            .build();
                })
                .toList();
    }

    public void eliminarPago(Integer pagoId) {

        // 🔥 OBTENER PAGO
        Pago pago = pagoRepository
                .findById(pagoId)
                .orElseThrow(() -> new BusinessException("Pago no encontrado"));

        // 🔥 VALIDAR ESTADO
        if (pago.getEstado() ==
                EstadoPago.ELIMINADO) {
            throw new BusinessException("El pago ya fue eliminado");
        }

        // 🔥 USUARIO LOGUEADO
        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        // 🔥 VALIDAR PERMISOS
        boolean esDeudor = pago.getDeudor().getEmail().equals(email);
        boolean esAcreedor = pago.getAcreedor().getEmail().equals(email);

        if (!esDeudor && !esAcreedor) {
            throw new BusinessException(
                    "No tienes permiso para eliminar este pago"
            );
        }

        // 🔥 SOFT DELETE
        pago.setEstado(
                EstadoPago.ELIMINADO
        );

        pagoRepository.save(pago);

        // 🔔 NOTIFICACIÓN OPCIONAL
        notificacionService.notificarUsuarios(
                List.of(
                        pago.getDeudor(),
                        pago.getAcreedor()
                ),
                "Se eliminó un pago de S/" + pago.getMonto(),
                "DELETE"
        );
    }
}