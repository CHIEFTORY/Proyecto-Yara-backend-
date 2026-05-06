package com.yara.services;

import com.yara.dtos.CrearPagoDTO;
import com.yara.dtos.PagoResponseDTO;
import com.yara.entities.*;
import com.yara.repositories.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

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

    public PagoService(
            PagoRepository pagoRepository,
            PagoDetalleRepository pagoDetalleRepository,
            MetodoPagoRepository metodoPagoRepository,
            UsuarioRepository usuarioRepository,
            GrupoRepository grupoRepository,
            GastoService gastoService
            , NotificacionService notificacionService
    ) {
        this.pagoRepository = pagoRepository;
        this.pagoDetalleRepository = pagoDetalleRepository;
        this.metodoPagoRepository = metodoPagoRepository;
        this.usuarioRepository = usuarioRepository;
        this.grupoRepository = grupoRepository;
        this.gastoService = gastoService;
        this.notificacionService = notificacionService;
    }

    public PagoResponseDTO registrarPago(
            CrearPagoDTO dto
    ) {

        Usuario deudor = usuarioRepository
                .findById(dto.getDeudorId())
                .orElseThrow();

        Usuario acreedor = usuarioRepository
                .findById(dto.getAcreedorId())
                .orElseThrow();

        Grupo grupo = grupoRepository
                .findById(dto.getGrupoId())
                .orElseThrow();

        MetodoPago metodoPago =
                metodoPagoRepository
                        .findById(dto.getMetodoPagoId())
                        .orElseThrow();

        // =========================
        // 🔐 SEGURIDAD
        // =========================

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        if (!deudor.getEmail().equals(email)) {
            throw new RuntimeException("No puedes pagar por otro usuario");
        }

        // =========================
        // ❗ VALIDACIONES DE NEGOCIO
        // =========================

        // No pagarse a sí mismo
        if (deudor.getId().equals(acreedor.getId())) {
            throw new RuntimeException("No puedes pagarte a ti mismo");
        }

        // =========================
        // 💰 VALIDACIÓN DE DEUDA
        // =========================

        BigDecimal balanceDeudor = gastoService
                .obtenerBalanceUsuario(
                        dto.getGrupoId(),
                        deudor.getNombre()
                );

        // Si no debe nada
        if (balanceDeudor.compareTo(BigDecimal.ZERO) >= 0) {
            throw new RuntimeException("El usuario no tiene deuda");
        }

        BigDecimal deudaReal = balanceDeudor.abs();

        // Si intenta pagar más de lo que debe
        if (dto.getMonto().compareTo(deudaReal) > 0) {
            throw new RuntimeException(
                    "El monto excede la deuda actual"
            );
        }

        // =========================
        // 🧾 CREAR PAGO
        // =========================

        Pago pago = Pago.builder()
                .grupo(grupo)
                .deudor(deudor)
                .acreedor(acreedor)
                .monto(dto.getMonto())
                .estado("CONFIRMADO")
                .fecha(LocalDateTime.now())
                .build();

        Pago pagoGuardado =
                pagoRepository.save(pago);

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
        // NOTIFICACIONES 🔥
        // =========================

        List<Usuario> usuariosNotificar = List.of(deudor, acreedor);

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
                .metodoPago(metodoPago.getNombre())
                .estado(pagoGuardado.getEstado())
                .fecha(pagoGuardado.getFecha())
                .build();
    }

    public List<PagoResponseDTO> listarPorGrupo(Integer grupoId) {

        List<Pago> pagos = pagoRepository
                .findByGrupoIdOrderByFechaDesc(grupoId);

        // 🔥 FILTRO IMPORTANTE
        pagos = pagos.stream()
                .filter(p -> p.getEstado().equals("CONFIRMADO"))
                .toList();

        return pagos.stream()
                .map(pago -> {

                    String metodo = "N/A";

                    if (pago.getPagoDetalles() != null && !pago.getPagoDetalles().isEmpty()) {
                        metodo = pago.getPagoDetalles()
                                .iterator()
                                .next()
                                .getMetodoPago()
                                .getNombre();
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

        Pago pago = pagoRepository
                .findById(pagoId)
                .orElseThrow();

        // 🔐 VALIDACIÓN (opcional pero PRO)
        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        boolean esDeudor = pago.getDeudor().getEmail().equals(email);
        boolean esAcreedor = pago.getAcreedor().getEmail().equals(email);

        if (!esDeudor && !esAcreedor) {
            throw new RuntimeException("No tienes permiso para eliminar este pago");
        }

        // 🔥 SOFT DELETE
        pago.setEstado("ELIMINADO");

        pagoRepository.save(pago);
    }
}