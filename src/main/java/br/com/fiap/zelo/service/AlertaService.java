package br.com.fiap.zelo.service;

import br.com.fiap.zelo.domain.Alerta;
import br.com.fiap.zelo.domain.Clinica;
import br.com.fiap.zelo.domain.Pet;
import br.com.fiap.zelo.domain.enums.StatusAlerta;
import br.com.fiap.zelo.exception.RecursoNaoEncontradoException;
import br.com.fiap.zelo.exception.RegraNegocioException;
import br.com.fiap.zelo.repository.AlertaRepository;
import br.com.fiap.zelo.repository.ClinicaRepository;
import br.com.fiap.zelo.repository.PetRepository;
import br.com.fiap.zelo.web.dto.AlertaForm;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Fluxo completo nº 2 exigido pela Sprint 3 de Java Advanced: "Alertas e
 * Plano de Cuidado" (Ciclo Zelo). Cobre criacao (pelo tutor, como lembrete
 * pessoal, ou pela clinica, como recomendacao clinica), disparo e
 * confirmacao, ate o cancelamento quando aplicavel.
 */
@Service
@Transactional
public class AlertaService {

    private static final List<StatusAlerta> STATUS_PENDENTES = List.of(StatusAlerta.PENDENTE, StatusAlerta.ENVIADO);

    private final AlertaRepository alertaRepository;
    private final PetRepository petRepository;
    private final ClinicaRepository clinicaRepository;

    public AlertaService(AlertaRepository alertaRepository, PetRepository petRepository, ClinicaRepository clinicaRepository) {
        this.alertaRepository = alertaRepository;
        this.petRepository = petRepository;
        this.clinicaRepository = clinicaRepository;
    }

    public Alerta criarComoTutor(Long tutorId, AlertaForm form) {
        Pet pet = validarPetDoTutor(form.getPetId(), tutorId);
        Alerta alerta = montarBase(pet, form);
        alerta.setClinica(null);
        return alertaRepository.save(alerta);
    }

    public Alerta criarComoClinica(Long clinicaId, AlertaForm form) {
        Pet pet = petRepository.findById(form.getPetId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pet nao encontrado."));
        Clinica clinica = clinicaRepository.findById(clinicaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Clinica nao encontrada."));
        Alerta alerta = montarBase(pet, form);
        alerta.setClinica(clinica);
        return alertaRepository.save(alerta);
    }

    private Pet validarPetDoTutor(Long petId, Long tutorId) {
        if (!petRepository.existeVinculoComTutor(petId, tutorId)) {
            throw new RecursoNaoEncontradoException("Este pet nao pertence ao tutor autenticado.");
        }
        return petRepository.findById(petId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pet nao encontrado."));
    }

    private Alerta montarBase(Pet pet, AlertaForm form) {
        return Alerta.builder()
                .pet(pet)
                .tipoAlerta(form.getTipoAlerta())
                .titulo(form.getTitulo())
                .mensagem(form.getMensagem())
                .dataPrevista(form.getDataPrevista())
                .statusAlerta(StatusAlerta.PENDENTE)
                .build();
    }

    @Transactional(readOnly = true)
    public List<Alerta> pendentesDoTutor(Long tutorId) {
        return alertaRepository.findPendentesPorTutor(tutorId, STATUS_PENDENTES);
    }

    @Transactional(readOnly = true)
    public List<Alerta> daClinica(Long clinicaId) {
        return alertaRepository.findByClinicaIdOrderByDataPrevistaAsc(clinicaId);
    }

    @Transactional(readOnly = true)
    public List<Alerta> doPet(Long petId) {
        return alertaRepository.findByPetIdOrderByDataPrevistaAsc(petId);
    }

    /** A clinica dispara a notificacao ao tutor (simula o envio do lembrete). */
    public Alerta marcarComoEnviado(Long alertaId, Long clinicaId) {
        Alerta alerta = buscarDaClinica(alertaId, clinicaId);
        if (alerta.getStatusAlerta() != StatusAlerta.PENDENTE) {
            throw new RegraNegocioException("Somente alertas pendentes podem ser marcados como enviados.");
        }
        alerta.setStatusAlerta(StatusAlerta.ENVIADO);
        alerta.setDataEnvio(LocalDateTime.now());
        return alertaRepository.save(alerta);
    }

    /** O tutor confirma que o cuidado foi realizado, fechando o ciclo. */
    public Alerta confirmar(Long alertaId, Long tutorId) {
        Alerta alerta = buscarDoTutor(alertaId, tutorId);
        if (alerta.getStatusAlerta() == StatusAlerta.CONCLUIDO || alerta.getStatusAlerta() == StatusAlerta.CANCELADO) {
            throw new RegraNegocioException("Este alerta ja foi finalizado.");
        }
        alerta.setStatusAlerta(StatusAlerta.CONCLUIDO);
        return alertaRepository.save(alerta);
    }

    public Alerta cancelarPeloTutor(Long alertaId, Long tutorId) {
        Alerta alerta = buscarDoTutor(alertaId, tutorId);
        garantirNaoFinalizado(alerta);
        alerta.setStatusAlerta(StatusAlerta.CANCELADO);
        return alertaRepository.save(alerta);
    }

    public Alerta cancelarPelaClinica(Long alertaId, Long clinicaId) {
        Alerta alerta = buscarDaClinica(alertaId, clinicaId);
        garantirNaoFinalizado(alerta);
        alerta.setStatusAlerta(StatusAlerta.CANCELADO);
        return alertaRepository.save(alerta);
    }

    /** Exclusao usada pela API do aplicativo mobile (o tutor remove um lembrete pessoal). */
    public void excluirPeloTutor(Long alertaId, Long tutorId) {
        Alerta alerta = buscarDoTutor(alertaId, tutorId);
        alertaRepository.delete(alerta);
    }

    private void garantirNaoFinalizado(Alerta alerta) {
        if (alerta.getStatusAlerta() == StatusAlerta.CONCLUIDO || alerta.getStatusAlerta() == StatusAlerta.CANCELADO) {
            throw new RegraNegocioException("Este alerta ja foi finalizado.");
        }
    }

    private Alerta buscarDaClinica(Long alertaId, Long clinicaId) {
        Alerta alerta = alertaRepository.findById(alertaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Alerta nao encontrado."));
        if (alerta.getClinica() == null || !alerta.getClinica().getId().equals(clinicaId)) {
            throw new RecursoNaoEncontradoException("Este alerta nao pertence a clinica autenticada.");
        }
        return alerta;
    }

    private Alerta buscarDoTutor(Long alertaId, Long tutorId) {
        Alerta alerta = alertaRepository.findById(alertaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Alerta nao encontrado."));
        if (!petRepository.existeVinculoComTutor(alerta.getPet().getId(), tutorId)) {
            throw new RecursoNaoEncontradoException("Este alerta nao pertence a um pet do tutor autenticado.");
        }
        return alerta;
    }
}
