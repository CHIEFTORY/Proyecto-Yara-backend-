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




}