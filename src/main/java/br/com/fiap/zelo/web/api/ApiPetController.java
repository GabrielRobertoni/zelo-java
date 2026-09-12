package br.com.fiap.zelo.web.api;

import br.com.fiap.zelo.domain.Pet;
import br.com.fiap.zelo.security.ContextoAtualService;
import br.com.fiap.zelo.security.ZeloUserPrincipal;
import br.com.fiap.zelo.service.PetService;
import br.com.fiap.zelo.web.dto.PetForm;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/pets")
@PreAuthorize("hasRole('TUTOR')")
public class ApiPetController {
    private final PetService petService;
    private final ContextoAtualService contexto;

    public ApiPetController(PetService petService, ContextoAtualService contexto) {
        this.petService = petService;
        this.contexto = contexto;
    }

    @GetMapping
    public List<ApiModels.PetResponse> list(@AuthenticationPrincipal ZeloUserPrincipal principal) {
        Long tutorId = contexto.tutorAtual(principal).getId();
        return petService.listarDoTutor(tutorId).stream().map(ApiPetController::response).toList();
    }

    @GetMapping("/{id}")
    public ApiModels.PetResponse get(@AuthenticationPrincipal ZeloUserPrincipal principal, @PathVariable Long id) {
        return response(petService.buscarDoTutor(id, contexto.tutorAtual(principal).getId()));
    }

    @PostMapping
    public ApiModels.PetResponse create(@AuthenticationPrincipal ZeloUserPrincipal principal, @Valid @RequestBody ApiModels.PetInput input) {
        return response(petService.cadastrar(contexto.tutorAtual(principal).getId(), form(input)));
    }

    @PutMapping("/{id}")
    public ApiModels.PetResponse update(@AuthenticationPrincipal ZeloUserPrincipal principal, @PathVariable Long id,
                                        @Valid @RequestBody ApiModels.PetInput input) {
        return response(petService.atualizar(id, contexto.tutorAtual(principal).getId(), form(input)));
    }

    @DeleteMapping("/{id}")
    public void delete(@AuthenticationPrincipal ZeloUserPrincipal principal, @PathVariable Long id) {
        petService.excluir(id, contexto.tutorAtual(principal).getId());
    }

    private static PetForm form(ApiModels.PetInput input) {
        PetForm form = new PetForm();
        form.setNome(input.nome()); form.setEspecie(input.especie()); form.setRaca(input.raca()); form.setSexo(input.sexo());
        form.setDataNascimento(input.dataNascimento()); form.setPesoKg(input.pesoKg()); form.setCastrado(Boolean.TRUE.equals(input.castrado()));
        return form;
    }

    private static ApiModels.PetResponse response(Pet pet) {
        return new ApiModels.PetResponse(pet.getId(), pet.getNome(), pet.getEspecie(), pet.getRaca(), pet.getSexo(), pet.getDataNascimento(), pet.getPesoKg(), pet.isCastrado());
    }
}
