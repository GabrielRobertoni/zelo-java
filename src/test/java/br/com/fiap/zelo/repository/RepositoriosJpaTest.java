package br.com.fiap.zelo.repository;

import br.com.fiap.zelo.domain.Alerta;
import br.com.fiap.zelo.domain.Clinica;
import br.com.fiap.zelo.domain.Pet;
import br.com.fiap.zelo.domain.Triagem;
import br.com.fiap.zelo.domain.Tutor;
import br.com.fiap.zelo.domain.Usuario;
import br.com.fiap.zelo.domain.enums.StatusAlerta;
import br.com.fiap.zelo.domain.enums.StatusTriagem;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes de integracao (@DataJpaTest) que validam, contra um H2 real em modo
 * de compatibilidade Oracle, as consultas JPQL customizadas dos repositorios
 * - em particular as que usam "join fetch" (adicionadas para evitar N+1) e
 * as que navegam por associacoes (ex.: Pet -> PetTutor -> Tutor).
 *
 * @AutoConfigureTestDatabase(Replace.NONE) preserva o datasource H2 +
 * Flyway configurados em application.yml (perfil "test") em vez do banco
 * embutido generico que o Spring Boot usaria por padrao em @DataJpaTest -
 * assim as migrations V1/V2 (schema + massa de dados) realmente rodam.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class RepositoriosJpaTest {

    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private TutorRepository tutorRepository;
    @Autowired
    private ClinicaRepository clinicaRepository;
    @Autowired
    private PetRepository petRepository;
    @Autowired
    private TriagemRepository triagemRepository;
    @Autowired
    private AlertaRepository alertaRepository;

    @Test
    void massaDeDadosDoFlywayFoiCarregadaCorretamente() {
        assertThat(usuarioRepository.findByEmailIgnoreCase("tutor@zelo.com.br")).isPresent();
        assertThat(usuarioRepository.existsByEmailIgnoreCase("GESTOR@ZELO.COM.BR")).isTrue();
        assertThat(clinicaRepository.findByAtivaOrderByNomeAsc("S")).hasSize(1);
        assertThat(petRepository.findAll()).hasSize(2);
    }

    @Test
    void findByMembroEquipeIdRetornaClinicaDoGestorEDoVeterinario() {
        Usuario gestor = usuarioRepository.findByEmailIgnoreCase("gestor@zelo.com.br").orElseThrow();
        Usuario veterinario = usuarioRepository.findByEmailIgnoreCase("veterinario@zelo.com.br").orElseThrow();

        Optional<Clinica> clinicaDoGestor = clinicaRepository.findByMembroEquipeId(gestor.getId());
        Optional<Clinica> clinicaDoVeterinario = clinicaRepository.findByMembroEquipeId(veterinario.getId());

        assertThat(clinicaDoGestor).isPresent();
        assertThat(clinicaDoVeterinario).isPresent();
        assertThat(clinicaDoGestor.get().getId()).isEqualTo(clinicaDoVeterinario.get().getId());
    }

    @Test
    void findByTutorIdRetornaOsPetsVinculadosOrdenadosPorNome() {
        Tutor tutor = tutorRepository.findByUsuarioId(
                usuarioRepository.findByEmailIgnoreCase("tutor@zelo.com.br").orElseThrow().getId()
        ).orElseThrow();

        List<Pet> pets = petRepository.findByTutorId(tutor.getId());

        assertThat(pets).extracting(Pet::getNome).containsExactly("Mia", "Rex");
    }

    @Test
    void existeVinculoComTutorDistingueTutoresDiferentes() {
        Tutor tutor = tutorRepository.findByUsuarioId(
                usuarioRepository.findByEmailIgnoreCase("tutor@zelo.com.br").orElseThrow().getId()
        ).orElseThrow();
        Pet rex = petRepository.findByTutorId(tutor.getId()).stream()
                .filter(p -> p.getNome().equals("Rex")).findFirst().orElseThrow();

        assertThat(petRepository.existeVinculoComTutor(rex.getId(), tutor.getId())).isTrue();
        assertThat(petRepository.existeVinculoComTutor(rex.getId(), 999_999L)).isFalse();
    }

    @Test
    void findFilaDeAtencaoTrazTriagensComPetJaCarregado() {
        Clinica clinica = clinicaRepository.findByAtivaOrderByNomeAsc("S").get(0);
        Tutor tutor = tutorRepository.findByUsuarioId(
                usuarioRepository.findByEmailIgnoreCase("tutor@zelo.com.br").orElseThrow().getId()
        ).orElseThrow();
        Pet rex = petRepository.findByTutorId(tutor.getId()).stream()
                .filter(p -> p.getNome().equals("Rex")).findFirst().orElseThrow();

        Triagem triagem = Triagem.builder()
                .pet(rex)
                .tutor(tutor)
                .clinica(clinica)
                .canal(br.com.fiap.zelo.domain.enums.CanalTriagem.TEXTO)
                .relato("Relato de teste de integracao")
                .scoreRisco(new java.math.BigDecimal("40.00"))
                .nivelUrgencia(br.com.fiap.zelo.domain.enums.NivelUrgencia.MEDIA)
                .statusTriagem(StatusTriagem.ABERTA)
                .build();
        triagemRepository.saveAndFlush(triagem);

        List<Triagem> fila = triagemRepository.findFilaDeAtencao(clinica.getId(), List.of(StatusTriagem.ABERTA));

        assertThat(fila).isNotEmpty();
        // getNome() acessa a associacao "pet" fora de qualquer sessao aberta manualmente
        // pelo teste: so funciona sem LazyInitializationException porque o "join fetch"
        // da query ja trouxe o Pet junto (ver TriagemRepository.findFilaDeAtencao).
        assertThat(fila.get(0).getPet().getNome()).isEqualTo("Rex");
    }

    @Test
    void findPendentesPorTutorTrazAlertasComPetJaCarregado() {
        Tutor tutor = tutorRepository.findByUsuarioId(
                usuarioRepository.findByEmailIgnoreCase("tutor@zelo.com.br").orElseThrow().getId()
        ).orElseThrow();
        Pet mia = petRepository.findByTutorId(tutor.getId()).stream()
                .filter(p -> p.getNome().equals("Mia")).findFirst().orElseThrow();

        Alerta alerta = Alerta.builder()
                .pet(mia)
                .tipoAlerta(br.com.fiap.zelo.domain.enums.TipoAlerta.VACINA)
                .titulo("Vacina de teste")
                .dataPrevista(java.time.LocalDate.now().plusDays(5))
                .statusAlerta(StatusAlerta.PENDENTE)
                .build();
        alertaRepository.saveAndFlush(alerta);

        List<Alerta> pendentes = alertaRepository.findPendentesPorTutor(tutor.getId(), List.of(StatusAlerta.PENDENTE));

        assertThat(pendentes).isNotEmpty();
        assertThat(pendentes.get(0).getPet().getNome()).isEqualTo("Mia");
    }
}
