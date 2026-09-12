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
 * Corresponde a tabela T_CH_CLINICA (schema real do time de Banco de Dados).
 * A tabela auxiliar T_CH_CLINICA_EQUIPE (nao prevista no DDL entregue pelo
 * time de Banco de Dados - zelo_criar.sql - adicionada aqui para viabilizar
 * o Spring Security) vincula usuarios VETERINARIO/GESTOR a uma clinica - ver
 * README, secao "Decisoes assumidas".
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
}
