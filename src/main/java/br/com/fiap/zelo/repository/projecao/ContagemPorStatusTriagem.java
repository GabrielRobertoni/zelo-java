package br.com.fiap.zelo.repository.projecao;

import br.com.fiap.zelo.domain.enums.StatusTriagem;

public record ContagemPorStatusTriagem(StatusTriagem status, Long total) {
}
