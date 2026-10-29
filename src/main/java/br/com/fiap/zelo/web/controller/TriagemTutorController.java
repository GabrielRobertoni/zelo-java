package br.com.fiap.zelo.web.controller;

import br.com.fiap.zelo.security.ContextoAtualService;
import br.com.fiap.zelo.security.ZeloUserPrincipal;
import br.com.fiap.zelo.service.ClinicaService;
import br.com.fiap.zelo.service.PetService;
import br.com.fiap.zelo.service.TriagemService;
import br.com.fiap.zelo.web.dto.TriagemForm;
import br.com.fiap.zelo.web.suporte.Feedback;
import br.com.fiap.zelo.web.suporte.Formulario;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/tutor/triagens")
public class TriagemTutorController {

    private static final String HISTORICO = "/tutor/triagens";

    private final TriagemService triagemService;
    private final PetService petService;
    private final ClinicaService clinicaService;
    private final ContextoAtualService contexto;

    public TriagemTutorController(TriagemService triagemService, PetService petService, ClinicaService clinicaService,
                                  ContextoAtualService contexto) {
        this.triagemService = triagemService;
        this.petService = petService;
        this.clinicaService = clinicaService;
        this.contexto = contexto;
    }

    @GetMapping
    public String historico(@AuthenticationPrincipal ZeloUserPrincipal principal, Model model) {
        model.addAttribute("triagens", triagemService.historicoDoTutor(contexto.tutorIdAtual(principal)));
        return "triagens/historico";
    }

    @GetMapping("/nova")
    public String nova(@AuthenticationPrincipal ZeloUserPrincipal principal,
                       @RequestParam(required = false) Long petId, Model model) {
        TriagemForm form = new TriagemForm();
        form.setPetId(petId);
        return exibirFormulario(principal, model, form);
    }

    @PostMapping
    public String abrir(@AuthenticationPrincipal ZeloUserPrincipal principal,
                        @Valid @ModelAttribute("form") TriagemForm form, BindingResult validacao, Model model,
                        RedirectAttributes atributos) {
        return Formulario.processar(validacao, () -> {
            var triagem = triagemService.abrirTriagem(contexto.tutorIdAtual(principal), form);
            atributos.addFlashAttribute("sucesso", "Triagem enviada para " + triagem.getClinica().getNome()
                    + ". Urgência classificada como " + triagem.getNivelUrgencia().getRotulo().toLowerCase() + ".");
            return "redirect:" + HISTORICO;
        }, () -> exibirFormulario(principal, model, form));
    }

    @PostMapping("/{id}/cancelar")
    public String cancelar(@AuthenticationPrincipal ZeloUserPrincipal principal, @PathVariable Long id,
                           RedirectAttributes atributos) {
        return Feedback.executar(atributos, HISTORICO, "Triagem cancelada.",
                () -> triagemService.cancelarPeloTutor(id, contexto.tutorIdAtual(principal)));
    }

    private String exibirFormulario(ZeloUserPrincipal principal, Model model, TriagemForm form) {
        model.addAttribute("form", form);
        model.addAttribute("pets", petService.listarDoTutor(contexto.tutorIdAtual(principal)));
        model.addAttribute("clinicas", clinicaService.listarAtivas());
        return "triagens/nova";
    }
}
