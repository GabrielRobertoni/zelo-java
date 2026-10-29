package br.com.fiap.zelo.service;

import br.com.fiap.zelo.domain.Alerta;
import br.com.fiap.zelo.domain.Clinica;
import br.com.fiap.zelo.domain.Pet;
import br.com.fiap.zelo.domain.enums.StatusAlerta;
import br.com.fiap.zelo.exception.RecursoNaoEncontradoException;
import br.com.fiap.zelo.repository.AlertaRepository;
import br.com.fiap.zelo.repository.ClinicaRepository;
import br.com.fiap.zelo.repository.PetRepository;
import br.com.fiap.zelo.web.dto.AlertaForm;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/** Fluxo "Plano de Cuidado": lembretes do tutor e recomendações da clínica até a confirmação. */
@Service
@Transactional
public class AlertaService {

    public static final Set<StatusAlerta> ABERTOS = EnumSet.of(StatusAlerta.PENDENTE, StatusAlerta.ENVIADO);
    public static final Set<StatusAlerta> FINALIZADOS = EnumSet.of(StatusAlerta.CONCLUIDO, StatusAlerta.CANCELADO);

    private static final Sort PROXIMOS_PRIMEIRO = Sort.by(Sort.Direction.ASC, "dataPrevista");
    private static final Sort RECENTES_PRIMEIRO = Sort.by(Sort.Direction.DESC, "dataPrevista");

    private final AlertaRepository alertaRepository;
    private final PetRepository petRepository;
    private final ClinicaRepository clinicaRepository;
    private final PetService petService;
    private final PacienteService pacienteService;

    public AlertaService(AlertaRepository alertaRepository, PetRepository petRepository,
                         ClinicaRepository clinicaRepository, PetService petService, PacienteService pacienteService) {
        this.alertaRepository = alertaRepository;
        this.petRepository = petRepository;
        this.clinicaRepository = clinicaRepository;
        this.petService = petService;
        this.pacienteService = pacienteService;
    }

    public Alerta criarComoTutor(Long tutorId, AlertaForm form) {
        Pet pet = petService.buscarDoTutor(form.getPetId(), tutorId);
        return alertaRepository.save(montar(pet, null, form));
    }

    /** A clínica só recomenda cuidados para os próprios pacientes. */
    public Alerta criarComoClinica(Long clinicaId, AlertaForm form) {
        Pet pet = pacienteService.buscarPaciente(form.getPetId(), clinicaId);
        Clinica clinica = clinicaRepository.findById(clinicaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Clínica não encontrada."));
        return alertaRepository.save(montar(pet, clinica, form));
    }

    @Transactional(readOnly = true)
    public List<Alerta> abertosDoTutor(Long tutorId) {
        return alertaRepository.findDoTutor(tutorId, ABERTOS, PROXIMOS_PRIMEIRO);
    }

    @Transactional(readOnly = true)
    public List<Alerta> historicoDoTutor(Long tutorId) {
        return alertaRepository.findDoTutor(tutorId, FINALIZADOS, RECENTES_PRIMEIRO);
    }

    @Transactional(readOnly = true)
    public List<Alerta> abertosDaClinica(Long clinicaId) {
        return alertaRepository.findDaClinica(clinicaId, ABERTOS, PROXIMOS_PRIMEIRO);
    }

    @Transactional(readOnly = true)
    public List<Alerta> historicoDaClinica(Long clinicaId) {
        return alertaRepository.findDaClinica(clinicaId, FINALIZADOS, RECENTES_PRIMEIRO);
    }

    public Alerta marcarComoEnviado(Long alertaId, Long clinicaId) {
        return alterarNaClinica(alertaId, clinicaId, Alerta::marcarComoEnviado);
    }

    public Alerta cancelarPelaClinica(Long alertaId, Long clinicaId) {
        return alterarNaClinica(alertaId, clinicaId, Alerta::cancelar);
    }

    /** O tutor confirma que o cuidado foi realizado, fechando o ciclo. */
    public Alerta confirmar(Long alertaId, Long tutorId) {
        return alterarDoTutor(alertaId, tutorId, Alerta::concluir);
    }

    public Alerta cancelarPeloTutor(Long alertaId, Long tutorId) {
        return alterarDoTutor(alertaId, tutorId, Alerta::cancelar);
    }

    /** Usado pelo aplicativo mobile para o tutor remover um lembrete. */
    public void excluirPeloTutor(Long alertaId, Long tutorId) {
        alertaRepository.delete(buscarDoTutor(alertaId, tutorId));
    }

    private Alerta montar(Pet pet, Clinica clinica, AlertaForm form) {
        return Alerta.novo(pet, clinica, form.getTipoAlerta(), form.getTitulo().trim(),
                Normalizador.textoOuNulo(form.getMensagem()), form.getDataPrevista());
    }

    private Alerta alterarNaClinica(Long alertaId, Long clinicaId, Consumer<Alerta> transicao) {
        Alerta alerta = buscar(alertaId);
        if (!alerta.pertenceAClinica(clinicaId)) {
            throw new RecursoNaoEncontradoException("Este alerta não pertence à sua clínica.");
        }
        transicao.accept(alerta);
        return alertaRepository.save(alerta);
    }

    private Alerta alterarDoTutor(Long alertaId, Long tutorId, Consumer<Alerta> transicao) {
        Alerta alerta = buscarDoTutor(alertaId, tutorId);
        transicao.accept(alerta);
        return alertaRepository.save(alerta);
    }

    private Alerta buscarDoTutor(Long alertaId, Long tutorId) {
        Alerta alerta = buscar(alertaId);
        if (!petRepository.existeVinculoComTutor(alerta.getPet().getId(), tutorId)) {
            throw new RecursoNaoEncontradoException("Este alerta não pertence a um pet seu.");
        }
        return alerta;
    }

    private Alerta buscar(Long alertaId) {
        return alertaRepository.findById(alertaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Alerta não encontrado."));
    }
}
