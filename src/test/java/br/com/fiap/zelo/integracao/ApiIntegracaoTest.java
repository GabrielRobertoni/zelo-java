package br.com.fiap.zelo.integracao;

import br.com.fiap.zelo.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.LocalDate;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** API REST do aplicativo mobile: autenticação JWT, permissões e os mesmos fluxos da web. */
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class ApiIntegracaoTest extends IntegracaoBase {

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UsuarioRepository usuarioRepository;

    private String token(String email, String senha) throws Exception {
        String resposta = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"senha\":\"" + senha + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(resposta).get("token").asText();
    }

    private MockHttpServletRequestBuilder autenticado(MockHttpServletRequestBuilder requisicao, String token) {
        return requisicao.header(HttpHeaders.AUTHORIZATION, "Bearer " + token).contentType(MediaType.APPLICATION_JSON);
    }

    private JsonNode json(MockHttpServletRequestBuilder requisicao, int statusEsperado) throws Exception {
        String corpo = mvc.perform(requisicao).andExpect(status().is(statusEsperado))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(corpo);
    }

    @Test
    void loginDevolveTokenEDadosDoUsuario() throws Exception {
        String token = token("tutor@zelo.com.br", SENHA_DEMO);

        mvc.perform(autenticado(get("/api/auth/me"), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("tutor@zelo.com.br"))
                .andExpect(jsonPath("$.tipo").value("TUTOR"));
    }

    @Test
    void senhaErradaDevolve401EmJson() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"tutor@zelo.com.br\",\"senha\":\"errada\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void semTokenOuComTokenInvalidoDevolve401SemRedirecionar() throws Exception {
        mvc.perform(get("/api/pets"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Autenticação necessária. Faça login novamente."));
        mvc.perform(get("/api/pets").header(HttpHeaders.AUTHORIZATION, "Bearer token.invalido.123"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void perfilDaClinicaNaoAcessaRecursosDoTutor() throws Exception {
        String token = token("veterinario@zelo.com.br", SENHA_DEMO);

        mvc.perform(autenticado(get("/api/pets"), token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
        mvc.perform(autenticado(get("/api/clinicas"), token)).andExpect(status().isOk());
    }

    @Test
    void tutorGerenciaPetsPelaApi() throws Exception {
        String token = token("tutor@zelo.com.br", SENHA_DEMO);

        JsonNode criado = json(autenticado(post("/api/pets"), token)
                .content("{\"nome\":\"Biscoito\",\"especie\":\"GATO\",\"castrado\":true}"), 201);
        long id = criado.get("id").asLong();

        mvc.perform(autenticado(put("/api/pets/" + id), token)
                        .content("{\"nome\":\"Biscoito\",\"especie\":\"GATO\",\"raca\":\"SRD\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.raca").value("SRD"));
        mvc.perform(autenticado(get("/api/pets"), token))
                .andExpect(jsonPath("$[*].nome", hasItem("Biscoito")));
        mvc.perform(autenticado(delete("/api/pets/" + id), token)).andExpect(status().isNoContent());
        mvc.perform(autenticado(get("/api/pets/" + id), token)).andExpect(status().isNotFound());
    }

    @Test
    void validacaoDevolve400ComOsCampos() throws Exception {
        String token = token("tutor@zelo.com.br", SENHA_DEMO);

        mvc.perform(autenticado(post("/api/pets"), token).content("{\"pesoKg\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.nome").value("Informe o nome do pet."))
                .andExpect(jsonPath("$.errors.pesoKg").value("O peso deve ser maior que zero."));
        mvc.perform(autenticado(post("/api/triagens"), token)
                        .content("{\"petId\":" + idDoPet("Rex") + ",\"relato\":\"curto\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.relato").exists());
    }

    @Test
    void tutorAbreECancelaTriagemPelaApi() throws Exception {
        String token = token("tutor@zelo.com.br", SENHA_DEMO);

        JsonNode triagem = json(autenticado(post("/api/triagens"), token)
                .content("{\"petId\":" + idDoPet("Rex") + ",\"relato\":\"O Rex está vomitando desde cedo.\"}"), 201);
        mvc.perform(autenticado(patch("/api/triagens/" + triagem.get("id").asLong() + "/cancelar"), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADA"))
                .andExpect(jsonPath("$.urgencia").value("ALTA"));
        mvc.perform(autenticado(patch("/api/triagens/" + triagem.get("id").asLong() + "/cancelar"), token))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void tutorCriaEConfirmaCuidadoPelaApi() throws Exception {
        String token = token("tutor@zelo.com.br", SENHA_DEMO);
        String corpo = "{\"petId\":" + idDoPet("Mia") + ",\"tipo\":\"VACINA\",\"titulo\":\"V4 pela API\","
                + "\"dataPrevista\":\"" + LocalDate.now().plusDays(2) + "\"}";

        JsonNode alerta = json(autenticado(post("/api/alertas"), token).content(corpo), 201);
        mvc.perform(autenticado(patch("/api/alertas/" + alerta.get("id").asLong() + "/confirmar"), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONCLUIDO"))
                .andExpect(jsonPath("$.petNome").value("Mia"));
        mvc.perform(autenticado(get("/api/alertas"), token))
                .andExpect(jsonPath("$[*].titulo", not(hasItem("V4 pela API"))));
    }

    @Test
    void cadastroPelaApiJaDevolveToken() throws Exception {
        String corpo = "{\"nome\":\"App Tutor\",\"email\":\"app@exemplo.com\",\"senha\":\"segredo123\","
                + "\"confirmarSenha\":\"segredo123\"}";

        JsonNode resposta = json(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(corpo), 200);
        mvc.perform(autenticado(get("/api/pets"), resposta.get("token").asText())).andExpect(status().isOk());
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("Já existe uma conta cadastrada com este e-mail."));
    }

    @Test
    void senhasDiferentesNoCadastroDevolvem400() throws Exception {
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"X\",\"email\":\"x@exemplo.com\",\"senha\":\"segredo123\",\"confirmarSenha\":\"outra\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.confirmarSenha").value("As senhas informadas não conferem."));
    }

    @Test
    void tokenDeUsuarioDesativadoDeixaDeValer() throws Exception {
        String token = token("tutor3@zelo.com.br", SENHA_DEMO);
        var usuario = usuarioRepository.findByEmailIgnoreCase("tutor3@zelo.com.br").orElseThrow();
        usuario.alternarAtivo();
        usuarioRepository.save(usuario);

        mvc.perform(autenticado(get("/api/pets"), token)).andExpect(status().isUnauthorized());
    }

    @Test
    void preflightCorsDoAplicativoEhPermitido() throws Exception {
        mvc.perform(options("/api/pets")
                        .header(HttpHeaders.ORIGIN, "http://localhost:8081")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "Authorization"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:8081"));
    }
}
