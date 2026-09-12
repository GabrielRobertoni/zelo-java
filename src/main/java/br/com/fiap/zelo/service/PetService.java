package br.com.fiap.zelo.service;

import br.com.fiap.zelo.domain.Pet;
import br.com.fiap.zelo.domain.PetTutor;
import br.com.fiap.zelo.domain.Tutor;
import br.com.fiap.zelo.exception.RecursoNaoEncontradoException;
import br.com.fiap.zelo.repository.PetRepository;
import br.com.fiap.zelo.repository.PetTutorRepository;
import br.com.fiap.zelo.repository.TutorRepository;
import br.com.fiap.zelo.web.dto.PetForm;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class PetService {

    private final PetRepository petRepository;
    private final PetTutorRepository petTutorRepository;
    private final TutorRepository tutorRepository;

    public PetService(PetRepository petRepository, PetTutorRepository petTutorRepository, TutorRepository tutorRepository) {
        this.petRepository = petRepository;
        this.petTutorRepository = petTutorRepository;
        this.tutorRepository = tutorRepository;
    }

    @Transactional(readOnly = true)
    public List<Pet> listarDoTutor(Long tutorId) {
        return petRepository.findByTutorId(tutorId);
    }

    /** Usado pela equipe da clinica para selecionar um pet ao criar um alerta. */
    @Transactional(readOnly = true)
    public List<Pet> listarTodos() {
        return petRepository.findAll(Sort.by(Sort.Direction.ASC, "nome"));
    }

    @Transactional(readOnly = true)
    public Pet buscarDoTutor(Long petId, Long tutorId) {
        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pet nao encontrado."));
        garantirVinculo(petId, tutorId);
        return pet;
    }

    public Pet cadastrar(Long tutorId, PetForm form) {
        Tutor tutor = tutorRepository.findById(tutorId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Tutor nao encontrado."));

        Pet pet = Pet.builder()
                .nome(form.getNome())
                .especie(form.getEspecie())
                .raca(form.getRaca())
                .sexo(form.getSexo())
                .dataNascimento(form.getDataNascimento())
                .pesoKg(form.getPesoKg())
                .castrado(form.isCastrado() ? "S" : "N")
                .build();
        pet = petRepository.save(pet);

        PetTutor vinculo = PetTutor.builder()
                .pet(pet)
                .tutor(tutor)
                .responsavelPrincipal("S")
                .build();
        petTutorRepository.save(vinculo);

        return pet;
    }

    public Pet atualizar(Long petId, Long tutorId, PetForm form) {
        Pet pet = buscarDoTutor(petId, tutorId);
        pet.setNome(form.getNome());
        pet.setEspecie(form.getEspecie());
        pet.setRaca(form.getRaca());
        pet.setSexo(form.getSexo());
        pet.setDataNascimento(form.getDataNascimento());
        pet.setPesoKg(form.getPesoKg());
        pet.setCastrado(form.isCastrado() ? "S" : "N");
        return petRepository.save(pet);
    }

    public void excluir(Long petId, Long tutorId) {
        Pet pet = buscarDoTutor(petId, tutorId);
        petRepository.delete(pet);
    }

    private void garantirVinculo(Long petId, Long tutorId) {
        if (!petRepository.existeVinculoComTutor(petId, tutorId)) {
            throw new RecursoNaoEncontradoException("Este pet nao pertence ao tutor autenticado.");
        }
    }
}
