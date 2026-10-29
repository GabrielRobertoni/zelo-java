package br.com.fiap.zelo.service;

import br.com.fiap.zelo.domain.Pet;
import br.com.fiap.zelo.domain.PetTutor;
import br.com.fiap.zelo.domain.Tutor;
import br.com.fiap.zelo.domain.enums.Especie;
import br.com.fiap.zelo.exception.RecursoNaoEncontradoException;
import br.com.fiap.zelo.exception.RegraNegocioException;
import br.com.fiap.zelo.repository.AlertaRepository;
import br.com.fiap.zelo.repository.PetRepository;
import br.com.fiap.zelo.repository.PetTutorRepository;
import br.com.fiap.zelo.repository.TriagemRepository;
import br.com.fiap.zelo.repository.TutorRepository;
import br.com.fiap.zelo.web.dto.PetForm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PetServiceTest {

    @Mock
    private PetRepository petRepository;
    @Mock
    private PetTutorRepository petTutorRepository;
    @Mock
    private TutorRepository tutorRepository;
    @Mock
    private TriagemRepository triagemRepository;
    @Mock
    private AlertaRepository alertaRepository;

    private PetService petService;
    private final Pet rex = Pet.builder().id(1L).nome("Rex").build();

    @BeforeEach
    void setUp() {
        petService = new PetService(petRepository, petTutorRepository, tutorRepository, triagemRepository, alertaRepository);
    }

    @Test
    void cadastrarCriaPetEVinculoPrincipalComOTutor() {
        Tutor tutor = Tutor.builder().id(10L).build();
        PetForm form = new PetForm();
        form.setNome("  Thor ");
        form.setEspecie(Especie.CAO);
        form.setRaca("");
        form.setPesoKg(new BigDecimal("12.5"));
        form.setCastrado(true);
        when(tutorRepository.findById(10L)).thenReturn(Optional.of(tutor));
        when(petRepository.save(any(Pet.class))).thenAnswer(invocacao -> invocacao.getArgument(0));

        Pet pet = petService.cadastrar(10L, form);

        assertThat(pet.getNome()).isEqualTo("Thor");
        assertThat(pet.getRaca()).isNull();
        assertThat(pet.isCastrado()).isTrue();
        ArgumentCaptor<PetTutor> vinculo = ArgumentCaptor.forClass(PetTutor.class);
        verify(petTutorRepository).save(vinculo.capture());
        assertThat(vinculo.getValue().getTutor()).isEqualTo(tutor);
        assertThat(vinculo.getValue().isResponsavelPrincipal()).isTrue();
    }

    @Test
    void buscarPetDeOutroTutorGeraRecursoNaoEncontrado() {
        when(petRepository.existeVinculoComTutor(1L, 99L)).thenReturn(false);

        assertThatThrownBy(() -> petService.buscarDoTutor(1L, 99L)).isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void excluiPetSemHistorico() {
        when(petRepository.existeVinculoComTutor(1L, 10L)).thenReturn(true);
        when(petRepository.findById(1L)).thenReturn(Optional.of(rex));

        petService.excluir(1L, 10L);

        verify(petRepository).delete(rex);
    }

    @Test
    void naoExcluiPetComHistoricoDeTriagens() {
        when(petRepository.existeVinculoComTutor(1L, 10L)).thenReturn(true);
        when(petRepository.findById(1L)).thenReturn(Optional.of(rex));
        when(triagemRepository.existsByPetId(1L)).thenReturn(true);

        assertThatThrownBy(() -> petService.excluir(1L, 10L))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("histórico");
        verify(petRepository, never()).delete(any());
    }
}
