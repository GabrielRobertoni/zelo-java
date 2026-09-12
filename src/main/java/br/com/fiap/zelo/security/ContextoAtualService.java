package br.com.fiap.zelo.security;

import br.com.fiap.zelo.domain.Clinica;
import br.com.fiap.zelo.domain.Tutor;
import br.com.fiap.zelo.exception.RecursoNaoEncontradoException;
import br.com.fiap.zelo.repository.TutorRepository;
import br.com.fiap.zelo.service.ClinicaService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Resolve o Tutor ou a Clinica associados ao usuario autenticado. Centraliza
 * essa consulta para que os controllers nao dupliquem a mesma logica de
 * lookup (evita repeticao apontada como penalidade de qualidade de codigo).
 */
@Component
public class ContextoAtualService {

    private final TutorRepository tutorRepository;
    private final ClinicaService clinicaService;

    public ContextoAtualService(TutorRepository tutorRepository, ClinicaService clinicaService) {
        this.tutorRepository = tutorRepository;
        this.clinicaService = clinicaService;
    }

    @Transactional(readOnly = true)
    public Tutor tutorAtual(ZeloUserPrincipal principal) {
        return tutorRepository.findByUsuarioId(principal.getUsuarioId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Perfil de tutor nao encontrado para o usuario autenticado."));
    }

    @Transactional(readOnly = true)
    public Clinica clinicaAtual(ZeloUserPrincipal principal) {
        return clinicaService.obterClinicaDoUsuario(principal.getUsuarioId());
    }
}
