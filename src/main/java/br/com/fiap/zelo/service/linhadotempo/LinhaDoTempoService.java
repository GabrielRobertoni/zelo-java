package br.com.fiap.zelo.service.linhadotempo;

import br.com.fiap.zelo.domain.Alerta;
import br.com.fiap.zelo.domain.Triagem;
import br.com.fiap.zelo.repository.AlertaRepository;
import br.com.fiap.zelo.repository.TriagemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/** Histórico longitudinal do pet: triagens e cuidados em uma única linha do tempo, do mais recente ao mais antigo. */
@Service
@Transactional(readOnly = true)
public class LinhaDoTempoService {

    private final TriagemRepository triagemRepository;
    private final AlertaRepository alertaRepository;
    private final Clock relogio;

    public LinhaDoTempoService(TriagemRepository triagemRepository, AlertaRepository alertaRepository, Clock relogio) {
        this.triagemRepository = triagemRepository;
        this.alertaRepository = alertaRepository;
        this.relogio = relogio;
    }

    /** Visão do tutor: tudo o que aconteceu com o pet, em qualquer clínica. */
    public List<EventoLinhaDoTempo> doPet(Long petId) {
        return montar(triagemRepository.findDoPet(petId), alertaRepository.findDoPet(petId));
    }

    /** Visão da clínica: apenas o que foi registrado nela. */
    public List<EventoLinhaDoTempo> doPacienteNaClinica(Long petId, Long clinicaId) {
        return montar(triagemRepository.findDoPetNaClinica(petId, clinicaId),
                alertaRepository.findDoPetNaClinica(petId, clinicaId));
    }

    private List<EventoLinhaDoTempo> montar(List<Triagem> triagens, List<Alerta> alertas) {
        LocalDateTime agora = LocalDateTime.now(relogio);
        return Stream.concat(
                        triagens.stream().map(EventoLinhaDoTempo::de),
                        alertas.stream().map(alerta -> EventoLinhaDoTempo.de(alerta, agora)))
                .sorted(Comparator.comparing(EventoLinhaDoTempo::momento).reversed())
                .toList();
    }
}
