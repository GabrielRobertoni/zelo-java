package br.com.fiap.zelo.integracao;

import org.junit.jupiter.api.Test;
import org.springframework.test.annotation.DirtiesContext;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;

/** O gestor pausa a clínica: ela some da lista do tutor e não aceita novas triagens. */
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class ClinicaIndisponivelIntegracaoTest extends IntegracaoBase {

    @Test
    void clinicaPausadaNaoRecebeTriagens() throws Exception {
        mvc.perform(post("/clinica/gerenciar/alternar").with(como("gestor2@zelo.com.br")).with(csrf()))
                .andExpect(flash().attribute("sucesso", "Clínica marcada como indisponível para novas triagens."));

        mvc.perform(get("/tutor/triagens/nova").with(como("tutor@zelo.com.br")))
                .andExpect(content().string(not(containsString("Vet Center Paulista"))));

        mvc.perform(post("/tutor/triagens").with(como("tutor@zelo.com.br")).with(csrf())
                        .param("petId", idDoPet("Rex").toString())
                        .param("clinicaId", idDaClinica(CNPJ_PAULISTA).toString())
                        .param("canal", "TEXTO")
                        .param("relato", "Rex está com tosse há dois dias."))
                .andExpect(content().string(containsString("não está recebendo triagens")));

        mvc.perform(post("/clinica/gerenciar/alternar").with(como("gestor2@zelo.com.br")).with(csrf()))
                .andExpect(flash().attribute("sucesso", "Clínica reativada: ela volta a receber triagens."));
    }

    @Test
    void gestorAtualizaDadosDaClinicaComValidacao() throws Exception {
        mvc.perform(post("/clinica/gerenciar").with(como("gestor2@zelo.com.br")).with(csrf())
                        .param("nome", "Vet Center Paulista 24h").param("cnpj", "98765432000110")
                        .param("estado", "XYZ"))
                .andExpect(content().string(containsString("Informe a UF com 2 letras")));

        mvc.perform(post("/clinica/gerenciar").with(como("gestor2@zelo.com.br")).with(csrf())
                        .param("nome", "Vet Center Paulista 24h").param("cnpj", CNPJ_AMIGA))
                .andExpect(content().string(containsString("Já existe uma clínica cadastrada com este CNPJ.")));

        mvc.perform(post("/clinica/gerenciar").with(como("gestor2@zelo.com.br")).with(csrf())
                        .param("nome", "Vet Center Paulista 24h").param("cnpj", "98765432000110").param("estado", "sp"))
                .andExpect(flash().attribute("sucesso", "Dados da clínica atualizados."));
        mvc.perform(get("/clinica/painel").with(como("gestor2@zelo.com.br")))
                .andExpect(content().string(containsString("Vet Center Paulista 24h")));
    }
}
