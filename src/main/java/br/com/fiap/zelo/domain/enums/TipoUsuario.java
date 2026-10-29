package br.com.fiap.zelo.domain.enums;

/**
 * Perfis de acesso:
 *  - TUTOR: seus pets, triagens e plano de cuidado.
 *  - VETERINARIO: painel, fila, pacientes e alertas da clínica.
 *  - GESTOR: tudo do veterinário, mais dados da clínica e equipe.
 */
public enum TipoUsuario {
    TUTOR("Tutor"),
    VETERINARIO("Veterinário"),
    GESTOR("Gestor");

    private final String rotulo;

    TipoUsuario(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() {
        return rotulo;
    }

    public String authority() {
        return "ROLE_" + name();
    }

    public boolean isEquipeClinica() {
        return this == VETERINARIO || this == GESTOR;
    }
}
