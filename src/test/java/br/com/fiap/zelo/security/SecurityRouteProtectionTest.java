package br.com.fiap.zelo.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.endsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Valida o requisito de Spring Security da Sprint 3 (30 pontos): pelo menos
 * dois perfis de usuario com protecao de rotas conforme o perfil.
 *
 * Este teste verifica apenas o LIMITE de autorizacao (redirecionamento para
 * /login quando anonimo, 403 quando o perfil autenticado nao tem permissao
 * para a rota) - nunca um acesso "de sucesso" que exigisse executar o corpo
 * do controller. Isso e proposital: @WithMockUser injeta um UserDetails
 * generico do Spring Security (nao um ZeloUserPrincipal), e os controllers
 * dependem de ContextoAtualService/ZeloUserPrincipal para localizar o
 * Tutor/Clinica do usuario autenticado - um teste de "sucesso" com
 * @WithMockUser falharia por esse motivo alheio a autorizacao em si. A
 * cobertura de "o perfil correto realmente ve a tela certa" fica com o
 * roteiro de teste manual do README (login com as contas de demonstracao).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityRouteProtectionTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void areaDoTutorRedirecionaParaLoginQuandoNaoAutenticado() throws Exception {
        mockMvc.perform(get("/tutor/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", endsWith("/login")));
    }

    @Test
    void areaDaClinicaRedirecionaParaLoginQuandoNaoAutenticado() throws Exception {
        mockMvc.perform(get("/clinica/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", endsWith("/login")));
    }

    @Test
    void paginasPublicasNaoExigemAutenticacao() throws Exception {
        mockMvc.perform(get("/")).andExpect(status().isOk());
        mockMvc.perform(get("/login")).andExpect(status().isOk());
        mockMvc.perform(get("/registro")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "VETERINARIO")
    void veterinarioNaoAcessaAreaExclusivaDoTutor() throws Exception {
        mockMvc.perform(get("/tutor/dashboard"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "TUTOR")
    void tutorNaoAcessaAreaDaClinica() throws Exception {
        mockMvc.perform(get("/clinica/dashboard"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "VETERINARIO")
    void veterinarioNaoAcessaAdministracaoDaClinicaRestritaAoGestor() throws Exception {
        // /clinica/gerenciar/** exige GESTOR; VETERINARIO tem acesso a /clinica/**
        // de forma geral, mas nao a esta sub-rota - valida a ordem das regras
        // em SecurityConfig (a regra mais especifica precisa vir primeiro).
        mockMvc.perform(get("/clinica/gerenciar"))
                .andExpect(status().isForbidden());
    }

    @Test
    void requisicaoPostSemTokenCsrfEhRejeitada() throws Exception {
        mockMvc.perform(post("/login").param("username", "tutor@zelo.com.br").param("password", "Zelo@123"))
                .andExpect(status().isForbidden());
    }
}
