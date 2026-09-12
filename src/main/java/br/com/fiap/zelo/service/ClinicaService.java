package br.com.fiap.zelo.service;

import br.com.fiap.zelo.domain.Clinica;
import br.com.fiap.zelo.exception.RecursoNaoEncontradoException;
import br.com.fiap.zelo.repository.ClinicaRepository;
import br.com.fiap.zelo.web.dto.ClinicaForm;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Nao ha auto-cadastro publico de clinicas: os vinculos VETERINARIO/GESTOR a
 * uma {@link Clinica} sao provisionados via massa de dados
 * (V2__seed_data.sql), assim como os perfis de usuario correspondentes -
 * ver {@link br.com.fiap.zelo.service.AuthService}. Por isso este servico
 * cobre apenas consulta e a administracao (pelo proprio GESTOR) da clinica
 * ja existente.
 */
@Service
@Transactional
public class ClinicaService {

    private final ClinicaRepository clinicaRepository;

    public ClinicaService(ClinicaRepository clinicaRepository) {
        this.clinicaRepository = clinicaRepository;
    }

    @Transactional(readOnly = true)
    public List<Clinica> listarAtivas() {
        return clinicaRepository.findByAtivaOrderByNomeAsc("S");
    }

    @Transactional(readOnly = true)
    public Clinica obterClinicaDoUsuario(Long usuarioId) {
        return clinicaRepository.findByMembroEquipeId(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("O usuario autenticado nao esta vinculado a nenhuma clinica."));
    }

    @Transactional(readOnly = true)
    public Clinica buscarPorId(Long id) {
        return clinicaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Clinica nao encontrada."));
    }

    /** Somente GESTOR da propria clinica. */
    public Clinica atualizar(Long clinicaId, Long usuarioGestorId, ClinicaForm form) {
        Clinica clinica = obterClinicaDoUsuario(usuarioGestorId);
        garantirMesmaClinica(clinica, clinicaId);

        clinica.setNome(form.getNome());
        clinica.setCnpj(form.getCnpj());
        clinica.setTelefone(form.getTelefone());
        clinica.setEndereco(form.getEndereco());
        clinica.setCidade(form.getCidade());
        clinica.setEstado(form.getEstado());
        return clinicaRepository.save(clinica);
    }

    /** Alterna ativa/inativa em vez de excluir fisicamente (preserva o historico de triagens/alertas). */
    public Clinica alternarDisponibilidade(Long clinicaId, Long usuarioGestorId) {
        Clinica clinica = obterClinicaDoUsuario(usuarioGestorId);
        garantirMesmaClinica(clinica, clinicaId);
        clinica.setAtiva(clinica.isAtiva() ? "N" : "S");
        return clinicaRepository.save(clinica);
    }

    private void garantirMesmaClinica(Clinica clinicaDoUsuario, Long clinicaIdSolicitada) {
        if (!clinicaDoUsuario.getId().equals(clinicaIdSolicitada)) {
            throw new RecursoNaoEncontradoException("Voce nao tem permissao para gerenciar esta clinica.");
        }
    }
}
