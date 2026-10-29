package br.com.fiap.zelo.repository.projecao;

import br.com.fiap.zelo.domain.enums.NivelUrgencia;

public record ContagemPorUrgencia(NivelUrgencia urgencia, Long total) {
}
