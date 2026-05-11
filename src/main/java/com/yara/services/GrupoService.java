package com.yara.services;
import com.yara.dtos.GrupoPreviewDTO;
import com.yara.entities.GrupoUsuario;
import com.yara.dtos.*;
import com.yara.entities.*;
import com.yara.entities.authYuser.Usuario;
import com.yara.enums.EstadoGasto;
import com.yara.enums.RolGrupo;
import com.yara.repositories.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;

import org.springframework.security.core.context.SecurityContextHolder;
import com.yara.entities.authYuser.Usuario;
import com.yara.repositories.UsuarioRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class GrupoService {

    private final GrupoRepository grupoRepository;
    private final UsuarioRepository usuarioRepository;
    private final GrupoUsuarioRepository grupoUsuarioRepository;
    private final GastoService gastoService;
    private final PagoService pagoService;
    private final GastoRepository gastoRepository;




    public GrupoService(
            GrupoRepository grupoRepository,
            UsuarioRepository usuarioRepository,
            GrupoUsuarioRepository grupoUsuarioRepository,
            GastoService gastoService,
            PagoService pagoService,
            GastoRepository gastoRepository

    ) {
        this.grupoRepository = grupoRepository;
        this.usuarioRepository = usuarioRepository;
        this.grupoUsuarioRepository = grupoUsuarioRepository;
        this.gastoService = gastoService;
        this.pagoService = pagoService;
        this.gastoRepository = gastoRepository;

    }

    public Grupo crearGrupo(CrearGrupoDTO dto) {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        Usuario usuario = usuarioRepository
                .findByEmail(email)
                .orElseThrow();

        Grupo grupo = Grupo.builder()
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .creadoPor(usuario)
                .estado("ACTIVO")
                .fechaCreacion(LocalDateTime.now())
                .build();

        Grupo grupoGuardado = grupoRepository.save(grupo);

        GrupoUsuario grupoUsuario = GrupoUsuario.builder()
                .grupo(grupoGuardado)
                .usuario(usuario)
                .rolGrupo(RolGrupo.ADMIN)
                .fechaUnion(LocalDateTime.now())
                .build();

        grupoUsuarioRepository.save(grupoUsuario);

        return grupoGuardado;
    }
    public ResumenDTO obtenerResumen(Integer grupoId) {

        Grupo grupo =
                grupoRepository
                        .findById(grupoId)
                        .orElseThrow();

        List<BalanceDTO> balances =
                gastoService.obtenerBalances(grupoId);

        List<DeudaDTO> deudas =
                gastoService.simplificarDeudas(grupoId);

        List<PagoResponseDTO> pagos =
                pagoService.listarPorGrupo(grupoId);

        List<GastoResponseDTO> gastos =
                gastoService.listarPorGrupoSinPaginacion(grupoId);

        BigDecimal totalGastos =
                gastoRepository
                        .findByGrupoIdAndEstado(
                                grupoId,
                                EstadoGasto.ACTIVO
                        )
                        .stream()
                        .map(Gasto::getMontoTotal)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        String email =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getName();

        Usuario usuario =
                usuarioRepository
                        .findByEmail(email)
                        .orElseThrow();

        BalanceDTO miBalance =
                balances.stream()

                        .filter(b ->

                                b.getUsuario()
                                        .equals(
                                                usuario.getNombre()
                                        )
                        )

                        .findFirst()

                        .orElse(null);

        BigDecimal totalDebes =
                BigDecimal.ZERO;

        BigDecimal totalTeDeben =
                BigDecimal.ZERO;

        BigDecimal balanceGeneral =
                BigDecimal.ZERO;

        if (miBalance != null) {

            balanceGeneral =
                    miBalance.getBalance();

            if (
                    balanceGeneral.compareTo(
                            BigDecimal.ZERO
                    ) < 0
            ) {

                totalDebes =
                        balanceGeneral.abs();

            } else {

                totalTeDeben =
                        balanceGeneral;
            }
        }

        return ResumenDTO.builder()
                .nombre(grupo.getNombre())
                .balances(balances)
                .deudas(deudas)
                .pagos(pagos)
                .gastos(gastos)
                .totalGastos(totalGastos)
                .totalDebes(totalDebes)
                .totalTeDeben(totalTeDeben)
                .balanceGeneral(balanceGeneral)
                .build();
    }

    public void agregarUsuarioAGrupo(Integer grupoId, Integer usuarioId) {

        // 🔥 1. Usuario logueado
        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        Usuario usuarioLogueado = usuarioRepository
                .findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Integer usuarioLogueadoId = usuarioLogueado.getId(); // 🔥 MOVER AQUÍ

        // 🔥 VALIDACIÓN EXTRA (AHORA SÍ)
        if (usuarioId.equals(usuarioLogueadoId)) {
            throw new RuntimeException("Ya perteneces al grupo");
        }

        // 🔥 2. Obtener grupo
        Grupo grupo = grupoRepository.findById(grupoId)
                .orElseThrow(() -> new RuntimeException("Grupo no encontrado"));

        // 🔥 3. Validar ADMIN
        GrupoUsuario admin = grupoUsuarioRepository
                .findByGrupo_IdAndUsuario_Id(grupoId, usuarioLogueadoId)
                .orElseThrow(() -> new RuntimeException("No perteneces al grupo"));

        if (admin.getRolGrupo()
                != RolGrupo.ADMIN) {
            throw new RuntimeException("Solo el admin puede agregar usuarios");
        }

        // 🔥 4. Usuario a agregar
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // 🔥 5. Evitar duplicados
        boolean existe = grupoUsuarioRepository
                .existsByGrupoIdAndUsuarioId(grupoId, usuarioId);

        if (existe) {
            throw new RuntimeException("El usuario ya pertenece al grupo");
        }

        // 🔥 6. Guardar
        GrupoUsuario gu = GrupoUsuario.builder()
                .grupo(grupo)
                .usuario(usuario)
                .rolGrupo(RolGrupo.MIEMBRO)
                .fechaUnion(LocalDateTime.now())
                .build();

        grupoUsuarioRepository.save(gu);
    }

    public void salirDelGrupo(Integer grupoId) {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        Usuario usuario = usuarioRepository
                .findByEmail(email)
                .orElseThrow();

        GrupoUsuario gu = grupoUsuarioRepository
                .findByGrupo_IdAndUsuario_Id(
                        grupoId,
                        usuario.getId()
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "No perteneces al grupo"
                        )
                );

        // 🔥 evitar que el único ADMIN se vaya
        if (
                gu.getRolGrupo()
                        == RolGrupo.ADMIN
        ) {

            long admins =
                    grupoUsuarioRepository
                            .countByGrupo_IdAndRolGrupo(
                                    grupoId,
                                    RolGrupo.ADMIN
                            );

            if (admins <= 1) {

                throw new RuntimeException(
                        "No puedes salir siendo el único ADMIN"
                );
            }
        }

        grupoUsuarioRepository.delete(gu);
    }


    public void eliminarUsuario(Integer grupoId, Integer usuarioId) {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        Usuario adminUser = usuarioRepository
                .findByEmail(email)
                .orElseThrow();

        // 🔥 VALIDAR ADMIN
        GrupoUsuario admin = grupoUsuarioRepository
                .findByGrupo_IdAndUsuario_Id(grupoId, adminUser.getId())
                .orElseThrow();

        if (admin.getRolGrupo()
                != RolGrupo.ADMIN) {
            throw new RuntimeException(
                    "Solo el admin puede eliminar usuarios"
            );
        }

        // 🔥 USUARIO A ELIMINAR
        GrupoUsuario gu = grupoUsuarioRepository
                .findByGrupo_IdAndUsuario_Id(grupoId, usuarioId)
                .orElseThrow(() ->
                        new RuntimeException("Usuario no encontrado en el grupo"));

        // 🔥 EVITAR ELIMINAR ÚLTIMO ADMIN
        if (gu.getRolGrupo()
                == RolGrupo.ADMIN) {

            long admins = grupoUsuarioRepository
                    .countByGrupo_IdAndRolGrupo(
                            grupoId,
                            RolGrupo.ADMIN
                    );

            if (admins <= 1) {
                throw new RuntimeException(
                        "No puedes eliminar al único ADMIN"
                );
            }
        }

        grupoUsuarioRepository.delete(gu);
    }
    public List<GrupoUsuarioDTO> listarUsuarios(
            Integer grupoId
    ) {

        return grupoUsuarioRepository
                .findByGrupo_Id(grupoId)
                .stream()
                .map(gu -> {

                    Usuario usuario =
                            gu.getUsuario();

                    return new GrupoUsuarioDTO(
                            usuario.getId(),
                            usuario.getNombre(),
                            usuario.getEmail(),
                            gu.getRolGrupo().name()
                    );

                })
                .toList();
    }

    public List<GrupoPreviewDTO> listarMisGrupos() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        String email =
                authentication.getName();

        Usuario usuario =
                usuarioRepository
                        .findByEmail(email)
                        .orElseThrow();

        List<GrupoUsuario> grupoUsuarios =
                grupoUsuarioRepository
                        .findByUsuarioId(
                                usuario.getId()
                        );

        return grupoUsuarios
                .stream()
                .map(grupoUsuario -> {

                    Grupo grupo =
                            grupoUsuario.getGrupo();

                    Integer cantidadMiembros =
                            grupoUsuarioRepository
                                    .findByGrupoId(
                                            grupo.getId()
                                    )
                                    .size();

                    return GrupoPreviewDTO.builder()

                            .id(
                                    grupo.getId()
                            )

                            .nombre(
                                    grupo.getNombre()
                            )

                            .cantidadMiembros(
                                    cantidadMiembros
                            )

                            .miBalance(
                                    gastoService.obtenerBalanceUsuario(
                                            grupo.getId(),
                                            usuario.getId()
                                    )
                            )

                            .build();

                })
                .toList();
    }
    public void eliminarGrupo(Integer grupoId) {

        Grupo grupo = grupoRepository
                .findById(grupoId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Grupo no encontrado"
                        )
                );

        // =========================
        // USUARIO LOGUEADO
        // =========================

        String email =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getName();

        Usuario usuario =
                usuarioRepository
                        .findByEmail(email)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Usuario no encontrado"
                                )
                        );

        // =========================
        // VALIDAR MEMBRO DEL GRUPO
        // =========================

        GrupoUsuario grupoUsuario =
                grupoUsuarioRepository
                        .findByGrupo_IdAndUsuario_Id(
                                grupoId,
                                usuario.getId()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "No perteneces al grupo"
                                )
                        );

        // =========================
        // SOLO ADMIN PUEDE ELIMINAR
        // =========================

        if (
                grupoUsuario.getRolGrupo()
                        != RolGrupo.ADMIN
        ) {

            throw new RuntimeException(
                    "Solo el administrador puede eliminar el grupo"
            );
        }

        // =========================
        // ELIMINAR
        // =========================

        grupoRepository.delete(grupo);
    }

    public DashboardBalanceDTO
    obtenerBalanceDashboard() {

        List<GrupoPreviewDTO> grupos =
                listarMisGrupos();

        BigDecimal totalDebes =
                BigDecimal.ZERO;

        BigDecimal totalTeDeben =
                BigDecimal.ZERO;

        for (GrupoPreviewDTO grupo : grupos) {

            BigDecimal balance =

                    grupo.getMiBalance() != null

                            ? grupo.getMiBalance()

                            : BigDecimal.ZERO;

            if (
                    balance.compareTo(
                            BigDecimal.ZERO
                    ) < 0
            ) {

                totalDebes =
                        totalDebes.add(
                                balance.abs()
                        );

            } else {

                totalTeDeben =
                        totalTeDeben.add(
                                balance
                        );
            }
        }

        BigDecimal balanceGeneral =
                totalTeDeben.subtract(
                        totalDebes
                );

        return DashboardBalanceDTO
                .builder()

                .totalDebes(totalDebes)

                .totalTeDeben(totalTeDeben)

                .balanceGeneral(balanceGeneral)

                .build();
    }

}