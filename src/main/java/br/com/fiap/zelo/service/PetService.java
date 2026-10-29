package br.com.fiap.zelo.service;

import br.com.fiap.zelo.domain.Pet;
import br.com.fiap.zelo.domain.PetTutor;
import br.com.fiap.zelo.domain.Tutor;
import br.com.fiap.zelo.exception.RecursoNaoEncontradoException;
import br.com.fiap.zelo.exception.RegraNegocioException;
import br.com.fiap.zelo.repository.AlertaRepository;
import br.com.fiap.zelo.repository.PetRepository;
import br.com.fiap.zelo.repository.PetTutorRepository;
import br.com.fiap.zelo.repository.TriagemRepository;
import br.com.fiap.zelo.repository.TutorRepository;
import br.com.fiap.zelo.web.dto.PetForm;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class PetService {

    private final PetRepository petRepository;
    private final PetTutorRepository petTutorRepository;
    private final TutorRepository tutorRepository;
    private final TriagemRepository triagemRepository;
    private final AlertaRepository alertaRepository;

    public PetService(PetRepository petRepository, PetTutorRepository petTutorRepository, TutorRepository tutorRepository,
                      TriagemRepository triagemRepository, AlertaRepository alertaRepository) {
        this.petRepository = petRepository;
        this.petTutorRepository = petTutorRepository;
        this.tutorRepository = tutorRepository;
        this.triagemRepository = triagemRepository;
        this.alertaRepository = alertaRepository;
    }

    @Transactional(readOnly = true)
    public List<Pet> listarDoTutor(Long tutorId) {
        return petRepository.findByTutorId(tutorId);
    }

    /** Garante que o pet existe e pertence ao tutor; usado também pelos fluxos de triagem e alerta. */
    @Transactional(readOnly = true)
    public Pet buscarDoTutor(Long petId, Long tutorId) {
        if (petId == null || !petRepository.existeVinculoComTutor(petId, tutorId)) {
            throw new RecursoNaoEncontradoException("Pet não encontrado entre os pets do tutor autenticado.");
        }
        return petRepository.findById(petId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pet não encontrado."));
    }

    public Pet cadastrar(Long tutorId, PetForm form) {
        Tutor tutor = tutorRepository.findById(tutorId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Tutor não encontrado."));
        Pet pet = petRepository.save(aplicar(Pet.builder().build(), form));
        petTutorRepository.save(PetTutor.builder().pet(pet).tutor(tutor).responsavelPrincipal("S").build());
        return pet;
    }

    public Pet atualizar(Long petId, Long tutorId, PetForm form) {
        return petRepository.save(aplicar(buscarDoTutor(petId, tutorId), form));
    }

    public void excluir(Long petId, Long tutorId) {
        Pet pet = buscarDoTutor(petId, tutorId);
        if (triagemRepository.existsByPetId(petId) || alertaRepository.existsByPetId(petId)) {
            throw new RegraNegocioException(
                    "Não é possível excluir " + pet.getNome() + ": o pet já possui histórico de triagens ou cuidados.");
        }
        petRepository.delete(pet);
    }

    private Pet aplicar(Pet pet, PetForm form) {
        pet.setNome(form.getNome().trim());
        pet.setEspecie(form.getEspecie());
        pet.setRaca(Normalizador.textoOuNulo(form.getRaca()));
        pet.setSexo(form.getSexo());
        pet.setDataNascimento(form.getDataNascimento());
        pet.setPesoKg(form.getPesoKg());
        pet.setCastrado(form.isCastrado() ? "S" : "N");
        return pet;
    }
}
