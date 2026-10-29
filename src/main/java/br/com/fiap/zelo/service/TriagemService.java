package br.com.fiap.zelo.service;

import br.com.fiap.zelo.domain.Clinica;
import br.com.fiap.zelo.domain.Pet;
import br.com.fiap.zelo.domain.Triagem;
import br.com.fiap.zelo.domain.Tutor;
import br.com.fiap.zelo.domain.enums.StatusTriagem;
import br.com.fiap.zelo.exception.RecursoNaoEncontradoException;
import br.com.fiap.zelo.repository.TriagemRepository;
import br.com.fiap.zelo.repository.TutorRepository;
import br.com.fiap.zelo.web.dto.TriagemForm;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/** Fluxo "Triagem e Encaminhamento": abertura classificada, fila priorizada e transições de status. */
@Service
@Transactional
public class TriagemService {

    public static final Set<StatusTriagem> EM_ANDAMENTO = EnumSet.of(StatusTriagem.ABERTA, StatusTriagem.ENCAMINHADA);
    public static final Set<StatusTriagem> FINALIZADAS = EnumSet.of(StatusTriagem.ATENDIDA, StatusTriagem.CANCELADA);

    private static final Comparator<Triagem> ORDEM_DA_FILA = Comparator
            .comparingInt((Triagem t) -> t.getNivelUrgencia().getPrioridade())
            .thenComparing(Triagem::getCriadaEm, Comparator.nullsLast(Comparator.naturalOrder()));

    private final TriagemRepository triagemRepository;
    private final TutorRepository tutorRepository;
    private final PetService petService;
    private final ClinicaService clinicaService;
    private final ClassificadorDeUrgencia classificador;

    public TriagemService(TriagemRepository triagemRepository, TutorRepository tutorRepository, PetService petService,
                          ClinicaService clinicaService, ClassificadorDeUrgencia classificador) {
        this.triagemRepository = triagemRepository;
        this.tutorRepository = tutorRepository;
        this.petService = petService;
        this.clinicaService = clinicaService;
        this.classificador = classificador;
    }

    public Triagem abrirTriagem(Long tutorId, TriagemForm form) {
        Pet pet = petService.buscarDoTutor(form.getPetId(), tutorId);
        Tutor tutor = tutorRepository.findById(tutorId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Tutor não encontrado."));
        Clinica clinica = clinicaService.buscarAtiva(form.getClinicaId());
        ClassificacaoUrgencia classificacao = classificador.classificar(form.getRelato());

        return triagemRepository.save(Triagem.abrir(pet, tutor, clinica, form.getCanal(), form.getRelato().trim(),
                classificacao.scoreRisco(), classificacao.nivelUrgencia()));
    }

    /** Casos em andamento ordenados por urgência e, no empate, por ordem de chegada. */
    @Transactional(readOnly = true)
    public List<Triagem> filaDeAtencao(Long clinicaId) {
        return triagemRepository.findDaClinica(clinicaId, EM_ANDAMENTO, Sort.unsorted()).stream()
                .sorted(ORDEM_DA_FILA)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Triagem> historicoDaClinica(Long clinicaId) {
        return triagemRepository.findDaClinica(clinicaId, FINALIZADAS, Sort.by(Sort.Direction.DESC, "criadaEm"));
    }

    @Transactional(readOnly = true)
    public List<Triagem> historicoDoTutor(Long tutorId) {
        return triagemRepository.findDoTutor(tutorId);
    }

    public Triagem encaminhar(Long triagemId, Long clinicaId) {
        return alterarNaClinica(triagemId, clinicaId, Triagem::encaminhar);
    }

    public Triagem concluir(Long triagemId, Long clinicaId) {
        return alterarNaClinica(triagemId, clinicaId, Triagem::concluir);
    }

    public Triagem cancelarPelaClinica(Long triagemId, Long clinicaId) {
        return alterarNaClinica(triagemId, clinicaId, Triagem::cancelar);
    }

    public Triagem cancelarPeloTutor(Long triagemId, Long tutorId) {
        Triagem triagem = buscarDoTutor(triagemId, tutorId);
        triagem.cancelarPeloTutor();
        return triagemRepository.save(triagem);
    }

    /** Usado pelo aplicativo mobile para o tutor remover uma triagem própria. */
    public void excluirPeloTutor(Long triagemId, Long tutorId) {
        triagemRepository.delete(buscarDoTutor(triagemId, tutorId));
    }

    private Triagem alterarNaClinica(Long triagemId, Long clinicaId, Consumer<Triagem> transicao) {
        Triagem triagem = buscar(triagemId);
        if (!triagem.pertenceAClinica(clinicaId)) {
            throw new RecursoNaoEncontradoException("Esta triagem não pertence à sua clínica.");
        }
        transicao.accept(triagem);
        return triagemRepository.save(triagem);
    }

    private Triagem buscarDoTutor(Long triagemId, Long tutorId) {
        Triagem triagem = buscar(triagemId);
        if (!triagem.pertenceAoTutor(tutorId)) {
            throw new RecursoNaoEncontradoException("Esta triagem não pertence ao tutor autenticado.");
        }
        return triagem;
    }

    private Triagem buscar(Long triagemId) {
        return triagemRepository.findById(triagemId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Triagem não encontrada."));
    }
}
