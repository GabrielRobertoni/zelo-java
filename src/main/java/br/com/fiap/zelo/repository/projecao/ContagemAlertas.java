package br.com.fiap.zelo.repository.projecao;

import br.com.fiap.zelo.domain.enums.StatusAlerta;
import br.com.fiap.zelo.domain.enums.TipoAlerta;

public record ContagemAlertas(TipoAlerta tipo, StatusAlerta status, Long total) {
}
