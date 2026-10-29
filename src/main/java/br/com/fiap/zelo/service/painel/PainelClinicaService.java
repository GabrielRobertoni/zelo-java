package br.com.fiap.zelo.service.painel;

import br.com.fiap.zelo.domain.Alerta;
import br.com.fiap.zelo.domain.Clinica;
import br.com.fiap.zelo.domain.Triagem;
import br.com.fiap.zelo.domain.enums.StatusAlerta;
import br.com.fiap.zelo.domain.enums.StatusTriagem;
import br.com.fiap.zelo.repository.AlertaRepository;
import br.com.fiap.zelo.repository.TriagemRepository;
import br.com.fiap.zelo.repository.projecao.ContagemAlertas;
import br.com.fiap.zelo.repository.projecao.ContagemPorStatusTriagem;
import br.com.fiap.zelo.repository.projecao.MomentoTriagem;
import br.com.fiap.zelo.service.AlertaService;
import br.com.fiap.zelo.service.EquipeService;
import br.com.fiap.zelo.service.TriagemService;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;

/**
 * Painel operacional da clínica: indicadores, gráficos e listas de ação.
 * As contagens usam consultas agregadas (group by) no banco; só a série
 * diária e o tempo médio são calculados aqui, a partir de uma projeção leve
 * com as datas das triagens do período.
 */
@Service
@Transactional(readOnly = true)
public class PainelClinicaService {

    private static final int LIMITE_LISTAS = 5;
    private static final int DIAS_PROXIMOS_ALERTAS = 7;
    private static final DateTimeFormatter ROTULO_DIA = DateTimeFormatter.ofPattern("dd/MM");

    private final TriagemRepository triagemRepository;
    private final AlertaRepository alertaRepository;
    private final TriagemService triagemService;
    private final AlertaService alertaService;
    private final EquipeService equipeService;
    private final Clock relogio;

    public PainelClinicaService(TriagemRepository triagemRepository, AlertaRepository alertaRepository,
                                TriagemService triagemService, AlertaService alertaService,
                                EquipeService equipeService, Clock relogio) {
        this.triagemRepository = triagemRepository;
        this.alertaRepository = alertaRepository;
        this.triagemService = triagemService;
        this.alertaService = alertaService;
        this.equipeService = equipeService;
        this.relogio = relogio;
    }

    public PainelClinica montar(Clinica clinica, Periodo periodo) {
        Long clinicaId = clinica.getId();
        LocalDate hoje = LocalDate.now(relogio);
        LocalDate inicio = periodo.inicio(hoje);
        LocalDateTime desde = inicio.atStartOfDay();

        List<Triagem> fila = triagemService.filaDeAtencao(clinicaId);
        List<MomentoTriagem> momentos = triagemRepository.listarMomentosNaClinica(clinicaId, desde);
        Map<StatusTriagem, Long> porStatus = ContadorPorEnum.contar(StatusTriagem.class,
                triagemRepository.contarPorStatusNaClinica(clinicaId, desde),
                ContagemPorStatusTriagem::status, ContagemPorStatusTriagem::total);
        List<ContagemAlertas> contagemAlertas = alertaRepository.contarPorTipoEStatusNaClinica(clinicaId, inicio);
        List<Alerta> abertos = alertaService.abertosDaClinica(clinicaId);
        List<Alerta> atrasados = abertos.stream().filter(alerta -> alerta.getDataPrevista().isBefore(hoje)).toList();
        List<Alerta> proximos = abertos.stream()
                .filter(alerta -> !alerta.getDataPrevista().isBefore(hoje)
                        && !alerta.getDataPrevista().isAfter(hoje.plusDays(DIAS_PROXIMOS_ALERTAS)))
                .toList();

        List<Indicador> indicadores = List.of(
                indicadorFila(fila),
                new Indicador("Triagens no período", String.valueOf(momentos.size()), periodo.getRotulo(),
                        "bi-clipboard2-pulse", "neutro"),
                indicadorResolucao(porStatus, momentos.size()),
                new Indicador("Tempo até encaminhar", Formatos.duracao(tempoMedioAteEncaminhar(momentos)),
                        "média da abertura ao encaminhamento", "bi-stopwatch", "neutro"),
                indicadorAtrasados(atrasados, proximos),
                indicadorAdesao(contagemAlertas, atrasados, inicio));

        Map<String, Grafico> graficos = new LinkedHashMap<>();
        graficos.put("urgencia", GraficoFactory.porUrgencia(triagemRepository.contarPorUrgenciaNaClinica(clinicaId, desde)));
        graficos.put("evolucao", evolucaoDiaria(momentos, inicio, hoje));
        graficos.put("status", porStatusTriagem(porStatus));
        graficos.put("alertas", GraficoFactory.alertasPorTipoEStatus(contagemAlertas));

        return new PainelClinica(clinica, periodo, indicadores, graficos,
                fila.stream().limit(LIMITE_LISTAS).toList(), fila.size(),
                atrasados.stream().limit(LIMITE_LISTAS).toList(),
                proximos.stream().limit(LIMITE_LISTAS).toList(),
                triagemRepository.listarPacientesRecorrentes(clinicaId, desde, PageRequest.of(0, LIMITE_LISTAS)),
                equipeService.listarMembros(clinicaId).size());
    }

    private Indicador indicadorFila(List<Triagem> fila) {
        long criticas = fila.stream().filter(triagem -> triagem.getNivelUrgencia().isCritica()).count();
        return new Indicador("Na fila agora", String.valueOf(fila.size()),
                Formatos.quantidade(criticas, "caso crítico", "casos críticos") + " (alta ou emergência)",
                "bi-list-check", criticas > 0 ? "critico" : "neutro");
    }

    private Indicador indicadorResolucao(Map<StatusTriagem, Long> porStatus, long total) {
        long atendidas = porStatus.get(StatusTriagem.ATENDIDA);
        return new Indicador("Taxa de resolução", Formatos.percentual(atendidas, total),
                Formatos.quantidade(atendidas, "triagem atendida", "triagens atendidas")
                        + ", " + porStatus.get(StatusTriagem.CANCELADA) + " canceladas",
                "bi-check2-circle", "positivo");
    }

    private Indicador indicadorAtrasados(List<Alerta> atrasados, List<Alerta> proximos) {
        return new Indicador("Alertas atrasados", String.valueOf(atrasados.size()),
                Formatos.quantidade(proximos.size(), "vence", "vencem") + " nos próximos " + DIAS_PROXIMOS_ALERTAS + " dias",
                "bi-alarm", atrasados.isEmpty() ? "positivo" : "atencao");
    }

    /** Dos cuidados que venceram no período, quantos foram cumpridos. */
    private Indicador indicadorAdesao(List<ContagemAlertas> contagem, List<Alerta> atrasados, LocalDate inicio) {
        long concluidos = contagem.stream()
                .filter(linha -> linha.status() == StatusAlerta.CONCLUIDO)
                .mapToLong(ContagemAlertas::total)
                .sum();
        long vencidosNoPeriodo = atrasados.stream().filter(alerta -> !alerta.getDataPrevista().isBefore(inicio)).count();
        return new Indicador("Adesão ao plano de cuidado", Formatos.percentual(concluidos, concluidos + vencidosNoPeriodo),
                "cuidados vencidos que foram concluídos", "bi-heart-pulse", "positivo");
    }

    private Duration tempoMedioAteEncaminhar(List<MomentoTriagem> momentos) {
        OptionalDouble mediaSegundos = momentos.stream()
                .filter(momento -> momento.encaminhadaEm() != null)
                .mapToLong(momento -> Duration.between(momento.criadaEm(), momento.encaminhadaEm()).getSeconds())
                .average();
        return mediaSegundos.isPresent() ? Duration.ofSeconds(Math.round(mediaSegundos.getAsDouble())) : null;
    }

    private Grafico evolucaoDiaria(List<MomentoTriagem> momentos, LocalDate inicio, LocalDate hoje) {
        List<String> rotulos = new ArrayList<>();
        List<Long> todas = new ArrayList<>();
        List<Long> criticas = new ArrayList<>();
        for (LocalDate dia = inicio; !dia.isAfter(hoje); dia = dia.plusDays(1)) {
            LocalDate diaAtual = dia;
            List<MomentoTriagem> doDia = momentos.stream()
                    .filter(momento -> momento.criadaEm().toLocalDate().equals(diaAtual))
                    .toList();
            rotulos.add(dia.format(ROTULO_DIA));
            todas.add((long) doDia.size());
            criticas.add(doDia.stream().filter(momento -> momento.urgencia().isCritica()).count());
        }
        return new Grafico(rotulos, rotulos, List.of(
                new Grafico.Serie("TODAS", "Todas as triagens", todas),
                new Grafico.Serie("CRITICAS", "Alta ou emergência", criticas)));
    }

    private Grafico porStatusTriagem(Map<StatusTriagem, Long> porStatus) {
        List<StatusTriagem> status = Arrays.asList(StatusTriagem.values());
        return new Grafico(
                status.stream().map(Enum::name).toList(),
                status.stream().map(StatusTriagem::getRotulo).toList(),
                List.of(new Grafico.Serie("TRIAGENS", "Triagens", status.stream().map(porStatus::get).toList())));
    }
}
