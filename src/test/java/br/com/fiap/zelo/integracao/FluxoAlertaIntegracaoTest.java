package br.com.fiap.zelo.integracao;

import br.com.fiap.zelo.domain.Alerta;
import br.com.fiap.zelo.domain.enums.StatusAlerta;
import br.com.fiap.zelo.repository.AlertaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDate;
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

/** Fluxo 2 de ponta a ponta: a clínica recomenda, envia o lembrete e o tutor confirma o cuidado. */
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class FluxoAlertaIntegracaoTest extends IntegracaoBase {

    @Autowired
    private AlertaRepository alertaRepository;

    private Alerta ultimoAlerta() {
        return alertaRepository.findAll().stream().max(Comparator.comparing(Alerta::getId)).orElseThrow();
    }

    private StatusAlerta statusDe(Alerta alerta) {
        return alertaRepository.findById(alerta.getId()).orElseThrow().getStatusAlerta();
    }

    @Test
    void cicloCompletoDoPlanoDeCuidado() throws Exception {
        mvc.perform(post("/clinica/alertas").with(como("veterinario@zelo.com.br")).with(csrf())
                        .param("petId", idDoPet("Rex").toString())
                        .param("tipoAlerta", "CHECKUP")
                        .param("titulo", "Check-up cardiológico")
                        .param("mensagem", "Trazer em jejum de 8 horas.")
                        .param("dataPrevista", LocalDate.now().plusDays(15).toString()))
                .andExpect(redirectedUrl("/clinica/alertas"));
        Alerta alerta = ultimoAlerta();
        assertThat(alerta.getTitulo()).isEqualTo("Check-up cardiológico");
        assertThat(alerta.getStatusAlerta()).isEqualTo(StatusAlerta.PENDENTE);

        mvc.perform(post("/clinica/alertas/" + alerta.getId() + "/enviar").with(como("veterinario@zelo.com.br")).with(csrf()))
                .andExpect(flash().attribute("sucesso", "Lembrete enviado ao tutor."));
        assertThat(statusDe(alerta)).isEqualTo(StatusAlerta.ENVIADO);

        mvc.perform(get("/tutor/alertas").with(como("tutor@zelo.com.br")))
                .andExpect(content().string(containsString("Check-up cardiológico")))
                .andExpect(content().string(containsString("Recomendado por Clínica Amiga Bicho")));

        mvc.perform(post("/tutor/alertas/" + alerta.getId() + "/confirmar").with(como("tutor@zelo.com.br")).with(csrf())
                        .param("voltarPara", "/tutor/painel"))
                .andExpect(redirectedUrl("/tutor/painel"))
                .andExpect(flash().attribute("sucesso", "Cuidado confirmado. Continue assim!"));
        assertThat(statusDe(alerta)).isEqualTo(StatusAlerta.CONCLUIDO);

        mvc.perform(get("/tutor/alertas?aba=historico").with(como("tutor@zelo.com.br")))
                .andExpect(content().string(containsString("Check-up cardiológico")));
    }

    @Test
    void tutorCriaLembretePessoal() throws Exception {
        mvc.perform(post("/tutor/alertas").with(como("tutor@zelo.com.br")).with(csrf())
                        .param("petId", idDoPet("Mia").toString())
                        .param("tipoAlerta", "VACINA")
                        .param("titulo", "Vermífugo da Mia")
                        .param("dataPrevista", LocalDate.now().plusDays(3).toString()))
                .andExpect(flash().attribute("sucesso", "Lembrete adicionado ao plano de cuidado."));
        assertThat(ultimoAlerta().getClinica()).isNull();
    }

    @Test
    void clinicaNaoRecomendaCuidadoParaQuemNaoEPaciente() throws Exception {
        mvc.perform(post("/clinica/alertas").with(como("veterinario2@zelo.com.br")).with(csrf())
                        .param("petId", idDoPet("Rex").toString())
                        .param("tipoAlerta", "VACINA")
                        .param("titulo", "Tentativa")
                        .param("dataPrevista", LocalDate.now().plusDays(3).toString()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Este pet não é paciente da sua clínica.")));
    }

    @Test
    void dataNoPassadoEhRecusada() throws Exception {
        mvc.perform(post("/tutor/alertas").with(como("tutor@zelo.com.br")).with(csrf())
                        .param("petId", idDoPet("Rex").toString())
                        .param("tipoAlerta", "VACINA")
                        .param("titulo", "Atrasado")
                        .param("dataPrevista", LocalDate.now().minusDays(1).toString()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("A data prevista não pode estar no passado.")));
    }

    @Test
    void naoExcluiPetComHistorico() throws Exception {
        mvc.perform(post("/tutor/pets/" + idDoPet("Rex") + "/excluir").with(como("tutor@zelo.com.br")).with(csrf()))
                .andExpect(flash().attribute("erro", containsString("já possui histórico")));
    }

    @Test
    void tutorCadastraEditaEExcluiPetSemHistorico() throws Exception {
        mvc.perform(post("/tutor/pets").with(como("tutor@zelo.com.br")).with(csrf())
                        .param("nome", "Paçoca").param("especie", "CAO").param("pesoKg", "8.5"))
                .andExpect(status().is3xxRedirection());
        Long id = idDoPet("Paçoca");

        mvc.perform(post("/tutor/pets/" + id).with(como("tutor@zelo.com.br")).with(csrf())
                        .param("nome", "Paçoca").param("especie", "CAO").param("raca", "Beagle"))
                .andExpect(redirectedUrl("/tutor/pets/" + id));
        assertThat(petRepository.findById(id).orElseThrow().getRaca()).isEqualTo("Beagle");

        mvc.perform(post("/tutor/pets/" + id + "/excluir").with(como("tutor@zelo.com.br")).with(csrf()))
                .andExpect(flash().attribute("sucesso", "Pet removido."));
        assertThat(petRepository.findById(id)).isEmpty();
    }
}
