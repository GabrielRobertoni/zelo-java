package br.com.fiap.zelo.domain.enums;

/**
 * Perfis de acesso da plataforma Zelo.
 * Corresponde ao dominio da coluna ZELO_USUARIO.tipo_usuario (CHECK), definido
 * pela modelagem relacional entregue pelo grupo de Banco de Dados.
 *
 * Regras de autorizacao (Spring Security):
 *  - TUTOR: acessa seus proprios pets, cria triagens e confirma alertas.
 *  - VETERINARIO: acessa o painel da clinica, atende triagens e gerencia alertas dos pets atendidos.
 *  - GESTOR: tudo que o VETERINARIO pode, alem de administrar o cadastro da propria clinica.
 */
public enum TipoUsuario {
    TUTOR,
    VETERINARIO,
    GESTOR;

    public String authority() {
        return "ROLE_" + name();
    }
}
