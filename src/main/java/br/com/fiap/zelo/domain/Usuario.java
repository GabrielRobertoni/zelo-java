package br.com.fiap.zelo.domain;

import br.com.fiap.zelo.domain.enums.TipoUsuario;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** Identidade de acesso (T_CH_USUARIO). Especializa-se em Tutor ou em membro da equipe de uma Clínica. */
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

    public static Usuario novo(String nome, String email, String senhaHash, TipoUsuario tipo) {
        return Usuario.builder()
                .nome(nome.trim())
                .email(email.trim().toLowerCase())
                .senhaHash(senhaHash)
                .tipoUsuario(tipo)
                .build();
    }

    /** Iniciais do primeiro e do último nome, usadas no avatar do menu (ex.: "Ana Gestora" -> "AG"). */
    public String getIniciais() {
        String[] partes = nome == null ? new String[0] : nome.trim().split("\\s+");
        if (partes.length == 0 || partes[0].isEmpty()) {
            return "?";
        }
        String iniciais = partes[0].substring(0, 1);
        if (partes.length > 1) {
            iniciais += partes[partes.length - 1].substring(0, 1);
        }
        return iniciais.toUpperCase();
    }

    public boolean isAtivo() {
        return "S".equalsIgnoreCase(ativo);
    }

    public void alternarAtivo() {
        ativo = isAtivo() ? "N" : "S";
    }
}
