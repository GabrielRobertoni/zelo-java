package br.com.fiap.zelo.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/** Vínculo N:N entre pet e tutor (T_CH_PET_TUTOR), com chave substituta e par (pet, tutor) único. */
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
