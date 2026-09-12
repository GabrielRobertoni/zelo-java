package br.com.fiap.zelo.domain;

import br.com.fiap.zelo.domain.enums.Especie;
import br.com.fiap.zelo.domain.enums.Sexo;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

/**
 * Corresponde a tabela T_CH_PET (schema real do time de Banco de Dados):
 * entidade central do acompanhamento longitudinal do Zelo.
 */
@Entity
@Table(name = "T_CH_PET")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pet")
    private Long id;

    @Column(name = "nm_pet", nullable = false, length = 100)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(name = "tp_especie", nullable = false, length = 30)
    private Especie especie;

    @Column(name = "nm_raca", length = 80)
    private String raca;

    @Enumerated(EnumType.STRING)
    @Column(name = "tp_sexo", length = 1)
    private Sexo sexo;

    @Column(name = "dt_nascimento")
    private LocalDate dataNascimento;

    @Column(name = "vl_peso_kg", precision = 6, scale = 2)
    private BigDecimal pesoKg;

    @Column(name = "fl_castrado", length = 1)
    private String castrado;

    @Column(name = "dt_criacao", updatable = false, insertable = false)
    private LocalDateTime criadoEm;

    @OneToMany(mappedBy = "pet", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PetTutor> tutores = new ArrayList<>();

    public boolean isCastrado() {
        return "S".equalsIgnoreCase(castrado);
    }

    public Integer getIdadeAnos() {
        if (dataNascimento == null) {
            return null;
        }
        return Period.between(dataNascimento, LocalDate.now()).getYears();
    }
}
