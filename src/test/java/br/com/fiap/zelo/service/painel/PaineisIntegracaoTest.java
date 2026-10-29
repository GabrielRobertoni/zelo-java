package br.com.fiap.zelo.service.painel;

import br.com.fiap.zelo.domain.Clinica;
import br.com.fiap.zelo.repository.ClinicaRepository;
import br.com.fiap.zelo.repository.TutorRepository;
import br.com.fiap.zelo.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/** Os números dos painéis batem com a massa de demonstração e cada clínica só vê os próprios dados. */
@SpringBootTest
@ActiveProfiles("test")
class PaineisIntegracaoTest {

    @Autowired
    private PainelClinicaService painelClinicaService;
    @Autowired
    private PainelTutorService painelTutorService;
    @Autowired
    private ClinicaRepository clinicaRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private TutorRepository tutorRepository;

    private Clinica clinica(String cnpj) {
        return clinicaRepository.findAll().stream().filter(c -> cnpj.equals(c.getCnpj())).findFirst().orElseThrow();
    }

    @Test
    void painelDaClinicaConsolidaOPeriodo() {
        PainelClinica painel = painelClinicaService.montar(clinica("12345678000190"), Periodo.TRINTA_DIAS);

        assertThat(painel.totalNaFila()).isEqualTo(6);
        assertThat(painel.filaPrioritaria()).hasSize(5);
        assertThat(painel.filaPrioritaria().get(0).getNivelUrgencia().name()).isEqualTo("EMERGENCIA");
        assertThat(painel.indicadores()).hasSize(6);
        assertThat(painel.indicadores().get(1).valor()).isEqualTo("22");
        assertThat(painel.graficos()).containsOnlyKeys("urgencia", "evolucao", "status", "alertas");
        assertThat(painel.graficos().get("urgencia").total()).isEqualTo(22);
        assertThat(painel.graficos().get("status").total()).isEqualTo(22);
        assertThat(painel.graficos().get("evolucao").rotulos()).hasSize(30);
        assertThat(painel.graficos().get("evolucao").series().get(0).valores().stream().mapToLong(Long::longValue).sum())
                .isEqualTo(22);
        assertThat(painel.alertasAtrasados()).isNotEmpty().allSatisfy(alerta -> assertThat(alerta.isAtrasado()).isTrue());
        assertThat(painel.tamanhoEquipe()).isEqualTo(3);
    }

    @Test
    void periodoCurtoReduzOsNumeros() {
        PainelClinica semana = painelClinicaService.montar(clinica("12345678000190"), Periodo.SETE_DIAS);

        assertThat(semana.graficos().get("evolucao").rotulos()).hasSize(7);
        assertThat(semana.graficos().get("urgencia").total()).isLessThan(22);
    }

    @Test
    void cadaClinicaVeApenasOsProprioDados() {
        PainelClinica paulista = painelClinicaService.montar(clinica("98765432000110"), Periodo.NOVENTA_DIAS);

        assertThat(paulista.totalNaFila()).isEqualTo(2);
        assertThat(paulista.graficos().get("urgencia").total()).isEqualTo(4);
        assertThat(paulista.pacientesRecorrentes()).isEmpty();
    }

    @Test
    void painelDoTutorMostraASituacaoDeCadaPet() {
        Long usuario = usuarioRepository.findByEmailIgnoreCase("tutor@zelo.com.br").orElseThrow().getId();
        PainelTutor painel = painelTutorService.montar(tutorRepository.findByUsuarioId(usuario).orElseThrow().getId());

        assertThat(painel.pets()).extracting(situacao -> situacao.pet().getNome()).containsExactly("Mia", "Rex");
        assertThat(painel.pets()).allSatisfy(situacao -> assertThat(situacao.situacao()).isEqualTo(PainelTutor.Situacao.ATRASADO));
        assertThat(painel.indicadores()).hasSize(4);
        assertThat(painel.graficos()).containsOnlyKeys("urgencia", "cuidados");
        assertThat(painel.proximosCuidados()).isSortedAccordingTo(
                java.util.Comparator.comparing(br.com.fiap.zelo.domain.Alerta::getDataPrevista));
    }
}
