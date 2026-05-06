package com.yara.services;

import com.yara.dtos.*;
import com.yara.entities.*;
import com.yara.repositories.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
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
                .rolGrupo("ADMIN")
                .fechaUnion(LocalDateTime.now())
                .build();

        grupoUsuarioRepository.save(grupoUsuario);

        return grupoGuardado;
    }
    public ResumenDTO obtenerResumen(Integer grupoId) {

        List<BalanceDTO> balances =
                gastoService.obtenerBalances(grupoId);

        List<DeudaDTO> deudas =
                gastoService.simplificarDeudas(grupoId);

        List<PagoResponseDTO> pagos =
                pagoService.listarPorGrupo(grupoId);

        List<GastoResponseDTO> gastos =
                gastoService.listarPorGrupoSinPaginacion(grupoId);

        BigDecimal totalGastos =
                gastoRepository.findByGrupoIdAndEstado(grupoId, "ACTIVO")
                        .stream()
                        .map(Gasto::getMontoTotal)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        return ResumenDTO.builder()
                .balances(balances)
                .deudas(deudas)
                .pagos(pagos)
                .gastos(gastos)
                .totalGastos(totalGastos)
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

        if (!"ADMIN".equalsIgnoreCase(admin.getRolGrupo())) {
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
                .rolGrupo("MIEMBRO")
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
                .findByGrupo_IdAndUsuario_Id(grupoId, usuario.getId())
                .orElseThrow(() -> new RuntimeException("No perteneces al grupo"));

        // 🔥 evitar que el único ADMIN se vaya
        if ("ADMIN".equalsIgnoreCase(gu.getRolGrupo())) {

            long admins = grupoUsuarioRepository
                    .countByGrupo_IdAndRolGrupo(grupoId, "ADMIN");

            if (admins <= 1) {
                throw new RuntimeException("No puedes salir siendo el único ADMIN");
            }
        }

        grupoUsuarioRepository.delete(gu);
    }

    public void eliminarUsuario(Integer grupoId, Integer usuarioId) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Usuario adminUser = usuarioRepository.findByEmail(email).orElseThrow();

        GrupoUsuario admin = grupoUsuarioRepository
                .findByGrupo_IdAndUsuario_Id(grupoId, adminUser.getId())
                .orElseThrow();

        if (!"ADMIN".equalsIgnoreCase(admin.getRolGrupo())) {
            throw new RuntimeException("Solo el admin puede eliminar usuarios");
        }

        GrupoUsuario gu = grupoUsuarioRepository
                .findByGrupo_IdAndUsuario_Id(grupoId, usuarioId)
                .orElseThrow();

        grupoUsuarioRepository.delete(gu);
    }
    public List<String> listarUsuarios(Integer grupoId) {

        return grupoUsuarioRepository.findByGrupo_Id(grupoId)
                .stream()
                .map(gu -> gu.getUsuario().getNombre() + " (" + gu.getRolGrupo() + ")")
                .toList();
    }
    public List<String> listarMisGrupos() {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Usuario usuario = usuarioRepository.findByEmail(email).orElseThrow();

        return grupoUsuarioRepository.findByUsuario_Id(usuario.getId())
                .stream()
                .map(gu -> gu.getGrupo().getNombre())
                .toList();
    }


}