package br.com.fiap.zelo.service;

import br.com.fiap.zelo.domain.Clinica;
import br.com.fiap.zelo.domain.Pet;
import br.com.fiap.zelo.domain.Triagem;
import br.com.fiap.zelo.domain.Tutor;
import br.com.fiap.zelo.domain.enums.CanalTriagem;
import br.com.fiap.zelo.domain.enums.NivelUrgencia;
import br.com.fiap.zelo.domain.enums.StatusTriagem;
import br.com.fiap.zelo.exception.RecursoNaoEncontradoException;
import br.com.fiap.zelo.exception.RegraNegocioException;
import br.com.fiap.zelo.repository.ClinicaRepository;
import br.com.fiap.zelo.repository.PetRepository;
import br.com.fiap.zelo.repository.TriagemRepository;
import br.com.fiap.zelo.repository.TutorRepository;
import br.com.fiap.zelo.web.dto.TriagemForm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Cobre o fluxo completo nº 1 exigido pela Sprint 3 (Triagem e
 * Encaminhamento): abertura com classificacao automatica, fila de atencao
 * ordenada por urgencia e as transicoes de status permitidas/negadas.
 *
 * Os repositorios sao mockados (Mockito) para isolar a regra de negocio do
 * Hibernate/banco - por isso esta suite roda em milissegundos e nao depende
 * de nenhuma infraestrutura externa.
 */
@ExtendWith(MockitoExtension.class)
class TriagemServiceTest {

    @Mock
    private TriagemRepository triagemRepository;
    @Mock
    private PetRepository petRepository;
    @Mock
    private TutorRepository tutorRepository;
    @Mock
    private ClinicaRepository clinicaRepository;

    private TriagemService triagemService;

    private Pet pet;
    private Tutor tutor;
    private Clinica clinica;

    @BeforeEach
    void setUp() {
        // Classificador real (unidade pura, ja validada em ClassificadorDeUrgenciaTest)
        // para que o teste de servico verifique a integracao entre as duas classes.
        triagemService = new TriagemService(triagemRepository, petRepository, tutorRepository,
                clinicaRepository, new ClassificadorDeUrgencia());

        pet = Pet.builder().id(1L).nome("Rex").build();
        tutor = Tutor.builder().id(10L).build();
        clinica = Clinica.builder().id(100L).nome("Clinica Amiga Bicho").ativa("S").build();
    }

    @Test
    void abrirTriagemClassificaEPersisteComStatusAberta() {
        TriagemForm form = new TriagemForm();
        form.setPetId(1L);
        form.setClinicaId(100L);
        form.setCanal(CanalTriagem.TEXTO);
        form.setRelato("Meu cachorro esta sangrando e nao para");

        when(petRepository.existeVinculoComTutor(1L, 10L)).thenReturn(true);
        when(petRepository.findById(1L)).thenReturn(Optional.of(pet));
        when(tutorRepository.findById(10L)).thenReturn(Optional.of(tutor));
        when(clinicaRepository.findById(100L)).thenReturn(Optional.of(clinica));
        when(triagemRepository.save(any(Triagem.class))).thenAnswer(inv -> inv.getArgument(0));

        Triagem resultado = triagemService.abrirTriagem(10L, form);

        assertThat(resultado.getStatusTriagem()).isEqualTo(StatusTriagem.ABERTA);
        assertThat(resultado.getNivelUrgencia()).isEqualTo(NivelUrgencia.EMERGENCIA);
        assertThat(resultado.getPet()).isEqualTo(pet);
        assertThat(resultado.getTutor()).isEqualTo(tutor);
        assertThat(resultado.getClinica()).isEqualTo(clinica);

        ArgumentCaptor<Triagem> captor = ArgumentCaptor.forClass(Triagem.class);
        verify(triagemRepository).save(captor.capture());
        assertThat(captor.getValue().getScoreRisco()).isEqualByComparingTo(new BigDecimal("90"));
    }

    @Test
    void abrirTriagemComPetDeOutroTutorLancaExcecao() {
        TriagemForm form = new TriagemForm();
        form.setPetId(1L);
        form.setClinicaId(100L);
        form.setCanal(CanalTriagem.TEXTO);
        form.setRelato("Relato qualquer");

        when(petRepository.existeVinculoComTutor(1L, 10L)).thenReturn(false);

        assertThatThrownBy(() -> triagemService.abrirTriagem(10L, form))
                .isInstanceOf(RecursoNaoEncontradoException.class);

        verifyNoInteractions(triagemRepository);
    }

    @Test
    void abrirTriagemComClinicaInativaLancaRegraDeNegocio() {
        clinica.setAtiva("N");
        TriagemForm form = new TriagemForm();
        form.setPetId(1L);
        form.setClinicaId(100L);
        form.setCanal(CanalTriagem.TEXTO);
        form.setRelato("Relato qualquer");

        when(petRepository.existeVinculoComTutor(1L, 10L)).thenReturn(true);
        when(petRepository.findById(1L)).thenReturn(Optional.of(pet));
        when(tutorRepository.findById(10L)).thenReturn(Optional.of(tutor));
        when(clinicaRepository.findById(100L)).thenReturn(Optional.of(clinica));

        assertThatThrownBy(() -> triagemService.abrirTriagem(10L, form))
                .isInstanceOf(RegraNegocioException.class);

        verify(triagemRepository, never()).save(any());
    }

    @Test
    void filaDeAtencaoOrdenaPorUrgenciaEDepoisPorOrdemDeChegada() {
        Triagem baixaMaisAntiga = triagemComUrgencia(1L, NivelUrgencia.BAIXA, LocalDateTime.now().minusHours(3));
        Triagem emergenciaMaisRecente = triagemComUrgencia(2L, NivelUrgencia.EMERGENCIA, LocalDateTime.now());
        Triagem altaIntermediaria = triagemComUrgencia(3L, NivelUrgencia.ALTA, LocalDateTime.now().minusHours(1));
        Triagem emergenciaMaisAntiga = triagemComUrgencia(4L, NivelUrgencia.EMERGENCIA, LocalDateTime.now().minusHours(2));

        when(triagemRepository.findFilaDeAtencao(eq(100L), anyList()))
                .thenReturn(List.of(baixaMaisAntiga, emergenciaMaisRecente, altaIntermediaria, emergenciaMaisAntiga));

        List<Triagem> fila = triagemService.filaDeAtencao(100L);

        // As duas EMERGENCIA vem primeiro (ordenadas por chegada entre si), depois ALTA, depois BAIXA.
        assertThat(fila).extracting(Triagem::getId)
                .containsExactly(4L, 2L, 3L, 1L);
    }

    @Test
    void encaminharSoPermitidoQuandoStatusEAberta() {
        Triagem triagem = triagemComStatus(StatusTriagem.ENCAMINHADA);
        when(triagemRepository.findById(5L)).thenReturn(Optional.of(triagem));

        assertThatThrownBy(() -> triagemService.encaminhar(5L, 100L))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void encaminharComSucessoAtualizaStatusEDataDeEncaminhamento() {
        Triagem triagem = triagemComStatus(StatusTriagem.ABERTA);
        when(triagemRepository.findById(5L)).thenReturn(Optional.of(triagem));
        when(triagemRepository.save(any(Triagem.class))).thenAnswer(inv -> inv.getArgument(0));

        Triagem resultado = triagemService.encaminhar(5L, 100L);

        assertThat(resultado.getStatusTriagem()).isEqualTo(StatusTriagem.ENCAMINHADA);
        assertThat(resultado.getEncaminhadaEm()).isNotNull();
    }

    @Test
    void buscarTriagemDeOutraClinicaLancaExcecao() {
        Triagem triagem = triagemComStatus(StatusTriagem.ABERTA);
        // clinica com id diferente do solicitado
        when(triagemRepository.findById(5L)).thenReturn(Optional.of(triagem));

        assertThatThrownBy(() -> triagemService.encaminhar(5L, 999L))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void cancelarTriagemJaAtendidaLancaExcecao() {
        Triagem triagem = triagemComStatus(StatusTriagem.ATENDIDA);
        when(triagemRepository.findById(5L)).thenReturn(Optional.of(triagem));

        assertThatThrownBy(() -> triagemService.cancelarPelaClinica(5L, 100L))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void tutorNaoConsegueCancelarTriagemDeOutroTutor() {
        Triagem triagem = Triagem.builder()
                .id(7L)
                .tutor(Tutor.builder().id(999L).build())
                .statusTriagem(StatusTriagem.ABERTA)
                .build();
        when(triagemRepository.findById(7L)).thenReturn(Optional.of(triagem));

        assertThatThrownBy(() -> triagemService.cancelarPeloTutor(7L, 10L))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    private Triagem triagemComStatus(StatusTriagem status) {
        return Triagem.builder()
                .id(5L)
                .pet(pet)
                .tutor(tutor)
                .clinica(clinica)
                .statusTriagem(status)
                .nivelUrgencia(NivelUrgencia.MEDIA)
                .build();
    }

    private Triagem triagemComUrgencia(Long id, NivelUrgencia nivel, LocalDateTime criadaEm) {
        Triagem triagem = Triagem.builder()
                .id(id)
                .pet(pet)
                .clinica(clinica)
                .nivelUrgencia(nivel)
                .statusTriagem(StatusTriagem.ABERTA)
                .build();
        triagem.setCriadaEm(criadaEm);
        return triagem;
    }
}
