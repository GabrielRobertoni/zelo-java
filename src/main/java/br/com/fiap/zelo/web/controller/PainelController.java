package br.com.fiap.zelo.web.controller;

import br.com.fiap.zelo.security.ContextoAtualService;
import br.com.fiap.zelo.security.ZeloUserPrincipal;
import br.com.fiap.zelo.service.painel.PainelClinicaService;
import br.com.fiap.zelo.service.painel.PainelTutorService;
import br.com.fiap.zelo.service.painel.Periodo;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PainelController {

    private final ContextoAtualService contexto;
    private final PainelTutorService painelTutorService;
    private final PainelClinicaService painelClinicaService;

    public PainelController(ContextoAtualService contexto, PainelTutorService painelTutorService,
                            PainelClinicaService painelClinicaService) {
        this.contexto = contexto;
        this.painelTutorService = painelTutorService;
        this.painelClinicaService = painelClinicaService;
    }

    @GetMapping("/tutor/painel")
    public String painelTutor(@AuthenticationPrincipal ZeloUserPrincipal principal, Model model) {
        model.addAttribute("painel", painelTutorService.montar(contexto.tutorIdAtual(principal)));
        return "painel/tutor";
    }

    @GetMapping("/clinica/painel")
    public String painelClinica(@AuthenticationPrincipal ZeloUserPrincipal principal,
                                @RequestParam(name = "periodo", required = false) String dias, Model model) {
        model.addAttribute("painel", painelClinicaService.montar(contexto.clinicaAtual(principal), Periodo.deDias(dias)));
        model.addAttribute("periodos", Periodo.values());
        return "painel/clinica";
    }
}
