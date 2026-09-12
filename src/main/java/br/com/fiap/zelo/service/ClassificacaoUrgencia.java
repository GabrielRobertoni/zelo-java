package br.com.fiap.zelo.service;

import br.com.fiap.zelo.domain.enums.NivelUrgencia;

import java.math.BigDecimal;

/** Resultado imutavel de uma classificacao de urgencia. */
public record ClassificacaoUrgencia(BigDecimal scoreRisco, NivelUrgencia nivelUrgencia) {
}
