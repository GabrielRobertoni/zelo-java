package br.com.fiap.zelo.web.api;

import br.com.fiap.zelo.security.ContextoAtualService;
import br.com.fiap.zelo.security.ZeloUserPrincipal;
import br.com.fiap.zelo.service.AlertaService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/alertas")
public class ApiAlertController {

    private final AlertaService alertaService;
    private final ContextoAtualService contexto;
    private final ApiFormularios formularios;

    public ApiAlertController(AlertaService alertaService, ContextoAtualService contexto, ApiFormularios formularios) {
        this.alertaService = alertaService;
        this.contexto = contexto;
        this.formularios = formularios;
    }

    @GetMapping
    public List<ApiModels.AlertResponse> listar(@AuthenticationPrincipal ZeloUserPrincipal principal) {
        return alertaService.abertosDoTutor(contexto.tutorIdAtual(principal)).stream()
                .map(ApiModels.AlertResponse::de)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiModels.AlertResponse criar(@AuthenticationPrincipal ZeloUserPrincipal principal,
                                         @RequestBody ApiModels.AlertInput entrada) throws BindException {
        var form = formularios.validar(entrada.paraFormulario());
        return ApiModels.AlertResponse.de(alertaService.criarComoTutor(contexto.tutorIdAtual(principal), form));
    }

    @PatchMapping("/{id}/confirmar")
    public ApiModels.AlertResponse confirmar(@AuthenticationPrincipal ZeloUserPrincipal principal, @PathVariable Long id) {
        return ApiModels.AlertResponse.de(alertaService.confirmar(id, contexto.tutorIdAtual(principal)));
    }

    @PatchMapping("/{id}/cancelar")
    public ApiModels.AlertResponse cancelar(@AuthenticationPrincipal ZeloUserPrincipal principal, @PathVariable Long id) {
        return ApiModels.AlertResponse.de(alertaService.cancelarPeloTutor(id, contexto.tutorIdAtual(principal)));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@AuthenticationPrincipal ZeloUserPrincipal principal, @PathVariable Long id) {
        alertaService.excluirPeloTutor(id, contexto.tutorIdAtual(principal));
    }
}
