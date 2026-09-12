package br.com.fiap.zelo.service;

import br.com.fiap.zelo.domain.Pet;
import br.com.fiap.zelo.domain.PetTutor;
import br.com.fiap.zelo.domain.Tutor;
import br.com.fiap.zelo.exception.RecursoNaoEncontradoException;
import br.com.fiap.zelo.repository.PetRepository;
import br.com.fiap.zelo.repository.PetTutorRepository;
import br.com.fiap.zelo.repository.TutorRepository;
import br.com.fiap.zelo.web.dto.PetForm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Cobre o cadastro/edicao de pets pelo tutor, incluindo a criacao automatica
 * do vinculo Pet-Tutor (recurso Multi-Tutor) e a checagem de posse usada
 * para impedir que um tutor acesse ou remova o pet de outro tutor.
 */
@ExtendWith(MockitoExtension.class)
class PetServiceTest {

    @Mock
    private PetRepository petRepository;
    @Mock
    private PetTutorRepository petTutorRepository;
    @Mock
    private TutorRepository tutorRepository;

    private PetService petService;

    @BeforeEach
    void setUp() {
        petService = new PetService(petRepository, petTutorRepository, tutorRepository);
    }

    @Test
    void cadastrarCriaPetEVinculoComOTutor() {
        Tutor tutor = Tutor.builder().id(10L).build();
        PetForm form = new PetForm();
        form.setNome("Rex");
        form.setEspecie(br.com.fiap.zelo.domain.enums.Especie.CAO);
        form.setCastrado(true);

        when(tutorRepository.findById(10L)).thenReturn(Optional.of(tutor));
        when(petRepository.save(any(Pet.class))).thenAnswer(inv -> {
            Pet p = inv.getArgument(0);
            p.setId(1L);
            return p;
        });

        Pet pet = petService.cadastrar(10L, form);

        assertThat(pet.getId()).isEqualTo(1L);
        assertThat(pet.getCastrado()).isEqualTo("S");

        ArgumentCaptor<PetTutor> captor = ArgumentCaptor.forClass(PetTutor.class);
        verify(petTutorRepository).save(captor.capture());
        assertThat(captor.getValue().getPet()).isEqualTo(pet);
        assertThat(captor.getValue().getTutor()).isEqualTo(tutor);
        assertThat(captor.getValue().isResponsavelPrincipal()).isTrue();
    }

    @Test
    void buscarDoTutorComPetDeOutroTutorLancaExcecao() {
        Pet pet = Pet.builder().id(1L).nome("Rex").build();
        when(petRepository.findById(1L)).thenReturn(Optional.of(pet));
        when(petRepository.existeVinculoComTutor(1L, 999L)).thenReturn(false);

        assertThatThrownBy(() -> petService.buscarDoTutor(1L, 999L))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void buscarDoTutorInexistenteLancaExcecao() {
        when(petRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> petService.buscarDoTutor(1L, 10L))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void excluirDelegaParaORepositorioApenasQuandoOPetPertenceAoTutor() {
        Pet pet = Pet.builder().id(1L).nome("Rex").build();
        when(petRepository.findById(1L)).thenReturn(Optional.of(pet));
        when(petRepository.existeVinculoComTutor(1L, 10L)).thenReturn(true);

        petService.excluir(1L, 10L);

        verify(petRepository).delete(pet);
    }
}
