package br.com.fiap.zelo.service;

import br.com.fiap.zelo.domain.Clinica;
import br.com.fiap.zelo.domain.Pet;
import br.com.fiap.zelo.domain.Triagem;
import br.com.fiap.zelo.domain.Tutor;
import br.com.fiap.zelo.domain.enums.NivelUrgencia;
import br.com.fiap.zelo.domain.enums.StatusTriagem;
import br.com.fiap.zelo.exception.RecursoNaoEncontradoException;
import br.com.fiap.zelo.exception.RegraNegocioException;
import br.com.fiap.zelo.repository.ClinicaRepository;
import br.com.fiap.zelo.repository.PetRepository;
import br.com.fiap.zelo.repository.TriagemRepository;
import br.com.fiap.zelo.repository.TutorRepository;
import br.com.fiap.zelo.web.dto.TriagemForm;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Fluxo completo nº 1 exigido pela Sprint 3 de Java Advanced: "Triagem e
 * Encaminhamento". Cobre criacao (com classificacao automatica de
 * urgencia), fila de atencao da clinica e as transicoes de status ate a
 * conclusao ou cancelamento do caso.
 */
@Service
@Transactional
public class TriagemService {

    private static final List<StatusTriagem> STATUS_ATIVOS_NA_FILA = List.of(StatusTriagem.ABERTA, StatusTriagem.ENCAMINHADA);

    private static final Map<NivelUrgencia, Integer> PRIORIDADE_URGENCIA = new EnumMap<>(NivelUrgencia.class);
    static {
        PRIORIDADE_URGENCIA.put(NivelUrgencia.EMERGENCIA, 0);
        PRIORIDADE_URGENCIA.put(NivelUrgencia.ALTA, 1);
        PRIORIDADE_URGENCIA.put(NivelUrgencia.MEDIA, 2);
        PRIORIDADE_URGENCIA.put(NivelUrgencia.BAIXA, 3);
    }

    private final TriagemRepository triagemRepository;
    private final PetRepository petRepository;
    private final TutorRepository tutorRepository;
    private final ClinicaRepository clinicaRepository;
    private final ClassificadorDeUrgencia classificador;

    public TriagemService(TriagemRepository triagemRepository, PetRepository petRepository,
                           TutorRepository tutorRepository, ClinicaRepository clinicaRepository,
                           ClassificadorDeUrgencia classificador) {
        this.triagemRepository = triagemRepository;
        this.petRepository = petRepository;
        this.tutorRepository = tutorRepository;
        this.clinicaRepository = clinicaRepository;
        this.classificador = classificador;
    }

    public Triagem abrirTriagem(Long tutorId, TriagemForm form) {
        if (!petRepository.existeVinculoComTutor(form.getPetId(), tutorId)) {
            throw new RecursoNaoEncontradoException("Este pet nao pertence ao tutor autenticado.");
        }
        Pet pet = petRepository.findById(form.getPetId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pet nao encontrado."));
        Tutor tutor = tutorRepository.findById(tutorId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Tutor nao encontrado."));
        Clinica clinica = clinicaRepository.findById(form.getClinicaId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Clinica nao encontrada."));
        if (!clinica.isAtiva()) {
            throw new RegraNegocioException("A clinica selecionada nao esta ativa no momento.");
        }

        ClassificacaoUrgencia classificacao = classificador.classificar(form.getRelato());

        Triagem triagem = Triagem.builder()
                .pet(pet)
                .tutor(tutor)
                .clinica(clinica)
                .canal(form.getCanal())
                .relato(form.getRelato())
                .scoreRisco(classificacao.scoreRisco())
                .nivelUrgencia(classificacao.nivelUrgencia())
                .statusTriagem(StatusTriagem.ABERTA)
                .build();

        return triagemRepository.save(triagem);
    }

    @Transactional(readOnly = true)
    public List<Triagem> filaDeAtencao(Long clinicaId) {
        List<Triagem> triagens = triagemRepository.findFilaDeAtencao(clinicaId, STATUS_ATIVOS_NA_FILA);
        return triagens.stream()
                .sorted(Comparator
                        .comparing((Triagem t) -> PRIORIDADE_URGENCIA.get(t.getNivelUrgencia()))
                        .thenComparing(Triagem::getCriadaEm, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Triagem> historicoDoTutor(Long tutorId) {
        return triagemRepository.findByTutorIdOrderByCriadaEmDesc(tutorId);
    }

    @Transactional(readOnly = true)
    public List<Triagem> historicoDoPet(Long petId) {
        return triagemRepository.findByPetIdOrderByCriadaEmDesc(petId);
    }

    public Triagem encaminhar(Long triagemId, Long clinicaId) {
        Triagem triagem = buscarDaClinica(triagemId, clinicaId);
        exigirTransicao(triagem, StatusTriagem.ABERTA, "encaminhar");
        triagem.setStatusTriagem(StatusTriagem.ENCAMINHADA);
        triagem.setEncaminhadaEm(LocalDateTime.now());
        return triagemRepository.save(triagem);
    }

    public Triagem concluir(Long triagemId, Long clinicaId) {
        Triagem triagem = buscarDaClinica(triagemId, clinicaId);
        if (triagem.getStatusTriagem() != StatusTriagem.ABERTA && triagem.getStatusTriagem() != StatusTriagem.ENCAMINHADA) {
            throw new RegraNegocioException("Somente triagens abertas ou encaminhadas podem ser atendidas.");
        }
        triagem.setStatusTriagem(StatusTriagem.ATENDIDA);
        return triagemRepository.save(triagem);
    }

    public Triagem cancelarPelaClinica(Long triagemId, Long clinicaId) {
        Triagem triagem = buscarDaClinica(triagemId, clinicaId);
        if (triagem.getStatusTriagem() == StatusTriagem.ATENDIDA || triagem.getStatusTriagem() == StatusTriagem.CANCELADA) {
            throw new RegraNegocioException("Nao e possivel cancelar uma triagem ja finalizada.");
        }
        triagem.setStatusTriagem(StatusTriagem.CANCELADA);
        return triagemRepository.save(triagem);
    }

    public Triagem cancelarPeloTutor(Long triagemId, Long tutorId) {
        Triagem triagem = triagemRepository.findById(triagemId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Triagem nao encontrada."));
        if (!triagem.getTutor().getId().equals(tutorId)) {
            throw new RecursoNaoEncontradoException("Esta triagem nao pertence ao tutor autenticado.");
        }
        exigirTransicao(triagem, StatusTriagem.ABERTA, "cancelar");
        triagem.setStatusTriagem(StatusTriagem.CANCELADA);
        return triagemRepository.save(triagem);
    }

    /** Exclusao usada pela API do aplicativo mobile (o tutor remove uma triagem sua). */
    public void excluirPeloTutor(Long triagemId, Long tutorId) {
        Triagem triagem = triagemRepository.findById(triagemId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Triagem nao encontrada."));
        if (triagem.getTutor() == null || !triagem.getTutor().getId().equals(tutorId)) {
            throw new RecursoNaoEncontradoException("Esta triagem nao pertence ao tutor autenticado.");
        }
        triagemRepository.delete(triagem);
    }

    private Triagem buscarDaClinica(Long triagemId, Long clinicaId) {
        Triagem triagem = triagemRepository.findById(triagemId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Triagem nao encontrada."));
        if (triagem.getClinica() == null || !triagem.getClinica().getId().equals(clinicaId)) {
            throw new RecursoNaoEncontradoException("Esta triagem nao pertence a clinica autenticada.");
        }
        return triagem;
    }

    private void exigirTransicao(Triagem triagem, StatusTriagem statusEsperado, String acao) {
        if (triagem.getStatusTriagem() != statusEsperado) {
            throw new RegraNegocioException(
                    "Nao e possivel " + acao + " uma triagem com status " + triagem.getStatusTriagem() + ".");
        }
    }
}
