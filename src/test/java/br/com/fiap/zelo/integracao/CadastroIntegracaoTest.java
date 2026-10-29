package br.com.fiap.zelo.integracao;

import br.com.fiap.zelo.domain.Clinica;
import br.com.fiap.zelo.domain.Usuario;
import br.com.fiap.zelo.domain.enums.TipoUsuario;
import br.com.fiap.zelo.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Cadastro público dos três perfis: todo usuário criado consegue entrar e usar o próprio painel. */
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class CadastroIntegracaoTest extends IntegracaoBase {

    @Autowired
    private UsuarioRepository usuarioRepository;

    private MockHttpServletRequestBuilder cadastro(String perfil, String nome, String email) {
        return post("/registro/" + perfil).with(csrf())
                .param("nome", nome)
                .param("email", email)
                .param("senha", "segredo123")
                .param("confirmarSenha", "segredo123");
    }

    private void entraEVaiPara(String email, String perfil, String painel) throws Exception {
        mvc.perform(formLogin("/login").user(email).password("segredo123"))
                .andExpect(authenticated().withRoles(perfil))
                .andExpect(redirectedUrl(painel));
        mvc.perform(get(painel).with(como(email))).andExpect(status().isOk());
    }

    @Test
    void tutorSeCadastraEntraEVePainelVazio() throws Exception {
        mvc.perform(cadastro("tutor", "Nova Tutora", "nova.tutora@exemplo.com").param("estado", "rj"))
                .andExpect(redirectedUrl("/login"))
                .andExpect(flash().attribute("emailCadastrado", "nova.tutora@exemplo.com"));

        entraEVaiPara("nova.tutora@exemplo.com", "TUTOR", "/tutor/painel");
        mvc.perform(get("/tutor/painel").with(como("nova.tutora@exemplo.com")))
                .andExpect(content().string(containsString("Você ainda não cadastrou nenhum pet.")));
    }

    @Test
    void gestorSeCadastraComAClinicaEEntraNoPainelDela() throws Exception {
        mvc.perform(cadastro("gestor", "Gestor Novo", "gestor.novo@exemplo.com")
                        .param("clinica.nome", "Clínica Patas Felizes")
                        .param("clinica.cnpj", "11222333000144")
                        .param("clinica.cidade", "Campinas")
                        .param("clinica.estado", "sp"))
                .andExpect(redirectedUrl("/login"));

        Usuario gestor = usuarioRepository.findByEmailIgnoreCase("gestor.novo@exemplo.com").orElseThrow();
        Clinica clinica = clinicaRepository.findByMembroEquipeId(gestor.getId()).orElseThrow();
        assertThat(gestor.getTipoUsuario()).isEqualTo(TipoUsuario.GESTOR);
        assertThat(clinica.getNome()).isEqualTo("Clínica Patas Felizes");
        assertThat(clinica.getEstado()).isEqualTo("SP");
        assertThat(clinica.isAtiva()).isTrue();
        assertThat(clinica.getCodigoConvite()).hasSize(8);

        entraEVaiPara("gestor.novo@exemplo.com", "GESTOR", "/clinica/painel");
        mvc.perform(get("/clinica/gerenciar/equipe").with(como("gestor.novo@exemplo.com")))
                .andExpect(content().string(containsString(clinica.getCodigoConvite())));
    }

    @Test
    void veterinarioSeCadastraComCodigoDeConviteEEntraNaClinica() throws Exception {
        mvc.perform(cadastro("veterinario", "Vet Nova", "vet.nova@exemplo.com").param("codigoConvite", " amiga2026 "))
                .andExpect(redirectedUrl("/login"));

        Usuario vet = usuarioRepository.findByEmailIgnoreCase("vet.nova@exemplo.com").orElseThrow();
        assertThat(clinicaRepository.findByMembroEquipeId(vet.getId()).orElseThrow().getCnpj()).isEqualTo(CNPJ_AMIGA);

        entraEVaiPara("vet.nova@exemplo.com", "VETERINARIO", "/clinica/painel");
        mvc.perform(get("/clinica/painel").with(como("vet.nova@exemplo.com")))
                .andExpect(content().string(containsString("Clínica Amiga Bicho")));
    }

    @Test
    void codigoDeConviteInvalidoMostraErroNoFormulario() throws Exception {
        mvc.perform(cadastro("veterinario", "Vet", "vet.erro@exemplo.com").param("codigoConvite", "NAOEXISTE"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Código de convite inválido")));
        assertThat(usuarioRepository.existsByEmailIgnoreCase("vet.erro@exemplo.com")).isFalse();
    }

    @Test
    void emailJaCadastradoMostraErroNoFormulario() throws Exception {
        mvc.perform(cadastro("tutor", "Outra", "TUTOR@zelo.com.br"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Já existe uma conta cadastrada com este e-mail.")));
    }

    @Test
    void cnpjJaCadastradoMostraErroNoFormulario() throws Exception {
        mvc.perform(cadastro("gestor", "Gestor", "gestor.cnpj@exemplo.com")
                        .param("clinica.nome", "Cópia").param("clinica.cnpj", CNPJ_AMIGA))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Já existe uma clínica cadastrada com este CNPJ.")));
        assertThat(usuarioRepository.existsByEmailIgnoreCase("gestor.cnpj@exemplo.com")).isFalse();
    }

    @Test
    void dadosInvalidosVoltamParaOFormularioComAsMensagens() throws Exception {
        mvc.perform(post("/registro/tutor").with(csrf())
                        .param("nome", "")
                        .param("email", "invalido")
                        .param("senha", "segredo123")
                        .param("confirmarSenha", "diferente")
                        .param("estado", "SAO"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Informe o nome completo.")))
                .andExpect(content().string(containsString("Informe um e-mail válido.")))
                .andExpect(content().string(containsString("As senhas informadas não conferem.")))
                .andExpect(content().string(containsString("Informe a UF com 2 letras")))
                .andExpect(content().string(containsString("value=\"invalido\"")));
    }
}
