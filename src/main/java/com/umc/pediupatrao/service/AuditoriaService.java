package com.umc.pediupatrao.service;

import com.umc.pediupatrao.entity.RegistroAuditoria;
import com.umc.pediupatrao.repository.AuditoriaRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuditoriaService {

    private final AuditoriaRepository auditoriaRepository;

    public AuditoriaService(AuditoriaRepository auditoriaRepository) {
        this.auditoriaRepository = auditoriaRepository;
    }

    public void registrar(String operacao, String entidade, String entidadeId,
                          String campo, String valorAnterior, String valorNovo,
                          String justificativa) {
        RegistroAuditoria registro = new RegistroAuditoria();
        registro.setOperacao(operacao);
        registro.setEntidade(entidade);
        registro.setEntidadeId(entidadeId);
        registro.setCampo(campo);
        registro.setValorAnterior(valorAnterior);
        registro.setValorNovo(valorNovo);
        registro.setJustificativa(justificativa);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            registro.setUsuario(auth.getName());
            String perfil = auth.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .map(r -> r.replace("ROLE_", ""))
                    .collect(Collectors.joining(", "));
            registro.setPerfilUsuario(perfil);
        }

        auditoriaRepository.save(registro);
    }

    public void registrar(String operacao, String entidade, String entidadeId,
                          String campo, String valorAnterior, String valorNovo) {
        registrar(operacao, entidade, entidadeId, campo, valorAnterior, valorNovo, null);
    }

    public List<RegistroAuditoria> listarTodos() {
        return auditoriaRepository.findAllByOrderByDataHoraDesc();
    }

    public List<RegistroAuditoria> listarPorEntidade(String entidadeId) {
        return auditoriaRepository.findByEntidadeIdOrderByDataHoraDesc(entidadeId);
    }
}
