package br.com.fiap.zelo.service.linhadotempo;

import br.com.fiap.zelo.domain.Alerta;
import br.com.fiap.zelo.domain.Triagem;

import java.time.LocalDateTime;

/**
 * Item da linha do tempo do pet, unificando triagens e cuidados.
 *
 * @param destaque        classe CSS do selo (urgência da triagem ou tipo do cuidado)
 * @param classeSituacao classe CSS do status (mesmas cores das listas)
 */
public record EventoLinhaDoTempo(LocalDateTime momento, boolean triagem, String icone, String titulo,
                                 String descricao, String selo, String destaque, String situacao,
                                 String classeSituacao, String origem, boolean futuro) {

    private static final String ORIGEM_TUTOR = "Lembrete do tutor";

    static EventoLinhaDoTempo de(Triagem triagem) {
        return new EventoLinhaDoTempo(triagem.getCriadaEm(), true, "bi-clipboard2-pulse",
                "Triagem — score de risco " + triagem.getScoreRisco().stripTrailingZeros().toPlainString(),
                triagem.getRelato(), triagem.getNivelUrgencia().getRotulo(),
                "badge-urgencia-" + triagem.getNivelUrgencia().name(), triagem.getStatusTriagem().getRotulo(),
                "badge-status-" + triagem.getStatusTriagem().name(),
                triagem.getClinica() == null ? null : triagem.getClinica().getNome(), false);
    }

    static EventoLinhaDoTempo de(Alerta alerta, LocalDateTime agora) {
        LocalDateTime momento = alerta.getDataPrevista().atStartOfDay();
        return new EventoLinhaDoTempo(momento, false, "bi-calendar-heart", alerta.getTitulo(),
                alerta.getMensagem(), alerta.getTipoAlerta().getRotulo(), "badge-cuidado",
                alerta.isAtrasado() ? "Atrasado" : alerta.getStatusAlerta().getRotulo(),
                "badge-status-" + (alerta.isAtrasado() ? "ATRASADO" : alerta.getStatusAlerta().name()),
                alerta.isRecomendadoPorClinica() ? alerta.getClinica().getNome() : ORIGEM_TUTOR,
                momento.isAfter(agora));
    }
}
