package com.yara.services;

import com.yara.dtos.*;
import com.yara.entities.*;
import com.yara.exceptions.BusinessException;
import com.yara.repositories.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class GastoService {

    private final GastoRepository gastoRepository;
    private final GastoParticipanteRepository gastoParticipanteRepository;
    private final GrupoRepository grupoRepository;
    private final UsuarioRepository usuarioRepository;
    private final PagoRepository pagoRepository;
    private final GastoCategoriaRepository categoriaRepository;
    private final GastoCategoriaRelRepository gastoCategoriaRelRepository;
    private final GrupoUsuarioRepository grupoUsuarioRepository;
    private final NotificacionService notificacionService;
    private final SeguridadService seguridadService;

    public GastoService(
            GastoRepository gastoRepository,
            GastoParticipanteRepository gastoParticipanteRepository,
            GrupoRepository grupoRepository,
            UsuarioRepository usuarioRepository,
            PagoRepository pagoRepository,
            GastoCategoriaRepository categoriaRepository,
            GastoCategoriaRelRepository gastoCategoriaRelRepository
            , GrupoUsuarioRepository grupoUsuarioRepository
            , NotificacionService notificacionService
            , SeguridadService seguridadService
    ) {
        this.gastoRepository = gastoRepository;
        this.gastoParticipanteRepository = gastoParticipanteRepository;
        this.grupoRepository = grupoRepository;
        this.usuarioRepository = usuarioRepository;
        this.pagoRepository = pagoRepository;
        this.categoriaRepository = categoriaRepository;
        this.gastoCategoriaRelRepository = gastoCategoriaRelRepository;
        this.grupoUsuarioRepository = grupoUsuarioRepository;
        this.notificacionService = notificacionService;
        this.seguridadService = seguridadService;
    }

    // =========================
    // CREAR GASTO
    // =========================

    public GastoResponseDTO crearGasto(CrearGastoDTO dto) {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        Usuario pagador = usuarioRepository
                .findByEmail(email)
                .orElseThrow();



        Grupo grupo = grupoRepository
                .findById(dto.getGrupoId())
                .orElseThrow();

        boolean pertenece = grupoUsuarioRepository
                .existsByGrupoIdAndUsuarioId(grupo.getId(), pagador.getId());

        if (!pertenece) {
            throw new BusinessException("El usuario no pertenece al grupo");
        }

        // =========================
        // VALIDACIONES GENERALES
        // =========================

        if (dto.getParticipantes() == null || dto.getParticipantes().isEmpty()) {
            throw new RuntimeException("Debe haber participantes");
        }

        if (dto.getCategorias() == null || dto.getCategorias().isEmpty()) {
            throw new RuntimeException("Debe haber al menos una categoría");
        }

        if (!dto.getTipoDivision().equalsIgnoreCase("IGUAL") &&
                !dto.getTipoDivision().equalsIgnoreCase("PERSONALIZADO")) {
            throw new RuntimeException("Tipo de división inválido");
        }

        // =========================
        // VALIDACIÓN CATEGORÍAS
        // =========================

        BigDecimal sumaCategorias = dto.getCategorias()
                .stream()
                .map(CategoriaGastoDTO::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (sumaCategorias.compareTo(dto.getMonto()) != 0) {
            throw new RuntimeException("La suma de categorías no coincide con el monto total");
        }

        // =========================
        // CREAR GASTO
        // =========================

        Gasto gasto = Gasto.builder()
                .descripcion(dto.getDescripcion())
                .montoTotal(dto.getMonto())
                .grupo(grupo)
                .pagadoPor(pagador)
                .tipoDivision(dto.getTipoDivision())
                .estado("ACTIVO")
                .fecha(LocalDateTime.now())
                .build();

        Gasto gastoGuardado = gastoRepository.save(gasto);

        // =========================
        // GUARDAR CATEGORÍAS
        // =========================
        Set<Integer> categoriasIds = new HashSet<>();

        for (CategoriaGastoDTO cat : dto.getCategorias()) {
            if (!categoriasIds.add(cat.getCategoriaId())) {
                throw new BusinessException("Categoría repetida en el gasto");
            }
        }

        for (CategoriaGastoDTO catDTO : dto.getCategorias()) {

            GastoCategoria categoria = categoriaRepository
                    .findById(catDTO.getCategoriaId())
                    .orElseThrow();

            GastoCategoriaRel rel = GastoCategoriaRel.builder()
                    .gasto(gastoGuardado)
                    .categoria(categoria)
                    .monto(catDTO.getMonto())
                    .build();

            gastoCategoriaRelRepository.save(rel);
        }

        // =========================
        // DIVISIÓN IGUAL
        // =========================

        if (dto.getTipoDivision().equalsIgnoreCase("IGUAL")) {

            BigDecimal division = dto.getMonto().divide(
                    BigDecimal.valueOf(dto.getParticipantes().size()),
                    2,
                    RoundingMode.HALF_UP
            );
            Set<Integer> participantesIds = new HashSet<>();

            for (ParticipanteGastoDTO p : dto.getParticipantes()) {
                if (!participantesIds.add(p.getUsuarioId())) {
                    throw new BusinessException("Participante repetido en el gasto");
                }
            }

            for (ParticipanteGastoDTO participanteDTO : dto.getParticipantes()) {

                Usuario participante = usuarioRepository
                        .findById(participanteDTO.getUsuarioId())
                        .orElseThrow();

                GastoParticipante gp = GastoParticipante.builder()
                        .gasto(gastoGuardado)
                        .usuario(participante)
                        .monto(division)
                        .estado("PENDIENTE")
                        .build();

                gastoParticipanteRepository.save(gp);
            }
        }

        // =========================
        // DIVISIÓN PERSONALIZADA
        // =========================

        else {

            BigDecimal suma = dto.getParticipantes()
                    .stream()
                    .map(ParticipanteGastoDTO::getMonto)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            if (suma.compareTo(dto.getMonto()) != 0) {
                throw new RuntimeException(
                        "La suma de los montos no coincide con el monto total"
                );
            }

            for (ParticipanteGastoDTO participanteDTO : dto.getParticipantes()) {

                if (participanteDTO.getMonto() == null ||
                        participanteDTO.getMonto().compareTo(BigDecimal.ZERO) <= 0) {
                    throw new RuntimeException("Monto inválido en participantes");
                }

                Usuario participante = usuarioRepository
                        .findById(participanteDTO.getUsuarioId())
                        .orElseThrow();

                GastoParticipante gp = GastoParticipante.builder()
                        .gasto(gastoGuardado)
                        .usuario(participante)
                        .monto(participanteDTO.getMonto())
                        .estado("PENDIENTE")
                        .build();

                gastoParticipanteRepository.save(gp);
            }
        }

        // =========================
        // MAPEAR CATEGORÍAS PARA RESPONSE 🔥
        // =========================

        List<CategoriaResponseDTO> categorias = gastoCategoriaRelRepository
                .findByGastoId(gastoGuardado.getId())
                .stream()
                .map(rel -> CategoriaResponseDTO.builder()
                        .nombre(rel.getCategoria().getNombre())
                        .monto(rel.getMonto())
                        .build())
                .toList();

        // =========================
        // NOTIFICACIONES 🔥
        // =========================

        List<Usuario> usuariosNotificar = dto.getParticipantes()
                .stream()
                .map(p -> usuarioRepository.findById(p.getUsuarioId()).orElseThrow())
                .filter(u -> !u.getId().equals(pagador.getId())) // no notificar al que pagó
                .toList();

        notificacionService.notificarUsuarios(
                usuariosNotificar,
                pagador.getNombre() + " registró un gasto: " + dto.getDescripcion(),
                "GASTO"
        );

        // =========================
        // RESPONSE FINAL 🔥
        // =========================

        return GastoResponseDTO.builder()
                .id(gastoGuardado.getId())
                .descripcion(gastoGuardado.getDescripcion())
                .montoTotal(gastoGuardado.getMontoTotal())
                .grupoNombre(gastoGuardado.getGrupo().getNombre())
                .pagadoPor(gastoGuardado.getPagadoPor().getNombre())
                .tipoDivision(gastoGuardado.getTipoDivision())
                .estado(gastoGuardado.getEstado())
                .fecha(gastoGuardado.getFecha())
                .categorias(categorias)
                .build();
    }

    // =========================
    // LISTAR GASTOS
    // =========================

    public Page<GastoResponseDTO> listarPorGrupo(Integer grupoId, int page, int size) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("fecha").descending()
        );

        Page<Gasto> gastosPage =
                gastoRepository.findByGrupoIdAndEstado(grupoId, "ACTIVO", pageable);

        return gastosPage.map(gasto -> {

            List<CategoriaResponseDTO> categorias = gastoCategoriaRelRepository
                    .findByGastoId(gasto.getId())
                    .stream()
                    .map(rel -> CategoriaResponseDTO.builder()
                            .nombre(rel.getCategoria().getNombre())
                            .monto(rel.getMonto())
                            .build())
                    .toList();

            return GastoResponseDTO.builder()
                    .id(gasto.getId())
                    .descripcion(gasto.getDescripcion())
                    .montoTotal(gasto.getMontoTotal())
                    .grupoNombre(gasto.getGrupo().getNombre())
                    .pagadoPor(gasto.getPagadoPor().getNombre())
                    .tipoDivision(gasto.getTipoDivision())
                    .estado(gasto.getEstado())
                    .fecha(gasto.getFecha())
                    .categorias(categorias)
                    .build();
        });
    }

    // =========================
    // BALANCES (CON PAGOS)
    // =========================

    public List<BalanceDTO> obtenerBalances(Integer grupoId) {

        List<GastoParticipante> participaciones =
                gastoParticipanteRepository.findByGastoGrupoId(grupoId);

        // 🔥 FILTRO IMPORTANTE
        participaciones = participaciones.stream()
                .filter(gp -> gp.getGasto().getEstado().equals("ACTIVO"))
                .toList();

        List<Pago> pagos =
                pagoRepository.findByGrupoId(grupoId);

        // 🔥 FILTRO PAGOS
        pagos = pagos.stream()
                .filter(p -> p.getEstado().equals("CONFIRMADO"))
                .toList();

        Map<String, BigDecimal> balances = new HashMap<>();

        // =========================
        // GASTOS
        // =========================

        for (GastoParticipante gp : participaciones) {

            String participante = gp.getUsuario().getNombre();
            BigDecimal deuda = gp.getMonto();

            balances.putIfAbsent(participante, BigDecimal.ZERO);

            balances.put(
                    participante,
                    balances.get(participante).subtract(deuda)
            );

            String pagador = gp.getGasto()
                    .getPagadoPor()
                    .getNombre();

            balances.putIfAbsent(pagador, BigDecimal.ZERO);

            balances.put(
                    pagador,
                    balances.get(pagador).add(deuda)
            );
        }

        // =========================
        // PAGOS
        // =========================

        for (Pago pago : pagos) {

            String deudor = pago.getDeudor().getNombre();
            String acreedor = pago.getAcreedor().getNombre();
            BigDecimal monto = pago.getMonto();

            balances.putIfAbsent(deudor, BigDecimal.ZERO);
            balances.putIfAbsent(acreedor, BigDecimal.ZERO);

            balances.put(
                    deudor,
                    balances.get(deudor).add(monto)
            );

            balances.put(
                    acreedor,
                    balances.get(acreedor).subtract(monto)
            );
        }

        return balances.entrySet()
                .stream()
                .map(entry -> BalanceDTO.builder()
                        .usuario(entry.getKey())
                        .balance(entry.getValue())
                        .build())
                .toList();
    }

    public BigDecimal obtenerBalanceUsuario(Integer grupoId, String nombreUsuario) {

        List<BalanceDTO> balances = obtenerBalances(grupoId);

        return balances.stream()
                .filter(b -> b.getUsuario().equals(nombreUsuario))
                .map(BalanceDTO::getBalance)
                .findFirst()
                .orElse(BigDecimal.ZERO);
    }

    public List<DeudaDTO> simplificarDeudas(Integer grupoId) {

        List<BalanceDTO> balances = obtenerBalances(grupoId);

        List<BalanceDTO> deudores = balances.stream()
                .filter(b -> b.getBalance().compareTo(BigDecimal.ZERO) < 0)
                .toList();

        List<BalanceDTO> acreedores = balances.stream()
                .filter(b -> b.getBalance().compareTo(BigDecimal.ZERO) > 0)
                .toList();

        List<DeudaDTO> resultado = new ArrayList<>();

        int i = 0;
        int j = 0;

        while (i < deudores.size() && j < acreedores.size()) {

            BalanceDTO deudor = deudores.get(i);
            BalanceDTO acreedor = acreedores.get(j);

            BigDecimal deuda = deudor.getBalance().abs();
            BigDecimal credito = acreedor.getBalance();

            BigDecimal monto = deuda.min(credito)
                    .setScale(2, RoundingMode.HALF_UP);

            resultado.add(
                    DeudaDTO.builder()
                            .deudor(deudor.getUsuario())
                            .acreedor(acreedor.getUsuario())
                            .monto(monto)
                            .build()
            );

            deudor.setBalance(deudor.getBalance().add(monto));
            acreedor.setBalance(acreedor.getBalance().subtract(monto));

            if (deudor.getBalance().compareTo(BigDecimal.ZERO) == 0) i++;
            if (acreedor.getBalance().compareTo(BigDecimal.ZERO) == 0) j++;
        }

        // eliminar montos 0
        resultado.removeIf(d -> d.getMonto().compareTo(BigDecimal.ZERO) <= 0);

        resultado.sort((a, b) ->
                b.getMonto().compareTo(a.getMonto())
        );

        // ordenar de mayor a menor
        resultado.sort((a, b) ->
                b.getMonto().compareTo(a.getMonto())
        );

        return resultado;
    }

    @Transactional
    public void eliminarGasto(Integer gastoId) {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        Usuario usuario = usuarioRepository
                .findByEmail(email)
                .orElseThrow();

        Gasto gasto = gastoRepository
                .findById(gastoId)
                .orElseThrow();

        Integer grupoId = gasto.getGrupo().getId();

        GrupoUsuario gu = grupoUsuarioRepository
                .findByGrupo_IdAndUsuario_Id(grupoId, usuario.getId())
                .orElseThrow(() -> new RuntimeException("No perteneces al grupo"));

        if (!"ADMIN".equalsIgnoreCase(gu.getRolGrupo())) {
            throw new RuntimeException("Solo el admin puede eliminar gastos");
        }

        if ("ELIMINADO".equalsIgnoreCase(gasto.getEstado())) {
            throw new RuntimeException("Ya está eliminado");
        }

        gasto.setEstado("ELIMINADO");
        gastoRepository.save(gasto);
    }

    @Transactional
    public GastoResponseDTO editarGasto(Integer gastoId, CrearGastoDTO dto) {

        // 🔥 1. Usuario logueado
        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        Usuario usuario = usuarioRepository
                .findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Integer usuarioId = usuario.getId();

        // 🔥 2. Obtener gasto
        Gasto gasto = gastoRepository.findById(gastoId)
                .orElseThrow(() -> new RuntimeException("Gasto no encontrado"));

        Integer grupoId = gasto.getGrupo().getId();

        // 🔥 3. Validar pertenencia + rol en el grupo
        GrupoUsuario gu = grupoUsuarioRepository
                .findByGrupo_IdAndUsuario_Id(grupoId, usuarioId)
                .orElseThrow(() -> new RuntimeException("No perteneces a este grupo"));

        if (!"ADMIN".equalsIgnoreCase(gu.getRolGrupo())) {
            throw new RuntimeException("Solo el admin del grupo puede editar gastos");
        }

        // 🔥 4. Validación estado
        if ("ELIMINADO".equalsIgnoreCase(gasto.getEstado())) {
            throw new RuntimeException("No se puede editar un gasto eliminado");
        }

        // =========================
        // ELIMINAR RELACIONES
        // =========================

        gastoParticipanteRepository.deleteByGastoId(gastoId);
        gastoCategoriaRelRepository.deleteByGastoId(gastoId);

        // =========================
        // ACTUALIZAR GASTO
        // =========================

        gasto.setDescripcion(dto.getDescripcion());
        gasto.setMontoTotal(dto.getMonto());
        gasto.setTipoDivision(dto.getTipoDivision());

        gastoRepository.save(gasto);

        return crearGastoDesdeExistente(gasto, dto);
    }

    private GastoResponseDTO crearGastoDesdeExistente(Gasto gasto, CrearGastoDTO dto) {

        // =========================
        // VALIDACIÓN CATEGORÍAS
        // =========================

        BigDecimal sumaCategorias = dto.getCategorias()
                .stream()
                .map(CategoriaGastoDTO::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (sumaCategorias.compareTo(dto.getMonto()) != 0) {
            throw new RuntimeException("La suma de categorías no coincide con el monto total");
        }

        // =========================
        // GUARDAR CATEGORÍAS
        // =========================

        for (CategoriaGastoDTO catDTO : dto.getCategorias()) {

            GastoCategoria categoria = categoriaRepository
                    .findById(catDTO.getCategoriaId())
                    .orElseThrow();

            GastoCategoriaRel rel = GastoCategoriaRel.builder()
                    .gasto(gasto)
                    .categoria(categoria)
                    .monto(catDTO.getMonto())
                    .build();

            gastoCategoriaRelRepository.save(rel);
        }

        // =========================
        // DIVISIÓN IGUAL
        // =========================

        if (dto.getTipoDivision().equalsIgnoreCase("IGUAL")) {

            BigDecimal division = dto.getMonto().divide(
                    BigDecimal.valueOf(dto.getParticipantes().size()),
                    2,
                    RoundingMode.HALF_UP
            );

            for (ParticipanteGastoDTO participanteDTO : dto.getParticipantes()) {

                Usuario participante = usuarioRepository
                        .findById(participanteDTO.getUsuarioId())
                        .orElseThrow();

                GastoParticipante gp = GastoParticipante.builder()
                        .gasto(gasto)
                        .usuario(participante)
                        .monto(division)
                        .estado("PENDIENTE")
                        .build();

                gastoParticipanteRepository.save(gp);
            }
        }

        // =========================
        // DIVISIÓN PERSONALIZADA
        // =========================

        else {

            BigDecimal suma = dto.getParticipantes()
                    .stream()
                    .map(ParticipanteGastoDTO::getMonto)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            if (suma.compareTo(dto.getMonto()) != 0) {
                throw new RuntimeException(
                        "La suma de los montos no coincide con el monto total"
                );
            }

            for (ParticipanteGastoDTO participanteDTO : dto.getParticipantes()) {

                if (participanteDTO.getMonto() == null ||
                        participanteDTO.getMonto().compareTo(BigDecimal.ZERO) <= 0) {
                    throw new RuntimeException("Monto inválido en participantes");
                }

                Usuario participante = usuarioRepository
                        .findById(participanteDTO.getUsuarioId())
                        .orElseThrow();

                GastoParticipante gp = GastoParticipante.builder()
                        .gasto(gasto)
                        .usuario(participante)
                        .monto(participanteDTO.getMonto())
                        .estado("PENDIENTE")
                        .build();

                gastoParticipanteRepository.save(gp);
            }
        }

        // =========================
        // MAPEAR CATEGORÍAS
        // =========================

        List<CategoriaResponseDTO> categorias = gastoCategoriaRelRepository
                .findByGastoId(gasto.getId())
                .stream()
                .map(rel -> CategoriaResponseDTO.builder()
                        .nombre(rel.getCategoria().getNombre())
                        .monto(rel.getMonto())
                        .build())
                .toList();

        // =========================
        // NOTIFICACIONES 🔥
        // =========================

        List<Usuario> usuariosNotificar = dto.getParticipantes()
                .stream()
                .map(p -> usuarioRepository.findById(p.getUsuarioId()).orElseThrow())
                .toList();

        notificacionService.notificarUsuarios(
                usuariosNotificar,
                "Se actualizó el gasto: " + dto.getDescripcion(),
                "EDIT"
        );
        // =========================
        // RESPONSE
        // =========================

        return GastoResponseDTO.builder()
                .id(gasto.getId())
                .descripcion(gasto.getDescripcion())
                .montoTotal(gasto.getMontoTotal())
                .grupoNombre(gasto.getGrupo().getNombre())
                .pagadoPor(gasto.getPagadoPor().getNombre())
                .tipoDivision(gasto.getTipoDivision())
                .estado(gasto.getEstado())
                .fecha(gasto.getFecha())
                .categorias(categorias)
                .build();
    }

    public List<GastoResponseDTO> listarPorGrupoSinPaginacion(Integer grupoId) {

        List<Gasto> gastos =
                gastoRepository.findByGrupoIdAndEstado(grupoId, "ACTIVO");

        return gastos.stream()
                .sorted(Comparator.comparing(Gasto::getFecha).reversed())
                .map(gasto -> {

                    List<CategoriaResponseDTO> categorias = gastoCategoriaRelRepository
                            .findByGastoId(gasto.getId())
                            .stream()
                            .map(rel -> CategoriaResponseDTO.builder()
                                    .nombre(rel.getCategoria().getNombre())
                                    .monto(rel.getMonto())
                                    .build())
                            .toList();

                    return GastoResponseDTO.builder()
                            .id(gasto.getId())
                            .descripcion(gasto.getDescripcion())
                            .montoTotal(gasto.getMontoTotal())
                            .grupoNombre(gasto.getGrupo().getNombre())
                            .pagadoPor(gasto.getPagadoPor().getNombre())
                            .tipoDivision(gasto.getTipoDivision())
                            .estado(gasto.getEstado())
                            .fecha(gasto.getFecha())
                            .categorias(categorias)
                            .build();
                })
                .toList();
    }
}