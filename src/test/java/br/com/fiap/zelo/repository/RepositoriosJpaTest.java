package br.com.fiap.zelo.repository;

import br.com.fiap.zelo.domain.Clinica;
import br.com.fiap.zelo.domain.Pet;
import br.com.fiap.zelo.domain.Triagem;
import br.com.fiap.zelo.domain.enums.StatusTriagem;
import br.com.fiap.zelo.repository.projecao.ContagemPorUrgencia;
import br.com.fiap.zelo.repository.projecao.PacienteResumo;
import br.com.fiap.zelo.service.ClassificadorDeUrgencia;
import br.com.fiap.zelo.service.ClassificacaoUrgencia;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Consultas JPQL (incluindo as agregadas dos painéis) contra o H2 criado pelas migrations reais. */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class RepositoriosJpaTest {

    private static final LocalDateTime INICIO_DOS_TEMPOS = LocalDateTime.of(2000, 1, 1, 0, 0);

    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private ClinicaRepository clinicaRepository;
    @Autowired
    private PetRepository petRepository;
    @Autowired
    private TriagemRepository triagemRepository;
    @Autowired
    private AlertaRepository alertaRepository;
    @Autowired
    private TutorRepository tutorRepository;

    private Clinica clinica(String cnpj) {
        return clinicaRepository.findAll().stream().filter(c -> cnpj.equals(c.getCnpj())).findFirst().orElseThrow();
    }

    private Pet pet(String nome) {
        return petRepository.findAll().stream().filter(p -> p.getNome().equals(nome)).findFirst().orElseThrow();
    }

    @Test
    void migrationsCarregamAMassaDeDemonstracao() {
        assertThat(usuarioRepository.count()).isEqualTo(8);
        assertThat(clinicaRepository.count()).isEqualTo(2);
        assertThat(petRepository.count()).isEqualTo(7);
        assertThat(triagemRepository.count()).isEqualTo(26);
        assertThat(clinicaRepository.findByCodigoConviteIgnoreCase("amiga2026")).isPresent();
    }

    @Test
    void cadaTriagemDaMassaTemAUrgenciaQueOClassificadorCalcularia() {
        ClassificadorDeUrgencia classificador = new ClassificadorDeUrgencia();
        for (Triagem triagem : triagemRepository.findAll()) {
            ClassificacaoUrgencia esperado = classificador.classificar(triagem.getRelato());
            assertThat(triagem.getNivelUrgencia()).as(triagem.getRelato()).isEqualTo(esperado.nivelUrgencia());
            assertThat(triagem.getScoreRisco()).as(triagem.getRelato()).isEqualByComparingTo(esperado.scoreRisco());
        }
    }

    @Test
    void equipeDaClinicaEhResolvidaPeloUsuario() {
        Long gestorId = usuarioRepository.findByEmailIgnoreCase("gestor@zelo.com.br").orElseThrow().getId();
        assertThat(clinicaRepository.findByMembroEquipeId(gestorId).orElseThrow().getCnpj()).isEqualTo("12345678000190");
        assertThat(clinicaRepository.findComEquipe(clinica("12345678000190").getId()).orElseThrow().getEquipe()).hasSize(3);
    }

    @Test
    void filaTrazTutorEUsuarioJaCarregadosSemConsultasExtras() {
        List<Triagem> fila = triagemRepository.findDaClinica(clinica("12345678000190").getId(),
                EnumSet.of(StatusTriagem.ABERTA, StatusTriagem.ENCAMINHADA), Sort.unsorted());

        assertThat(fila).hasSize(6);
        assertThat(fila).allSatisfy(triagem -> {
            assertThat(Hibernate.isInitialized(triagem.getPet())).isTrue();
            assertThat(Hibernate.isInitialized(triagem.getTutor().getUsuario())).isTrue();
        });
    }

    @Test
    void contagemPorUrgenciaSomaOTotalDeTriagensDaClinica() {
        Long amiga = clinica("12345678000190").getId();
        long total = triagemRepository.contarPorUrgenciaNaClinica(amiga, INICIO_DOS_TEMPOS).stream()
                .mapToLong(ContagemPorUrgencia::total).sum();

        assertThat(total).isEqualTo(22);
        assertThat(triagemRepository.listarMomentosNaClinica(amiga, INICIO_DOS_TEMPOS)).hasSize(22);
    }

    @Test
    void pacientesSaoApenasOsPetsQueAbriramTriagemNaClinica() {
        Long paulista = clinica("98765432000110").getId();

        assertThat(petRepository.findPacientesDaClinica(paulista)).extracting(Pet::getNome)
                .containsExactly("Bolt", "Luna", "Nina", "Thor");
        assertThat(triagemRepository.existsByPetIdAndClinicaId(pet("Rex").getId(), paulista)).isFalse();
        assertThat(triagemRepository.listarPacientesDaClinica(paulista)).extracting(PacienteResumo::totalTriagens)
                .containsOnly(1L);
    }

    @Test
    void recorrentesTemMaisDeUmaTriagemEOrdemDecrescente() {
        List<PacienteResumo> recorrentes = triagemRepository.listarPacientesRecorrentes(
                clinica("12345678000190").getId(), INICIO_DOS_TEMPOS, PageRequest.of(0, 5));

        assertThat(recorrentes).hasSize(5);
        assertThat(recorrentes.get(0).petNome()).isEqualTo("Rex");
        assertThat(recorrentes).allSatisfy(paciente -> assertThat(paciente.totalTriagens()).isGreaterThan(1));
    }

    @Test
    void alertasDoTutorIncluemLembretesPessoaisERecomendacoes() {
        Long carla = tutorRepository.findByUsuarioId(
                usuarioRepository.findByEmailIgnoreCase("tutor@zelo.com.br").orElseThrow().getId()).orElseThrow().getId();

        assertThat(alertaRepository.findDoTutor(carla, EnumSet.allOf(br.com.fiap.zelo.domain.enums.StatusAlerta.class),
                Sort.by("dataPrevista")))
                .extracting(alerta -> alerta.getPet().getNome())
                .containsOnly("Rex", "Mia");
        assertThat(alertaRepository.existsByPetId(pet("Rex").getId())).isTrue();
    }
}
