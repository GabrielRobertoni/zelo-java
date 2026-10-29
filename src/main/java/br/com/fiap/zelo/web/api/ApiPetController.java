package br.com.fiap.zelo.web.api;

import br.com.fiap.zelo.security.ContextoAtualService;
import br.com.fiap.zelo.security.ZeloUserPrincipal;
import br.com.fiap.zelo.service.PetService;
import br.com.fiap.zelo.web.dto.PetForm;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/pets")
public class ApiPetController {

    private final PetService petService;
    private final ContextoAtualService contexto;

    public ApiPetController(PetService petService, ContextoAtualService contexto) {
        this.petService = petService;
        this.contexto = contexto;
    }

    @GetMapping
    public List<ApiModels.PetResponse> listar(@AuthenticationPrincipal ZeloUserPrincipal principal) {
        return petService.listarDoTutor(contexto.tutorIdAtual(principal)).stream().map(ApiModels.PetResponse::de).toList();
    }

    @GetMapping("/{id}")
    public ApiModels.PetResponse buscar(@AuthenticationPrincipal ZeloUserPrincipal principal, @PathVariable Long id) {
        return ApiModels.PetResponse.de(petService.buscarDoTutor(id, contexto.tutorIdAtual(principal)));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiModels.PetResponse cadastrar(@AuthenticationPrincipal ZeloUserPrincipal principal,
                                           @Valid @RequestBody PetForm form) {
        return ApiModels.PetResponse.de(petService.cadastrar(contexto.tutorIdAtual(principal), form));
    }

    @PutMapping("/{id}")
    public ApiModels.PetResponse atualizar(@AuthenticationPrincipal ZeloUserPrincipal principal, @PathVariable Long id,
                                           @Valid @RequestBody PetForm form) {
        return ApiModels.PetResponse.de(petService.atualizar(id, contexto.tutorIdAtual(principal), form));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@AuthenticationPrincipal ZeloUserPrincipal principal, @PathVariable Long id) {
        petService.excluir(id, contexto.tutorIdAtual(principal));
    }
}
