package br.com.fiap.zelo.service.painel;

import br.com.fiap.zelo.domain.Alerta;
import br.com.fiap.zelo.domain.Pet;
import br.com.fiap.zelo.domain.Triagem;
import br.com.fiap.zelo.repository.AlertaRepository;
import br.com.fiap.zelo.repository.TriagemRepository;
import br.com.fiap.zelo.service.AlertaService;
import br.com.fiap.zelo.service.PetService;
import br.com.fiap.zelo.service.TriagemService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Painel do tutor: situação de cuidado de cada pet, próximos compromissos e histórico. */
@Service
@Transactional(readOnly = true)
public class PainelTutorService {

    private static final int LIMITE_LISTAS = 5;
    private static final int DIAS_PROXIMOS_CUIDADOS = 7;
    private static final int DIAS_HISTORICO_URGENCIA = 90;

    private final PetService petService;
    private final AlertaService alertaService;
    private final TriagemService triagemService;
    private final TriagemRepository triagemRepository;
    private final AlertaRepository alertaRepository;
    private final Clock relogio;

    public PainelTutorService(PetService petService, AlertaService alertaService, TriagemService triagemService,
                              TriagemRepository triagemRepository, AlertaRepository alertaRepository, Clock relogio) {
        this.petService = petService;
        this.alertaService = alertaService;
        this.triagemService = triagemService;
        this.triagemRepository = triagemRepository;
        this.alertaRepository = alertaRepository;
        this.relogio = relogio;
    }

    public PainelTutor montar(Long tutorId) {
        LocalDate hoje = LocalDate.now(relogio);
        List<Pet> pets = petService.listarDoTutor(tutorId);
        List<Alerta> abertos = alertaService.abertosDoTutor(tutorId);
        List<Triagem> triagens = triagemService.historicoDoTutor(tutorId);

        long atrasados = abertos.stream().filter(alerta -> alerta.getDataPrevista().isBefore(hoje)).count();
        long proximos = abertos.stream().filter(alerta -> venceEmBreve(alerta, hoje)).count();
        long emAndamento = triagens.stream().filter(triagem -> triagem.getStatusTriagem().isEmAndamento()).count();

        List<Indicador> indicadores = List.of(
                new Indicador("Pets acompanhados", String.valueOf(pets.size()), "cadastrados no Zelo", "bi-heart", "neutro"),
                new Indicador("Cuidados pendentes", String.valueOf(abertos.size()),
                        proximos + " nos próximos " + DIAS_PROXIMOS_CUIDADOS + " dias", "bi-calendar-heart", "neutro"),
                new Indicador("Cuidados atrasados", String.valueOf(atrasados),
                        atrasados == 0 ? "nenhum cuidado vencido" : "confirme ou fale com a clínica",
                        "bi-alarm", atrasados == 0 ? "positivo" : "critico"),
                new Indicador("Triagens em andamento", String.valueOf(emAndamento), "aguardando a clínica",
                        "bi-clipboard2-pulse", emAndamento == 0 ? "neutro" : "atencao"));

        Map<String, Grafico> graficos = new LinkedHashMap<>();
        graficos.put("urgencia", GraficoFactory.porUrgencia(triagemRepository.contarPorUrgenciaDoTutor(
                tutorId, hoje.minusDays(DIAS_HISTORICO_URGENCIA).atStartOfDay())));
        graficos.put("cuidados", GraficoFactory.alertasPorTipoEStatus(alertaRepository.contarPorTipoEStatusDoTutor(tutorId)));

        List<PainelTutor.SituacaoPet> situacoes = pets.stream()
                .map(pet -> situacaoDo(pet, abertos, triagens, hoje))
                .toList();

        return new PainelTutor(indicadores, graficos, situacoes,
                abertos.stream().limit(LIMITE_LISTAS).toList(),
                triagens.stream().limit(LIMITE_LISTAS).toList());
    }

    private PainelTutor.SituacaoPet situacaoDo(Pet pet, List<Alerta> abertos, List<Triagem> triagens, LocalDate hoje) {
        List<Alerta> abertosDoPet = abertos.stream().filter(alerta -> alerta.getPet().getId().equals(pet.getId())).toList();
        Optional<Triagem> ultimaTriagem = triagens.stream()
                .filter(triagem -> triagem.getPet().getId().equals(pet.getId()))
                .findFirst();

        PainelTutor.Situacao situacao;
        if (abertosDoPet.stream().anyMatch(alerta -> alerta.getDataPrevista().isBefore(hoje))) {
            situacao = PainelTutor.Situacao.ATRASADO;
        } else if (abertosDoPet.stream().anyMatch(alerta -> venceEmBreve(alerta, hoje))
                || ultimaTriagem.map(triagem -> triagem.getStatusTriagem().isEmAndamento()).orElse(false)) {
            situacao = PainelTutor.Situacao.ATENCAO;
        } else {
            situacao = PainelTutor.Situacao.EM_DIA;
        }
        return new PainelTutor.SituacaoPet(pet, abertosDoPet.stream().findFirst().orElse(null),
                ultimaTriagem.orElse(null), situacao);
    }

    private boolean venceEmBreve(Alerta alerta, LocalDate hoje) {
        LocalDate data = alerta.getDataPrevista();
        return !data.isBefore(hoje) && !data.isAfter(hoje.plusDays(DIAS_PROXIMOS_CUIDADOS));
    }
}
