package br.com.fiap.zelo.web.controller;

import br.com.fiap.zelo.security.ContextoAtualService;
import br.com.fiap.zelo.security.ZeloUserPrincipal;
import br.com.fiap.zelo.service.PacienteService;
import br.com.fiap.zelo.service.linhadotempo.LinhaDoTempoService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/** Pacientes da clínica: pets que já abriram triagem nela, com o histórico registrado ali. */
@Controller
@RequestMapping("/clinica/pacientes")
public class PacienteController {

    private final PacienteService pacienteService;
    private final LinhaDoTempoService linhaDoTempoService;
    private final ContextoAtualService contexto;

    public PacienteController(PacienteService pacienteService, LinhaDoTempoService linhaDoTempoService,
                              ContextoAtualService contexto) {
        this.pacienteService = pacienteService;
        this.linhaDoTempoService = linhaDoTempoService;
        this.contexto = contexto;
    }

    @GetMapping
    public String listar(@AuthenticationPrincipal ZeloUserPrincipal principal, Model model) {
        model.addAttribute("pacientes", pacienteService.listarResumos(contexto.clinicaIdAtual(principal)));
        return "pacientes/lista";
    }

    @GetMapping("/{petId}")
    public String detalhar(@AuthenticationPrincipal ZeloUserPrincipal principal, @PathVariable Long petId, Model model) {
        Long clinicaId = contexto.clinicaIdAtual(principal);
        model.addAttribute("pet", pacienteService.buscarPaciente(petId, clinicaId));
        model.addAttribute("eventos", linhaDoTempoService.doPacienteNaClinica(petId, clinicaId));
        return "pacientes/detalhe";
    }
}
