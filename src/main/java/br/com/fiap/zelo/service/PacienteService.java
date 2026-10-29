package br.com.fiap.zelo.service;

import br.com.fiap.zelo.domain.Pet;
import br.com.fiap.zelo.exception.RecursoNaoEncontradoException;
import br.com.fiap.zelo.repository.PetRepository;
import br.com.fiap.zelo.repository.TriagemRepository;
import br.com.fiap.zelo.repository.projecao.PacienteResumo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Um pet passa a ser paciente de uma clínica quando o tutor abre uma triagem
 * nela. Só pacientes ficam visíveis para a equipe (privacidade dos dados).
 */
@Service
@Transactional(readOnly = true)
public class PacienteService {

    private final TriagemRepository triagemRepository;
    private final PetRepository petRepository;

    public PacienteService(TriagemRepository triagemRepository, PetRepository petRepository) {
        this.triagemRepository = triagemRepository;
        this.petRepository = petRepository;
    }

    public List<PacienteResumo> listarResumos(Long clinicaId) {
        return triagemRepository.listarPacientesDaClinica(clinicaId);
    }

    public List<Pet> listarPacientes(Long clinicaId) {
        return petRepository.findPacientesDaClinica(clinicaId);
    }

    public Pet buscarPaciente(Long petId, Long clinicaId) {
        if (petId == null || !triagemRepository.existsByPetIdAndClinicaId(petId, clinicaId)) {
            throw new RecursoNaoEncontradoException("Este pet não é paciente da sua clínica.");
        }
        return petRepository.findComTutores(petId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pet não encontrado."));
    }
}
