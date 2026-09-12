package br.com.fiap.zelo.web.controller;

import br.com.fiap.zelo.domain.Clinica;
import br.com.fiap.zelo.domain.Tutor;
import br.com.fiap.zelo.security.ContextoAtualService;
import br.com.fiap.zelo.security.ZeloUserPrincipal;
import br.com.fiap.zelo.service.AlertaService;
import br.com.fiap.zelo.service.PetService;
import br.com.fiap.zelo.service.TriagemService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final ContextoAtualService contexto;
    private final PetService petService;
    private final TriagemService triagemService;
    private final AlertaService alertaService;

    public DashboardController(ContextoAtualService contexto, PetService petService,
                                TriagemService triagemService, AlertaService alertaService) {
        this.contexto = contexto;
        this.petService = petService;
        this.triagemService = triagemService;
        this.alertaService = alertaService;
    }

    @GetMapping("/tutor/dashboard")
    public String dashboardTutor(@AuthenticationPrincipal ZeloUserPrincipal principal, Model model) {
        Tutor tutor = contexto.tutorAtual(principal);
        model.addAttribute("pets", petService.listarDoTutor(tutor.getId()));
        model.addAttribute("alertasPendentes", alertaService.pendentesDoTutor(tutor.getId()));
        model.addAttribute("triagensRecentes", triagemService.historicoDoTutor(tutor.getId()));
        return "dashboard/tutor";
    }

    @GetMapping("/clinica/dashboard")
    public String dashboardClinica(@AuthenticationPrincipal ZeloUserPrincipal principal, Model model) {
        Clinica clinica = contexto.clinicaAtual(principal);
        model.addAttribute("clinica", clinica);
        model.addAttribute("filaDeAtencao", triagemService.filaDeAtencao(clinica.getId()));
        model.addAttribute("alertas", alertaService.daClinica(clinica.getId()));
        return "dashboard/clinica";
    }
}
