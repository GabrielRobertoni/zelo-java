package br.com.fiap.zelo.web.controller;

import br.com.fiap.zelo.domain.Clinica;
import br.com.fiap.zelo.security.ContextoAtualService;
import br.com.fiap.zelo.security.ZeloUserPrincipal;
import br.com.fiap.zelo.service.ClinicaService;
import br.com.fiap.zelo.web.dto.ClinicaForm;
import br.com.fiap.zelo.web.suporte.Feedback;
import br.com.fiap.zelo.web.suporte.Formulario;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Área exclusiva do GESTOR: dados, disponibilidade e código de convite da clínica. */
@Controller
@RequestMapping("/clinica/gerenciar")
public class ClinicaGerenciarController {

    private static final String PAGINA = "/clinica/gerenciar";

    private final ClinicaService clinicaService;
    private final ContextoAtualService contexto;

    public ClinicaGerenciarController(ClinicaService clinicaService, ContextoAtualService contexto) {
        this.clinicaService = clinicaService;
        this.contexto = contexto;
    }

    @GetMapping
    public String visualizar(@AuthenticationPrincipal ZeloUserPrincipal principal, Model model) {
        Clinica clinica = contexto.clinicaAtual(principal);
        return exibir(model, clinica, ClinicaForm.de(clinica));
    }

    @PostMapping
    public String atualizar(@AuthenticationPrincipal ZeloUserPrincipal principal,
                            @Valid @ModelAttribute("form") ClinicaForm form, BindingResult validacao, Model model,
                            RedirectAttributes atributos) {
        Clinica clinica = contexto.clinicaAtual(principal);
        return Formulario.processar(validacao, () -> {
            clinicaService.atualizar(clinica.getId(), form);
            atributos.addFlashAttribute("sucesso", "Dados da clínica atualizados.");
            return "redirect:" + PAGINA;
        }, () -> exibir(model, clinica, form));
    }

    @PostMapping("/alternar")
    public String alternarDisponibilidade(@AuthenticationPrincipal ZeloUserPrincipal principal,
                                          RedirectAttributes atributos) {
        return Feedback.executar(atributos, PAGINA, () -> clinicaService
                .alternarDisponibilidade(contexto.clinicaIdAtual(principal)).isAtiva()
                ? "Clínica reativada: ela volta a receber triagens."
                : "Clínica marcada como indisponível para novas triagens.");
    }

    @PostMapping("/convite")
    public String renovarConvite(@AuthenticationPrincipal ZeloUserPrincipal principal, RedirectAttributes atributos) {
        return Feedback.executar(atributos, "/clinica/gerenciar/equipe", () -> "Novo código de convite: "
                + clinicaService.renovarConvite(contexto.clinicaIdAtual(principal)).getCodigoConvite()
                + ". O código anterior deixou de valer.");
    }

    private String exibir(Model model, Clinica clinica, ClinicaForm form) {
        model.addAttribute("clinica", clinica);
        model.addAttribute("form", form);
        return "clinicas/gerenciar";
    }
}
