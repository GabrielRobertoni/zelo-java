package br.com.fiap.zelo.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Corresponde a tabela T_CH_PET_TUTOR (schema real do time de Banco de
 * Dados): implementa o recurso Multi-Tutor (varios tutores podem acompanhar
 * o mesmo pet).
 *
 * IMPORTANTE: diferente da modelagem original assumida antes de recebermos
 * o DDL real (zelo_criar.sql), esta tabela usa chave primaria substituta
 * (id_pet_tutor, auto-gerada) em vez de chave composta (id_pet, id_tutor) -
 * o par (id_pet, id_tutor) e apenas uma constraint UNIQUE. Por isso a
 * entidade usa um "Long id" simples, sem @EmbeddedId/@MapsId.
 */
@Entity
@Table(name = "T_CH_PET_TUTOR")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PetTutor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pet_tutor")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_pet", nullable = false)
    private Pet pet;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_tutor", nullable = false)
    private Tutor tutor;

    @Column(name = "fl_principal", nullable = false, length = 1)
    @Builder.Default
    private String responsavelPrincipal = "S";

    @Column(name = "dt_vinculo", nullable = false)
    @Builder.Default
    private LocalDate dataVinculo = LocalDate.now();

    public boolean isResponsavelPrincipal() {
        return "S".equalsIgnoreCase(responsavelPrincipal);
    }
}
