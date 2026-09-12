package br.com.fiap.zelo.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Corresponde a tabela T_CH_TUTOR (schema real do time de Banco de Dados):
 * dados complementares de um usuario que exerce o papel de tutor (relacao
 * 1:1 com T_CH_USUARIO).
 */
@Entity
@Table(name = "T_CH_TUTOR")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tutor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tutor")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false, unique = true)
    private Usuario usuario;

    @Column(name = "nr_telefone", length = 25)
    private String telefone;

    @Column(name = "nm_cidade", length = 80)
    private String cidade;

    @Column(name = "sg_estado", length = 2)
    private String estado;

    @Column(name = "dt_criacao", updatable = false, insertable = false)
    private LocalDateTime criadoEm;
}
