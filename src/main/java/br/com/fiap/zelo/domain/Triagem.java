package br.com.fiap.zelo.domain;

import br.com.fiap.zelo.domain.enums.CanalTriagem;
import br.com.fiap.zelo.domain.enums.NivelUrgencia;
import br.com.fiap.zelo.domain.enums.StatusTriagem;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Corresponde a tabela T_CH_TRIAGEM (schema real do time de Banco de Dados):
 * registro do pre-atendimento do fluxo "Triagem e Encaminhamento". E a
 * tabela referenciada pela trigger de auditoria da disciplina de Banco de
 * Dados (T_CH_AUDITORIA_DML).
 */
@Entity
@Table(name = "T_CH_TRIAGEM")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Triagem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_triagem")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_pet", nullable = false)
    private Pet pet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_clinica")
    private Clinica clinica;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tutor")
    private Tutor tutor;

    @Enumerated(EnumType.STRING)
    @Column(name = "tp_canal", nullable = false, length = 15)
    private CanalTriagem canal;

    @Column(name = "ds_relato", nullable = false, length = 2000)
    private String relato;

    @Column(name = "vl_score_risco", nullable = false, precision = 5, scale = 2)
    private BigDecimal scoreRisco;

    @Enumerated(EnumType.STRING)
    @Column(name = "tp_urgencia", nullable = false, length = 15)
    private NivelUrgencia nivelUrgencia;

    @Column(name = "ds_analise_visual", length = 1000)
    private String analiseVisual;

    @Column(name = "dh_encaminhamento")
    private LocalDateTime encaminhadaEm;

    @Enumerated(EnumType.STRING)
    @Column(name = "st_triagem", nullable = false, length = 20)
    private StatusTriagem statusTriagem;

    @Column(name = "dt_criacao", updatable = false, insertable = false)
    private LocalDateTime criadaEm;
}
