package br.com.fiap.zelo.web.controller;

import br.com.fiap.zelo.domain.Clinica;
import br.com.fiap.zelo.security.ContextoAtualService;
import br.com.fiap.zelo.security.ZeloUserPrincipal;
import br.com.fiap.zelo.service.ClinicaService;
import br.com.fiap.zelo.web.dto.ClinicaForm;
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

/** Area exclusiva do GESTOR: administracao do cadastro da propria clinica. */
@Controller
@RequestMapping("/clinica/gerenciar")
public class ClinicaGerenciarController {

    private final ClinicaService clinicaService;
    private final ContextoAtualService contexto;

    public ClinicaGerenciarController(ClinicaService clinicaService, ContextoAtualService contexto) {
        this.clinicaService = clinicaService;
        this.contexto = contexto;
    }

    @GetMapping
    public String visualizar(@AuthenticationPrincipal ZeloUserPrincipal principal, Model model) {
        Clinica clinica = contexto.clinicaAtual(principal);
        ClinicaForm form = new ClinicaForm();
        form.setNome(clinica.getNome());
        form.setCnpj(clinica.getCnpj());
        form.setTelefone(clinica.getTelefone());
        form.setEndereco(clinica.getEndereco());
        form.setCidade(clinica.getCidade());
        form.setEstado(clinica.getEstado());

        model.addAttribute("form", form);
        model.addAttribute("clinica", clinica);
        return "clinicas/gerenciar";
    }

    @PostMapping
    public String atualizar(@AuthenticationPrincipal ZeloUserPrincipal principal,
                             @Valid @ModelAttribute("form") ClinicaForm form, BindingResult bindingResult,
                             Model model, RedirectAttributes redirectAttributes) {
        Clinica clinica = contexto.clinicaAtual(principal);
        if (bindingResult.hasErrors()) {
            model.addAttribute("clinica", clinica);
            return "clinicas/gerenciar";
        }
        clinicaService.atualizar(clinica.getId(), principal.getUsuarioId(), form);
        redirectAttributes.addFlashAttribute("sucesso", "Dados da clinica atualizados.");
        return "redirect:/clinica/gerenciar";
    }

    @PostMapping("/alternar")
    public String alternarDisponibilidade(@AuthenticationPrincipal ZeloUserPrincipal principal,
                                           RedirectAttributes redirectAttributes) {
        Clinica clinica = contexto.clinicaAtual(principal);
        Clinica atualizada = clinicaService.alternarDisponibilidade(clinica.getId(), principal.getUsuarioId());
        redirectAttributes.addFlashAttribute("sucesso",
                atualizada.isAtiva() ? "Clinica reativada." : "Clinica marcada como indisponivel.");
        return "redirect:/clinica/gerenciar";
    }
}
