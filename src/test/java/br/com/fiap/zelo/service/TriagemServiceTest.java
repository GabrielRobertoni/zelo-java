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
import br.com.fiap.zelo.repository.TriagemRepository;
import br.com.fiap.zelo.repository.TutorRepository;
import br.com.fiap.zelo.web.dto.TriagemForm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TriagemServiceTest {

    @Mock
    private TriagemRepository triagemRepository;
    @Mock
    private TutorRepository tutorRepository;
    @Mock
    private PetService petService;
    @Mock
    private ClinicaService clinicaService;

    private TriagemService triagemService;
    private final Pet pet = Pet.builder().id(1L).nome("Rex").build();
    private final Tutor tutor = Tutor.builder().id(10L).build();
    private final Clinica clinica = Clinica.builder().id(100L).nome("Clínica Amiga Bicho").build();

    @BeforeEach
    void setUp() {
        triagemService = new TriagemService(triagemRepository, tutorRepository, petService, clinicaService,
                new ClassificadorDeUrgencia());
    }

    private TriagemForm formulario(String relato) {
        TriagemForm form = new TriagemForm();
        form.setPetId(1L);
        form.setClinicaId(100L);
        form.setCanal(CanalTriagem.TEXTO);
        form.setRelato(relato);
        return form;
    }

    private Triagem triagem(Long id, NivelUrgencia urgencia, StatusTriagem status, LocalDateTime criadaEm) {
        return Triagem.builder().id(id).pet(pet).tutor(tutor).clinica(clinica).nivelUrgencia(urgencia)
                .statusTriagem(status).criadaEm(criadaEm).scoreRisco(BigDecimal.ONE).build();
    }

    @Test
    void abrirTriagemClassificaOPrioridadeEPersisteComoAberta() {
        when(petService.buscarDoTutor(1L, 10L)).thenReturn(pet);
        when(tutorRepository.findById(10L)).thenReturn(Optional.of(tutor));
        when(clinicaService.buscarAtiva(100L)).thenReturn(clinica);
        when(triagemRepository.save(any(Triagem.class))).thenAnswer(invocacao -> invocacao.getArgument(0));

        Triagem triagem = triagemService.abrirTriagem(10L, formulario("Meu cachorro está sangrando e não para"));

        assertThat(triagem.getStatusTriagem()).isEqualTo(StatusTriagem.ABERTA);
        assertThat(triagem.getNivelUrgencia()).isEqualTo(NivelUrgencia.EMERGENCIA);
        assertThat(triagem.getScoreRisco()).isEqualByComparingTo("90");
        assertThat(triagem.getClinica()).isEqualTo(clinica);
        assertThat(triagem.getTutor()).isEqualTo(tutor);
    }

    @Test
    void naoAbreTriagemParaPetDeOutroTutor() {
        when(petService.buscarDoTutor(1L, 10L)).thenThrow(new RecursoNaoEncontradoException("Pet não encontrado"));

        assertThatThrownBy(() -> triagemService.abrirTriagem(10L, formulario("Está com tosse há dias")))
                .isInstanceOf(RecursoNaoEncontradoException.class);
        verify(triagemRepository, never()).save(any());
    }

    @Test
    void naoAbreTriagemEmClinicaIndisponivel() {
        when(petService.buscarDoTutor(1L, 10L)).thenReturn(pet);
        when(tutorRepository.findById(10L)).thenReturn(Optional.of(tutor));
        when(clinicaService.buscarAtiva(100L)).thenThrow(new RegraNegocioException("Clínica indisponível"));

        assertThatThrownBy(() -> triagemService.abrirTriagem(10L, formulario("Está com tosse há dias")))
                .isInstanceOf(RegraNegocioException.class);
        verify(triagemRepository, never()).save(any());
    }

    @Test
    void filaOrdenaPorUrgenciaEDepoisPorChegada() {
        LocalDateTime agora = LocalDateTime.now();
        Triagem baixaAntiga = triagem(1L, NivelUrgencia.BAIXA, StatusTriagem.ABERTA, agora.minusHours(5));
        Triagem altaRecente = triagem(2L, NivelUrgencia.ALTA, StatusTriagem.ABERTA, agora.minusHours(1));
        Triagem altaAntiga = triagem(3L, NivelUrgencia.ALTA, StatusTriagem.ENCAMINHADA, agora.minusHours(3));
        Triagem emergencia = triagem(4L, NivelUrgencia.EMERGENCIA, StatusTriagem.ABERTA, agora);
        when(triagemRepository.findDaClinica(eq(100L), anyCollection(), any()))
                .thenReturn(List.of(baixaAntiga, altaRecente, altaAntiga, emergencia));

        assertThat(triagemService.filaDeAtencao(100L)).extracting(Triagem::getId).containsExactly(4L, 3L, 2L, 1L);
    }

    @Test
    void clinicaEncaminhaTriagemPropria() {
        Triagem triagem = triagem(1L, NivelUrgencia.MEDIA, StatusTriagem.ABERTA, LocalDateTime.now());
        when(triagemRepository.findById(1L)).thenReturn(Optional.of(triagem));
        when(triagemRepository.save(triagem)).thenReturn(triagem);

        assertThat(triagemService.encaminhar(1L, 100L).getStatusTriagem()).isEqualTo(StatusTriagem.ENCAMINHADA);
    }

    @Test
    void clinicaNaoAlteraTriagemDeOutraClinica() {
        when(triagemRepository.findById(1L))
                .thenReturn(Optional.of(triagem(1L, NivelUrgencia.MEDIA, StatusTriagem.ABERTA, LocalDateTime.now())));

        assertThatThrownBy(() -> triagemService.concluir(1L, 999L)).isInstanceOf(RecursoNaoEncontradoException.class);
        verify(triagemRepository, never()).save(any());
    }

    @Test
    void tutorNaoCancelaTriagemDeOutroTutor() {
        when(triagemRepository.findById(1L))
                .thenReturn(Optional.of(triagem(1L, NivelUrgencia.MEDIA, StatusTriagem.ABERTA, LocalDateTime.now())));

        assertThatThrownBy(() -> triagemService.cancelarPeloTutor(1L, 11L)).isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void tutorNaoCancelaTriagemSemTutorVinculado() {
        Triagem semTutor = triagem(1L, NivelUrgencia.MEDIA, StatusTriagem.ABERTA, LocalDateTime.now());
        semTutor.setTutor(null);
        when(triagemRepository.findById(1L)).thenReturn(Optional.of(semTutor));

        assertThatThrownBy(() -> triagemService.cancelarPeloTutor(1L, 10L)).isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void triagemInexistenteGeraRecursoNaoEncontrado() {
        when(triagemRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> triagemService.encaminhar(9L, 100L)).isInstanceOf(RecursoNaoEncontradoException.class);
    }
}
