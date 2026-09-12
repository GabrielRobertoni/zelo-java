package br.com.fiap.zelo.domain;

import br.com.fiap.zelo.domain.enums.StatusAlerta;
import br.com.fiap.zelo.domain.enums.TipoAlerta;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Corresponde a tabela T_CH_ALERTA (schema real do time de Banco de Dados):
 * sustenta o fluxo "Plano de Cuidado / Ciclo Zelo" (vacinas, retencoes,
 * pos-consulta e check-ups).
 */
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
}
