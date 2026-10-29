package br.com.fiap.zelo.domain;

import br.com.fiap.zelo.domain.enums.StatusAlerta;
import br.com.fiap.zelo.domain.enums.TipoAlerta;
import br.com.fiap.zelo.exception.RegraNegocioException;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Compromisso do plano de cuidado: vacina, check-up, retenção ou pós-consulta (T_CH_ALERTA). */
@Entity
@Table(name = "T_CH_ALERTA")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Alerta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_alerta")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_pet", nullable = false)
    private Pet pet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_clinica")
    private Clinica clinica;

    @Enumerated(EnumType.STRING)
    @Column(name = "tp_alerta", nullable = false, length = 25)
    private TipoAlerta tipoAlerta;

    @Column(name = "nm_alerta", nullable = false, length = 160)
    private String titulo;

    @Column(name = "ds_alerta", length = 1000)
    private String mensagem;

    @Column(name = "dt_prevista", nullable = false)
    private LocalDate dataPrevista;

    @Column(name = "dh_envio")
    private LocalDateTime dataEnvio;

    @Enumerated(EnumType.STRING)
    @Column(name = "st_alerta", nullable = false, length = 15)
    private StatusAlerta statusAlerta;

    @Column(name = "dt_criacao", updatable = false, insertable = false)
    private LocalDateTime criadoEm;

    public static Alerta novo(Pet pet, Clinica clinica, TipoAlerta tipo, String titulo, String mensagem,
                              LocalDate dataPrevista) {
        return Alerta.builder()
                .pet(pet)
                .clinica(clinica)
                .tipoAlerta(tipo)
                .titulo(titulo)
                .mensagem(mensagem)
                .dataPrevista(dataPrevista)
                .statusAlerta(StatusAlerta.PENDENTE)
                .build();
    }

    public void marcarComoEnviado() {
        if (statusAlerta != StatusAlerta.PENDENTE) {
            throw new RegraNegocioException("Somente alertas pendentes podem ser marcados como enviados.");
        }
        statusAlerta = StatusAlerta.ENVIADO;
        dataEnvio = LocalDateTime.now();
    }

    public void concluir() {
        exigirAberto();
        statusAlerta = StatusAlerta.CONCLUIDO;
    }

    public void cancelar() {
        exigirAberto();
        statusAlerta = StatusAlerta.CANCELADO;
    }

    public boolean isAtrasado() {
        return statusAlerta.isAberto() && dataPrevista.isBefore(LocalDate.now());
    }

    public boolean isRecomendadoPorClinica() {
        return clinica != null;
    }

    public boolean pertenceAClinica(Long clinicaId) {
        return clinica != null && clinica.getId().equals(clinicaId);
    }

    private void exigirAberto() {
        if (!statusAlerta.isAberto()) {
            throw new RegraNegocioException("Este alerta já foi finalizado.");
        }
    }
}
