package br.com.fiap.zelo.domain;

import br.com.fiap.zelo.domain.enums.TipoUsuario;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Corresponde a tabela T_CH_USUARIO (schema real do time de Banco de Dados,
 * arquivo zelo_criar.sql): identidade de acesso da plataforma. Um mesmo
 * usuario pode se especializar em Tutor (tabela T_CH_TUTOR) ou pertencer a
 * equipe de uma Clinica, dependendo do tipoUsuario.
 */
@Entity
@Table(name = "T_CH_USUARIO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long id;

    @Column(name = "nm_usuario", nullable = false, length = 120)
    private String nome;

    @Column(name = "ds_email", nullable = false, unique = true, length = 160)
    private String email;

    @Column(name = "ds_senha", nullable = false, length = 255)
    private String senhaHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "tp_usuario", nullable = false, length = 20)
    private TipoUsuario tipoUsuario;

    @Column(name = "st_usuario", nullable = false, length = 1)
    @Builder.Default
    private String ativo = "S";

    @Column(name = "dt_criacao", updatable = false, insertable = false)
    private LocalDateTime criadoEm;

    public boolean isAtivo() {
        return "S".equalsIgnoreCase(ativo);
    }
}
