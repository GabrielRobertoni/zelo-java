package br.com.fiap.zelo.integracao;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Renderiza de verdade todas as telas de cada perfil e confere a proteção das rotas. */
class NavegacaoPorPerfilTest extends IntegracaoBase {

    private void carregaTodas(String email, List<String> urls) throws Exception {
        for (String url : urls) {
            mvc.perform(get(url).with(como(email)))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("</html>")))
                    .andExpect(content().string(not(containsString("Whitelabel"))));
        }
    }

    @Test
    void todasAsTelasDoTutorCarregam() throws Exception {
        Long rex = idDoPet("Rex");
        carregaTodas("tutor@zelo.com.br", List.of("/", "/tutor/painel", "/tutor/pets", "/tutor/pets/novo",
                "/tutor/pets/" + rex, "/tutor/pets/" + rex + "/editar", "/tutor/triagens", "/tutor/triagens/nova",
                "/tutor/triagens/nova?petId=" + rex, "/tutor/alertas", "/tutor/alertas?aba=historico",
                "/tutor/alertas/novo", "/tutor/alertas/novo?petId=" + rex));
    }

    @Test
    void todasAsTelasDoVeterinarioCarregam() throws Exception {
        Long rex = idDoPet("Rex");
        carregaTodas("veterinario@zelo.com.br", List.of("/clinica/painel", "/clinica/painel?periodo=7",
                "/clinica/painel?periodo=90", "/clinica/painel?periodo=abc", "/clinica/triagens",
                "/clinica/triagens?aba=historico", "/clinica/pacientes", "/clinica/pacientes/" + rex,
                "/clinica/alertas", "/clinica/alertas?aba=historico", "/clinica/alertas/novo",
                "/clinica/alertas/novo?petId=" + rex));
    }

    @Test
    void todasAsTelasDoGestorCarregam() throws Exception {
        carregaTodas("gestor@zelo.com.br", List.of("/clinica/painel", "/clinica/triagens", "/clinica/pacientes",
                "/clinica/alertas", "/clinica/gerenciar", "/clinica/gerenciar/equipe"));
    }

    @Test
    void cadaTutorVeApenasOsProprioPets() throws Exception {
        mvc.perform(get("/tutor/pets").with(como("tutor2@zelo.com.br")))
                .andExpect(content().string(containsString("Thor")))
                .andExpect(content().string(not(containsString("Rex"))));
    }

    @ParameterizedTest
    @ValueSource(strings = {"/", "/login", "/registro", "/registro/tutor", "/registro/gestor", "/registro/veterinario"})
    void paginasPublicasCarregamSemLogin(String url) throws Exception {
        mvc.perform(get(url)).andExpect(status().isOk());
    }

    @ParameterizedTest
    @ValueSource(strings = {"/tutor/painel", "/clinica/painel", "/clinica/gerenciar"})
    void areasRestritasRedirecionamParaOLogin(String url) throws Exception {
        mvc.perform(get(url))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", endsWith("/login")));
    }

    @ParameterizedTest
    @CsvSource({
            "tutor@zelo.com.br, /clinica/painel",
            "tutor@zelo.com.br, /clinica/gerenciar",
            "veterinario@zelo.com.br, /tutor/painel",
            "veterinario@zelo.com.br, /clinica/gerenciar",
            "veterinario@zelo.com.br, /clinica/gerenciar/equipe",
            "gestor@zelo.com.br, /tutor/pets"
    })
    void perfilSemPermissaoRecebe403(String email, String url) throws Exception {
        mvc.perform(get(url).with(como(email)))
                .andExpect(status().isForbidden());
    }

    @Test
    void paginaDeAcessoNegadoEAmigavel() throws Exception {
        mvc.perform(get("/error").requestAttr("jakarta.servlet.error.status_code", 403).accept(MediaType.TEXT_HTML)
                        .with(como("tutor@zelo.com.br")))
                .andExpect(content().string(containsString("Acesso restrito ao seu perfil")));
    }

    @Test
    void usuarioLogadoQueVoltaAoLoginVaiParaOPainel() throws Exception {
        mvc.perform(get("/login").with(como("gestor@zelo.com.br")))
                .andExpect(redirectedUrl("/clinica/painel"));
    }

    @Test
    void tutorNaoVePetDeOutroTutor() throws Exception {
        mvc.perform(get("/tutor/pets/" + idDoPet("Thor")).with(como("tutor@zelo.com.br")))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Página não encontrada")));
    }

    @Test
    void clinicaNaoVePacienteDeOutraClinica() throws Exception {
        mvc.perform(get("/clinica/pacientes/" + idDoPet("Rex")).with(como("gestor2@zelo.com.br")))
                .andExpect(status().isNotFound());
    }

    @Test
    void enderecoComIdInvalidoDa404() throws Exception {
        mvc.perform(get("/tutor/pets/abc").with(como("tutor@zelo.com.br"))).andExpect(status().isNotFound());
    }

    @Test
    void painelDaClinicaTrazIndicadoresEGraficos() throws Exception {
        mvc.perform(get("/clinica/painel").with(como("veterinario@zelo.com.br")))
                .andExpect(content().string(containsString("Na fila agora")))
                .andExpect(content().string(containsString("Adesão ao plano de cuidado")))
                .andExpect(content().string(containsString("window.ZELO_GRAFICOS")))
                .andExpect(content().string(containsString("data-grafico=\"evolucao\"")))
                .andExpect(content().string(containsString("Pacientes recorrentes")));
    }

    @Test
    void codigoDeConviteAparecePraOGestor() throws Exception {
        mvc.perform(get("/clinica/gerenciar/equipe").with(como("gestor@zelo.com.br")))
                .andExpect(content().string(containsString("AMIGA2026")));
    }

    @Test
    void postSemTokenCsrfEhRecusado() throws Exception {
        mvc.perform(post("/login").param("username", "tutor@zelo.com.br").param("password", SENHA_DEMO))
                .andExpect(status().isForbidden());
        mvc.perform(post("/tutor/pets").with(como("tutor@zelo.com.br")).param("nome", "Sem CSRF"))
                .andExpect(status().isForbidden());
    }

    @Test
    void formulariosIncluemTokenCsrfAutomaticamente() throws Exception {
        mvc.perform(get("/tutor/pets/novo").with(como("tutor@zelo.com.br")))
                .andExpect(content().string(containsString("name=\"_csrf\"")));
        mvc.perform(get("/login").with(csrf()))
                .andExpect(content().string(containsString("name=\"_csrf\"")));
    }
}
