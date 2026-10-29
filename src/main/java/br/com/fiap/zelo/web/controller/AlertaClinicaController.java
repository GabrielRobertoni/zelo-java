package br.com.fiap.zelo.web.controller;

import br.com.fiap.zelo.security.ContextoAtualService;
import br.com.fiap.zelo.security.ZeloUserPrincipal;
import br.com.fiap.zelo.service.AlertaService;
import br.com.fiap.zelo.service.PacienteService;
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
@RequestMapping("/clinica/alertas")
public class AlertaClinicaController {

    private static final String LISTA = "/clinica/alertas";

    private final AlertaService alertaService;
    private final PacienteService pacienteService;
    private final ContextoAtualService contexto;

    public AlertaClinicaController(AlertaService alertaService, PacienteService pacienteService,
                                   ContextoAtualService contexto) {
        this.alertaService = alertaService;
        this.pacienteService = pacienteService;
        this.contexto = contexto;
    }

    @GetMapping
    public String listar(@AuthenticationPrincipal ZeloUserPrincipal principal,
                         @RequestParam(defaultValue = "abertos") String aba, Model model) {
        Long clinicaId = contexto.clinicaIdAtual(principal);
        boolean historico = "historico".equals(aba);
        model.addAttribute("aba", historico ? "historico" : "abertos");
        model.addAttribute("alertas", historico
                ? alertaService.historicoDaClinica(clinicaId)
                : alertaService.abertosDaClinica(clinicaId));
        return "alertas/clinica-lista";
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
            alertaService.criarComoClinica(contexto.clinicaIdAtual(principal), form);
            atributos.addFlashAttribute("sucesso", "Cuidado recomendado ao tutor.");
            return "redirect:" + LISTA;
        }, () -> exibirFormulario(principal, model, form));
    }

    @PostMapping("/{id}/enviar")
    public String enviar(@AuthenticationPrincipal ZeloUserPrincipal principal, @PathVariable Long id,
                         @RequestParam(required = false) String voltarPara, RedirectAttributes atributos) {
        return Feedback.executar(atributos, Feedback.destinoSeguro(voltarPara, LISTA),
                "Lembrete enviado ao tutor.",
                () -> alertaService.marcarComoEnviado(id, contexto.clinicaIdAtual(principal)));
    }

    @PostMapping("/{id}/cancelar")
    public String cancelar(@AuthenticationPrincipal ZeloUserPrincipal principal, @PathVariable Long id,
                           @RequestParam(required = false) String voltarPara, RedirectAttributes atributos) {
        return Feedback.executar(atributos, Feedback.destinoSeguro(voltarPara, LISTA), "Alerta cancelado.",
                () -> alertaService.cancelarPelaClinica(id, contexto.clinicaIdAtual(principal)));
    }

    private String exibirFormulario(ZeloUserPrincipal principal, Model model, AlertaForm form) {
        model.addAttribute("form", form);
        model.addAttribute("pets", pacienteService.listarPacientes(contexto.clinicaIdAtual(principal)));
        model.addAttribute("acao", LISTA);
        model.addAttribute("titulo", "Recomendar cuidado a um paciente");
        model.addAttribute("voltar", LISTA);
        return "alertas/form";
    }
}
