package br.com.fiap.zelo.web.api;

import br.com.fiap.zelo.domain.Triagem;
import br.com.fiap.zelo.security.ContextoAtualService;
import br.com.fiap.zelo.security.ZeloUserPrincipal;
import br.com.fiap.zelo.service.ClinicaService;
import br.com.fiap.zelo.service.TriagemService;
import br.com.fiap.zelo.web.dto.TriagemForm;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/triagens")
@PreAuthorize("hasRole('TUTOR')")
public class ApiTriageController {
    private final TriagemService triagemService; private final ClinicaService clinicaService; private final ContextoAtualService contexto;
    public ApiTriageController(TriagemService triagemService, ClinicaService clinicaService, ContextoAtualService contexto) { this.triagemService = triagemService; this.clinicaService = clinicaService; this.contexto = contexto; }
    @GetMapping public List<ApiModels.TriageResponse> list(@AuthenticationPrincipal ZeloUserPrincipal principal) { return triagemService.historicoDoTutor(contexto.tutorAtual(principal).getId()).stream().map(ApiTriageController::response).toList(); }
    @PostMapping public ApiModels.TriageResponse create(@AuthenticationPrincipal ZeloUserPrincipal principal, @Valid @RequestBody ApiModels.TriageInput input) { var clinics = clinicaService.listarAtivas(); if (clinics.isEmpty()) throw new IllegalStateException("Nenhuma clinica ativa disponivel."); TriagemForm form = new TriagemForm(); form.setPetId(input.petId()); form.setClinicaId(input.clinicaId() == null ? clinics.get(0).getId() : input.clinicaId()); form.setCanal(input.canal()); form.setRelato(input.relato()); return response(triagemService.abrirTriagem(contexto.tutorAtual(principal).getId(), form)); }
    @PatchMapping("/{id}/cancelar") public ApiModels.TriageResponse cancel(@AuthenticationPrincipal ZeloUserPrincipal principal, @PathVariable Long id) { return response(triagemService.cancelarPeloTutor(id, contexto.tutorAtual(principal).getId())); }
    @DeleteMapping("/{id}") public void delete(@AuthenticationPrincipal ZeloUserPrincipal principal, @PathVariable Long id) { triagemService.excluirPeloTutor(id, contexto.tutorAtual(principal).getId()); }
    private static ApiModels.TriageResponse response(Triagem triagem) { return new ApiModels.TriageResponse(triagem.getId(), triagem.getPet().getId(), triagem.getPet().getNome(), triagem.getClinica() == null ? null : triagem.getClinica().getNome(), triagem.getCanal(), triagem.getRelato(), triagem.getScoreRisco(), triagem.getNivelUrgencia().name(), triagem.getAnaliseVisual(), triagem.getStatusTriagem().name(), triagem.getCriadaEm()); }
}
