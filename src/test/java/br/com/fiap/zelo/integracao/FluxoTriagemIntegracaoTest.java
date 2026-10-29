package br.com.fiap.zelo.integracao;

import br.com.fiap.zelo.domain.Triagem;
import br.com.fiap.zelo.domain.enums.NivelUrgencia;
import br.com.fiap.zelo.domain.enums.StatusTriagem;
import br.com.fiap.zelo.repository.TriagemRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Comparator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Fluxo 1 de ponta a ponta: o tutor relata, a clínica encaminha e conclui, o tutor acompanha. */
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class FluxoTriagemIntegracaoTest extends IntegracaoBase {

    private static final String TUTORA = "tutor@zelo.com.br";
    private static final String VETERINARIO = "veterinario@zelo.com.br";

    @Autowired
    private TriagemRepository triagemRepository;

    private Triagem abrirTriagem(String pet, String relato) throws Exception {
        mvc.perform(post("/tutor/triagens").with(como(TUTORA)).with(csrf())
                        .param("petId", idDoPet(pet).toString())
                        .param("clinicaId", idDaClinica(CNPJ_AMIGA).toString())
                        .param("canal", "TEXTO")
                        .param("relato", relato))
                .andExpect(redirectedUrl("/tutor/triagens"))
                .andExpect(flash().attribute("sucesso", containsString("Urgência classificada como")));
        return triagemRepository.findAll().stream().max(Comparator.comparing(Triagem::getId)).orElseThrow();
    }

    private StatusTriagem statusDe(Triagem triagem) {
        return triagemRepository.findById(triagem.getId()).orElseThrow().getStatusTriagem();
    }

    @Test
    void fluxoCompletoDaTriagemAteOAtendimento() throws Exception {
        Triagem triagem = abrirTriagem("Mia", "A Mia teve uma convulsão agora há pouco e está muito fraca.");
        assertThat(triagem.getNivelUrgencia()).isEqualTo(NivelUrgencia.EMERGENCIA);
        assertThat(triagem.getStatusTriagem()).isEqualTo(StatusTriagem.ABERTA);

        mvc.perform(get("/clinica/triagens").with(como(VETERINARIO)))
                .andExpect(content().string(containsString("A Mia teve uma convulsão")));

        mvc.perform(post("/clinica/triagens/" + triagem.getId() + "/encaminhar").with(como(VETERINARIO)).with(csrf())
                        .param("voltarPara", "/clinica/painel"))
                .andExpect(redirectedUrl("/clinica/painel"))
                .andExpect(flash().attribute("sucesso", "Triagem encaminhada para atendimento."));
        assertThat(statusDe(triagem)).isEqualTo(StatusTriagem.ENCAMINHADA);

        mvc.perform(post("/clinica/triagens/" + triagem.getId() + "/concluir").with(como(VETERINARIO)).with(csrf()))
                .andExpect(redirectedUrl("/clinica/triagens"));
        assertThat(statusDe(triagem)).isEqualTo(StatusTriagem.ATENDIDA);

        mvc.perform(get("/clinica/triagens?aba=historico").with(como(VETERINARIO)))
                .andExpect(content().string(containsString("A Mia teve uma convulsão")));
        mvc.perform(get("/tutor/pets/" + idDoPet("Mia")).with(como(TUTORA)))
                .andExpect(content().string(containsString("A Mia teve uma convulsão")));
    }

    @Test
    void tutorCancelaTriagemAindaAberta() throws Exception {
        Triagem triagem = abrirTriagem("Rex", "Rex está com as unhas grandes, queria agendar.");
        assertThat(triagem.getNivelUrgencia()).isEqualTo(NivelUrgencia.BAIXA);

        mvc.perform(post("/tutor/triagens/" + triagem.getId() + "/cancelar").with(como(TUTORA)).with(csrf()))
                .andExpect(flash().attribute("sucesso", "Triagem cancelada."));
        assertThat(statusDe(triagem)).isEqualTo(StatusTriagem.CANCELADA);
    }

    @Test
    void outroTutorEOutraClinicaNaoMexemNaTriagem() throws Exception {
        Triagem triagem = abrirTriagem("Rex", "Rex está mancando da pata traseira desde ontem.");

        mvc.perform(post("/tutor/triagens/" + triagem.getId() + "/cancelar").with(como("tutor3@zelo.com.br")).with(csrf()))
                .andExpect(flash().attribute("erro", "Esta triagem não pertence ao tutor autenticado."));
        mvc.perform(post("/clinica/triagens/" + triagem.getId() + "/encaminhar").with(como("veterinario2@zelo.com.br")).with(csrf()))
                .andExpect(flash().attribute("erro", "Esta triagem não pertence à sua clínica."));
        assertThat(statusDe(triagem)).isEqualTo(StatusTriagem.ABERTA);
    }

    @Test
    void transicaoInvalidaMostraMensagem() throws Exception {
        Triagem triagem = abrirTriagem("Rex", "Rex está com tosse seca há alguns dias.");
        mvc.perform(post("/clinica/triagens/" + triagem.getId() + "/concluir").with(como(VETERINARIO)).with(csrf()));

        mvc.perform(post("/clinica/triagens/" + triagem.getId() + "/cancelar").with(como(VETERINARIO)).with(csrf()))
                .andExpect(flash().attribute("erro", "Não é possível cancelar uma triagem já finalizada."));
    }

    @Test
    void relatoCurtoVoltaAoFormularioComErro() throws Exception {
        mvc.perform(post("/tutor/triagens").with(como(TUTORA)).with(csrf())
                        .param("petId", idDoPet("Rex").toString())
                        .param("clinicaId", idDaClinica(CNPJ_AMIGA).toString())
                        .param("canal", "TEXTO")
                        .param("relato", "curto"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("O relato deve ter entre 10 e 2000 caracteres.")));
    }

    @Test
    void tutorNaoAbreTriagemParaPetDeOutraPessoa() throws Exception {
        mvc.perform(post("/tutor/triagens").with(como(TUTORA)).with(csrf())
                        .param("petId", idDoPet("Thor").toString())
                        .param("clinicaId", idDaClinica(CNPJ_AMIGA).toString())
                        .param("canal", "TEXTO")
                        .param("relato", "Tentando abrir triagem de outro pet."))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Pet não encontrado entre os pets do tutor autenticado.")));
    }
}
