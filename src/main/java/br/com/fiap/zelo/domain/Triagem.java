package br.com.fiap.zelo.domain;

import br.com.fiap.zelo.domain.enums.CanalTriagem;
import br.com.fiap.zelo.domain.enums.NivelUrgencia;
import br.com.fiap.zelo.domain.enums.StatusTriagem;
import br.com.fiap.zelo.exception.RegraNegocioException;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Pré-atendimento do fluxo "Triagem e Encaminhamento" (tabela T_CH_TRIAGEM). */
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

    public static Triagem abrir(Pet pet, Tutor tutor, Clinica clinica, CanalTriagem canal, String relato,
                                BigDecimal scoreRisco, NivelUrgencia nivelUrgencia) {
        return Triagem.builder()
                .pet(pet)
                .tutor(tutor)
                .clinica(clinica)
                .canal(canal)
                .relato(relato)
                .scoreRisco(scoreRisco)
                .nivelUrgencia(nivelUrgencia)
                .statusTriagem(StatusTriagem.ABERTA)
                .build();
    }

    public void encaminhar() {
        exigirStatus(StatusTriagem.ABERTA, "encaminhar");
        statusTriagem = StatusTriagem.ENCAMINHADA;
        encaminhadaEm = LocalDateTime.now();
    }

    public void concluir() {
        exigirEmAndamento("Somente triagens abertas ou encaminhadas podem ser atendidas.");
        statusTriagem = StatusTriagem.ATENDIDA;
    }

    public void cancelar() {
        exigirEmAndamento("Não é possível cancelar uma triagem já finalizada.");
        statusTriagem = StatusTriagem.CANCELADA;
    }

    public void cancelarPeloTutor() {
        exigirStatus(StatusTriagem.ABERTA, "cancelar");
        statusTriagem = StatusTriagem.CANCELADA;
    }

    public boolean pertenceAClinica(Long clinicaId) {
        return clinica != null && clinica.getId().equals(clinicaId);
    }

    public boolean pertenceAoTutor(Long tutorId) {
        return tutor != null && tutor.getId().equals(tutorId);
    }

    private void exigirStatus(StatusTriagem esperado, String acao) {
        if (statusTriagem != esperado) {
            throw new RegraNegocioException(
                    "Não é possível " + acao + " uma triagem com status " + statusTriagem.getRotulo().toLowerCase() + ".");
        }
    }

    private void exigirEmAndamento(String mensagem) {
        if (!statusTriagem.isEmAndamento()) {
            throw new RegraNegocioException(mensagem);
        }
    }
}
