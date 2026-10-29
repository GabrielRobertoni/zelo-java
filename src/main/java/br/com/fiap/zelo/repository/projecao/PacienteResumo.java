package br.com.fiap.zelo.repository.projecao;

import br.com.fiap.zelo.domain.enums.Especie;

import java.time.LocalDateTime;

public record PacienteResumo(Long petId, String petNome, Especie especie, Long totalTriagens,
                             LocalDateTime ultimaTriagem) {
}
