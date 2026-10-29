package br.com.fiap.zelo.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Clínica veterinária (T_CH_CLINICA). A equipe (VETERINARIO/GESTOR) fica em
 * T_CH_CLINICA_EQUIPE, tabela própria da aplicação Java.
 */
@Entity
@Table(name = "T_CH_CLINICA")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Clinica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_clinica")
    private Long id;

    @Column(name = "nm_clinica", nullable = false, length = 160)
    private String nome;

    @Column(name = "nr_cnpj", unique = true, length = 18)
    private String cnpj;

    @Column(name = "nr_telefone", length = 25)
    private String telefone;

    @Column(name = "ds_endereco", length = 220)
    private String endereco;

    @Column(name = "nm_cidade", length = 80)
    private String cidade;

    @Column(name = "sg_estado", length = 2)
    private String estado;

    @Column(name = "st_clinica", nullable = false, length = 1)
    @Builder.Default
    private String ativa = "S";

    @Column(name = "cd_convite", unique = true, length = 12)
    private String codigoConvite;

    @Column(name = "dt_criacao", updatable = false, insertable = false)
    private LocalDateTime criadaEm;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "T_CH_CLINICA_EQUIPE",
            joinColumns = @JoinColumn(name = "id_clinica"),
            inverseJoinColumns = @JoinColumn(name = "id_usuario")
    )
    @Builder.Default
    private List<Usuario> equipe = new ArrayList<>();

    public boolean isAtiva() {
        return "S".equalsIgnoreCase(ativa);
    }

    public void alternarDisponibilidade() {
        ativa = isAtiva() ? "N" : "S";
    }

    public void adicionarMembro(Usuario usuario) {
        equipe.add(usuario);
    }

    public String getLocalizacao() {
        if (cidade == null || cidade.isBlank()) {
            return estado == null ? "" : estado;
        }
        return estado == null || estado.isBlank() ? cidade : cidade + "/" + estado;
    }
}
