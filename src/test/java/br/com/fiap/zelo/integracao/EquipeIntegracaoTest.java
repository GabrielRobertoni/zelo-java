package br.com.fiap.zelo.integracao;

import br.com.fiap.zelo.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** O gestor monta a equipe: cadastra membros, controla o acesso e renova o código de convite. */
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class EquipeIntegracaoTest extends IntegracaoBase {

    private static final String GESTOR = "gestor@zelo.com.br";

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    void gestorCadastraMembroQueJaConsegueEntrar() throws Exception {
        mvc.perform(post("/clinica/gerenciar/equipe").with(como(GESTOR)).with(csrf())
                        .param("tipoUsuario", "GESTOR")
                        .param("nome", "Gestor Adjunto")
                        .param("email", "adjunto@exemplo.com")
                        .param("senha", "inicial123")
                        .param("confirmarSenha", "inicial123"))
                .andExpect(redirectedUrl("/clinica/gerenciar/equipe"));

        mvc.perform(formLogin("/login").user("adjunto@exemplo.com").password("inicial123"))
                .andExpect(authenticated().withRoles("GESTOR"));
    }

    @Test
    void membroDesativadoNaoConsegueEntrarAteSerReativado() throws Exception {
        Long fabio = usuarioRepository.findByEmailIgnoreCase("veterinario3@zelo.com.br").orElseThrow().getId();

        mvc.perform(post("/clinica/gerenciar/equipe/" + fabio + "/alternar").with(como(GESTOR)).with(csrf()))
                .andExpect(flash().attribute("sucesso", "Acesso de Fábio Vet desativado."));
        mvc.perform(formLogin("/login").user("veterinario3@zelo.com.br").password(SENHA_DEMO))
                .andExpect(unauthenticated())
                .andExpect(redirectedUrl("/login?erro"));

        mvc.perform(post("/clinica/gerenciar/equipe/" + fabio + "/alternar").with(como(GESTOR)).with(csrf()))
                .andExpect(flash().attribute("sucesso", "Acesso de Fábio Vet reativado."));
        mvc.perform(formLogin("/login").user("veterinario3@zelo.com.br").password(SENHA_DEMO))
                .andExpect(authenticated());
    }

    @Test
    void gestorNaoMexeEmMembroDeOutraClinica() throws Exception {
        Long elisa = usuarioRepository.findByEmailIgnoreCase("veterinario2@zelo.com.br").orElseThrow().getId();

        mvc.perform(post("/clinica/gerenciar/equipe/" + elisa + "/alternar").with(como(GESTOR)).with(csrf()))
                .andExpect(flash().attribute("erro", "Membro não encontrado na equipe desta clínica."));
        assertThat(usuarioRepository.findById(elisa).orElseThrow().isAtivo()).isTrue();
    }

    @Test
    void veterinarioNaoAdministraAEquipe() throws Exception {
        mvc.perform(post("/clinica/gerenciar/equipe").with(como("veterinario@zelo.com.br")).with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void novoCodigoDeConviteInvalidaOAnterior() throws Exception {
        mvc.perform(post("/clinica/gerenciar/convite").with(como("gestor2@zelo.com.br")).with(csrf()))
                .andExpect(flash().attribute("sucesso", containsString("Novo código de convite")));

        mvc.perform(post("/registro/veterinario").with(csrf())
                        .param("codigoConvite", "PAULISTA26")
                        .param("nome", "Vet Atrasado")
                        .param("email", "atrasado@exemplo.com")
                        .param("senha", "segredo123")
                        .param("confirmarSenha", "segredo123"))
                .andExpect(content().string(containsString("Código de convite inválido")));
    }
}
