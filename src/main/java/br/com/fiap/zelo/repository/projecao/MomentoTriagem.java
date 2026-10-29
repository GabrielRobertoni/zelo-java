package br.com.fiap.zelo.repository.projecao;

import br.com.fiap.zelo.domain.enums.NivelUrgencia;

import java.time.LocalDateTime;

/** Datas de uma triagem, usadas na série diária e no tempo médio até o encaminhamento. */
public record MomentoTriagem(LocalDateTime criadaEm, NivelUrgencia urgencia, LocalDateTime encaminhadaEm) {
}
