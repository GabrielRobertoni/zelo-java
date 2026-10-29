package br.com.fiap.zelo.web.api;

import br.com.fiap.zelo.exception.RegraNegocioException;
import br.com.fiap.zelo.security.ContextoAtualService;
import br.com.fiap.zelo.security.ZeloUserPrincipal;
import br.com.fiap.zelo.service.ClinicaService;
import br.com.fiap.zelo.service.TriagemService;
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
@RequestMapping("/api/triagens")
public class ApiTriageController {

    private final TriagemService triagemService;
    private final ClinicaService clinicaService;
    private final ContextoAtualService contexto;
    private final ApiFormularios formularios;

    public ApiTriageController(TriagemService triagemService, ClinicaService clinicaService,
                               ContextoAtualService contexto, ApiFormularios formularios) {
        this.triagemService = triagemService;
        this.clinicaService = clinicaService;
        this.contexto = contexto;
        this.formularios = formularios;
    }

    @GetMapping
    public List<ApiModels.TriageResponse> listar(@AuthenticationPrincipal ZeloUserPrincipal principal) {
        return triagemService.historicoDoTutor(contexto.tutorIdAtual(principal)).stream()
                .map(ApiModels.TriageResponse::de)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiModels.TriageResponse abrir(@AuthenticationPrincipal ZeloUserPrincipal principal,
                                          @RequestBody ApiModels.TriageInput entrada) throws BindException {
        var form = formularios.validar(entrada.paraFormulario(clinicaPadraoId()));
        return ApiModels.TriageResponse.de(triagemService.abrirTriagem(contexto.tutorIdAtual(principal), form));
    }

    @PatchMapping("/{id}/cancelar")
    public ApiModels.TriageResponse cancelar(@AuthenticationPrincipal ZeloUserPrincipal principal, @PathVariable Long id) {
        return ApiModels.TriageResponse.de(triagemService.cancelarPeloTutor(id, contexto.tutorIdAtual(principal)));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@AuthenticationPrincipal ZeloUserPrincipal principal, @PathVariable Long id) {
        triagemService.excluirPeloTutor(id, contexto.tutorIdAtual(principal));
    }

    private Long clinicaPadraoId() {
        return clinicaService.listarAtivas().stream()
                .findFirst()
                .orElseThrow(() -> new RegraNegocioException("Nenhuma clínica está recebendo triagens no momento."))
                .getId();
    }
}
