package br.com.fiap.zelo.web.api;

import br.com.fiap.zelo.domain.Alerta;
import br.com.fiap.zelo.security.ContextoAtualService;
import br.com.fiap.zelo.security.ZeloUserPrincipal;
import br.com.fiap.zelo.service.AlertaService;
import br.com.fiap.zelo.web.dto.AlertaForm;
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
@RequestMapping("/api/alertas")
@PreAuthorize("hasRole('TUTOR')")
public class ApiAlertController {
    private final AlertaService alertaService; private final ContextoAtualService contexto;
    public ApiAlertController(AlertaService alertaService, ContextoAtualService contexto) { this.alertaService = alertaService; this.contexto = contexto; }
    @GetMapping public List<ApiModels.AlertResponse> list(@AuthenticationPrincipal ZeloUserPrincipal principal) { return alertaService.pendentesDoTutor(contexto.tutorAtual(principal).getId()).stream().map(ApiAlertController::response).toList(); }
    @PostMapping public ApiModels.AlertResponse create(@AuthenticationPrincipal ZeloUserPrincipal principal, @Valid @RequestBody ApiModels.AlertInput input) { AlertaForm form = new AlertaForm(); form.setPetId(input.petId()); form.setTipoAlerta(input.tipo()); form.setTitulo(input.titulo()); form.setMensagem(input.descricao()); form.setDataPrevista(input.dataPrevista()); return response(alertaService.criarComoTutor(contexto.tutorAtual(principal).getId(), form)); }
    @PatchMapping("/{id}/confirmar") public ApiModels.AlertResponse confirm(@AuthenticationPrincipal ZeloUserPrincipal principal, @PathVariable Long id) { return response(alertaService.confirmar(id, contexto.tutorAtual(principal).getId())); }
    @PatchMapping("/{id}/cancelar") public ApiModels.AlertResponse cancel(@AuthenticationPrincipal ZeloUserPrincipal principal, @PathVariable Long id) { return response(alertaService.cancelarPeloTutor(id, contexto.tutorAtual(principal).getId())); }
    @DeleteMapping("/{id}") public void delete(@AuthenticationPrincipal ZeloUserPrincipal principal, @PathVariable Long id) { alertaService.excluirPeloTutor(id, contexto.tutorAtual(principal).getId()); }
    private static ApiModels.AlertResponse response(Alerta alerta) { return new ApiModels.AlertResponse(alerta.getId(), alerta.getPet().getId(), alerta.getPet().getNome(), alerta.getClinica() == null ? null : alerta.getClinica().getNome(), alerta.getTipoAlerta(), alerta.getTitulo(), alerta.getMensagem(), alerta.getDataPrevista(), alerta.getStatusAlerta().name()); }
}
