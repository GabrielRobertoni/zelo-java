package br.com.fiap.zelo.web.controller;

import br.com.fiap.zelo.security.ContextoAtualService;
import br.com.fiap.zelo.security.ZeloUserPrincipal;
import br.com.fiap.zelo.service.AlertaService;
import br.com.fiap.zelo.service.PetService;
import br.com.fiap.zelo.web.dto.AlertaForm;
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
@RequestMapping("/tutor/alertas")
public class AlertaTutorController {

    private static final String PLANO = "/tutor/alertas";

    private final AlertaService alertaService;
    private final PetService petService;
    private final ContextoAtualService contexto;

    public AlertaTutorController(AlertaService alertaService, PetService petService, ContextoAtualService contexto) {
        this.alertaService = alertaService;
        this.petService = petService;
        this.contexto = contexto;
    }

    @GetMapping
    public String listar(@AuthenticationPrincipal ZeloUserPrincipal principal,
                         @RequestParam(defaultValue = "pendentes") String aba, Model model) {
        Long tutorId = contexto.tutorIdAtual(principal);
        boolean historico = "historico".equals(aba);
        model.addAttribute("aba", historico ? "historico" : "pendentes");
        model.addAttribute("alertas", historico
                ? alertaService.historicoDoTutor(tutorId)
                : alertaService.abertosDoTutor(tutorId));
        return "alertas/tutor-lista";
    }

    @GetMapping("/novo")
    public String novo(@AuthenticationPrincipal ZeloUserPrincipal principal,
                       @RequestParam(required = false) Long petId, Model model) {
        AlertaForm form = new AlertaForm();
        form.setPetId(petId);
        return exibirFormulario(principal, model, form);
    }

    @PostMapping
    public String criar(@AuthenticationPrincipal ZeloUserPrincipal principal,
                        @Valid @ModelAttribute("form") AlertaForm form, BindingResult validacao, Model model,
                        RedirectAttributes atributos) {
        return Formulario.processar(validacao, () -> {
            alertaService.criarComoTutor(contexto.tutorIdAtual(principal), form);
            atributos.addFlashAttribute("sucesso", "Lembrete adicionado ao plano de cuidado.");
            return "redirect:" + PLANO;
        }, () -> exibirFormulario(principal, model, form));
    }

    @PostMapping("/{id}/confirmar")
    public String confirmar(@AuthenticationPrincipal ZeloUserPrincipal principal, @PathVariable Long id,
                            @RequestParam(required = false) String voltarPara, RedirectAttributes atributos) {
        return Feedback.executar(atributos, Feedback.destinoSeguro(voltarPara, PLANO),
                "Cuidado confirmado. Continue assim!",
                () -> alertaService.confirmar(id, contexto.tutorIdAtual(principal)));
    }

    @PostMapping("/{id}/cancelar")
    public String cancelar(@AuthenticationPrincipal ZeloUserPrincipal principal, @PathVariable Long id,
                           @RequestParam(required = false) String voltarPara, RedirectAttributes atributos) {
        return Feedback.executar(atributos, Feedback.destinoSeguro(voltarPara, PLANO), "Cuidado cancelado.",
                () -> alertaService.cancelarPeloTutor(id, contexto.tutorIdAtual(principal)));
    }

    private String exibirFormulario(ZeloUserPrincipal principal, Model model, AlertaForm form) {
        model.addAttribute("form", form);
        model.addAttribute("pets", petService.listarDoTutor(contexto.tutorIdAtual(principal)));
        model.addAttribute("acao", PLANO);
        model.addAttribute("titulo", "Novo lembrete de cuidado");
        model.addAttribute("voltar", PLANO);
        return "alertas/form";
    }
}
