package br.com.fiap.zelo.web.controller;

import br.com.fiap.zelo.domain.Clinica;
import br.com.fiap.zelo.domain.Tutor;
import br.com.fiap.zelo.domain.enums.TipoAlerta;
import br.com.fiap.zelo.exception.RecursoNaoEncontradoException;
import br.com.fiap.zelo.exception.RegraNegocioException;
import br.com.fiap.zelo.security.ContextoAtualService;
import br.com.fiap.zelo.security.ZeloUserPrincipal;
import br.com.fiap.zelo.service.AlertaService;
import br.com.fiap.zelo.service.PetService;
import br.com.fiap.zelo.web.dto.AlertaForm;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AlertaController {

    private final AlertaService alertaService;
    private final PetService petService;
    private final ContextoAtualService contexto;

    public AlertaController(AlertaService alertaService, PetService petService, ContextoAtualService contexto) {
        this.alertaService = alertaService;
        this.petService = petService;
        this.contexto = contexto;
    }

    // ---------------------------------------------------------------- Tutor
    @GetMapping("/tutor/alertas")
    public String listaTutor(@AuthenticationPrincipal ZeloUserPrincipal principal, Model model) {
        Tutor tutor = contexto.tutorAtual(principal);
        model.addAttribute("alertas", alertaService.pendentesDoTutor(tutor.getId()));
        return "alertas/tutor-lista";
    }

    @GetMapping("/tutor/alertas/novo")
    public String novoComoTutor(@AuthenticationPrincipal ZeloUserPrincipal principal, Model model) {
        Tutor tutor = contexto.tutorAtual(principal);
        model.addAttribute("form", new AlertaForm());
        model.addAttribute("pets", petService.listarDoTutor(tutor.getId()));
        model.addAttribute("tipos", TipoAlerta.values());
        model.addAttribute("acao", "/tutor/alertas");
        model.addAttribute("titulo", "Novo lembrete");
        return "alertas/form";
    }

    @PostMapping("/tutor/alertas")
    public String criarComoTutor(@AuthenticationPrincipal ZeloUserPrincipal principal,
                                  @Valid @ModelAttribute("form") AlertaForm form, BindingResult bindingResult,
                                  Model model, RedirectAttributes redirectAttributes) {
        Tutor tutor = contexto.tutorAtual(principal);
        if (bindingResult.hasErrors()) {
            model.addAttribute("pets", petService.listarDoTutor(tutor.getId()));
            model.addAttribute("tipos", TipoAlerta.values());
            model.addAttribute("acao", "/tutor/alertas");
            model.addAttribute("titulo", "Novo lembrete");
            return "alertas/form";
        }
        try {
            alertaService.criarComoTutor(tutor.getId(), form);
            redirectAttributes.addFlashAttribute("sucesso", "Lembrete criado.");
        } catch (RecursoNaoEncontradoException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/tutor/alertas";
    }

    @PostMapping("/tutor/alertas/{id}/confirmar")
    public String confirmar(@AuthenticationPrincipal ZeloUserPrincipal principal, @PathVariable Long id,
                             RedirectAttributes redirectAttributes) {
        Tutor tutor = contexto.tutorAtual(principal);
        try {
            alertaService.confirmar(id, tutor.getId());
            redirectAttributes.addFlashAttribute("sucesso", "Cuidado confirmado. Continue assim!");
        } catch (RecursoNaoEncontradoException | RegraNegocioException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/tutor/alertas";
    }

    @PostMapping("/tutor/alertas/{id}/cancelar")
    public String cancelarPeloTutor(@AuthenticationPrincipal ZeloUserPrincipal principal, @PathVariable Long id,
                                     RedirectAttributes redirectAttributes) {
        Tutor tutor = contexto.tutorAtual(principal);
        try {
            alertaService.cancelarPeloTutor(id, tutor.getId());
            redirectAttributes.addFlashAttribute("sucesso", "Alerta cancelado.");
        } catch (RecursoNaoEncontradoException | RegraNegocioException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/tutor/alertas";
    }

    // -------------------------------------------------------------- Clinica
    @GetMapping("/clinica/alertas")
    public String listaClinica(@AuthenticationPrincipal ZeloUserPrincipal principal, Model model) {
        Clinica clinica = contexto.clinicaAtual(principal);
        model.addAttribute("alertas", alertaService.daClinica(clinica.getId()));
        return "alertas/clinica-lista";
    }

    @GetMapping("/clinica/alertas/novo")
    public String novoComoClinica(Model model) {
        model.addAttribute("form", new AlertaForm());
        model.addAttribute("pets", petService.listarTodos());
        model.addAttribute("tipos", TipoAlerta.values());
        model.addAttribute("acao", "/clinica/alertas");
        model.addAttribute("titulo", "Novo alerta clinico");
        return "alertas/form";
    }

    @PostMapping("/clinica/alertas")
    public String criarComoClinica(@AuthenticationPrincipal ZeloUserPrincipal principal,
                                    @Valid @ModelAttribute("form") AlertaForm form, BindingResult bindingResult,
                                    Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("pets", petService.listarTodos());
            model.addAttribute("tipos", TipoAlerta.values());
            model.addAttribute("acao", "/clinica/alertas");
            model.addAttribute("titulo", "Novo alerta clinico");
            return "alertas/form";
        }
        Clinica clinica = contexto.clinicaAtual(principal);
        try {
            alertaService.criarComoClinica(clinica.getId(), form);
            redirectAttributes.addFlashAttribute("sucesso", "Alerta criado para o tutor.");
        } catch (RecursoNaoEncontradoException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/clinica/alertas";
    }

    @PostMapping("/clinica/alertas/{id}/enviar")
    public String enviar(@AuthenticationPrincipal ZeloUserPrincipal principal, @PathVariable Long id,
                          RedirectAttributes redirectAttributes) {
        Clinica clinica = contexto.clinicaAtual(principal);
        try {
            alertaService.marcarComoEnviado(id, clinica.getId());
            redirectAttributes.addFlashAttribute("sucesso", "Notificacao enviada ao tutor.");
        } catch (RecursoNaoEncontradoException | RegraNegocioException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/clinica/alertas";
    }

    @PostMapping("/clinica/alertas/{id}/cancelar")
    public String cancelarPelaClinica(@AuthenticationPrincipal ZeloUserPrincipal principal, @PathVariable Long id,
                                       RedirectAttributes redirectAttributes) {
        Clinica clinica = contexto.clinicaAtual(principal);
        try {
            alertaService.cancelarPelaClinica(id, clinica.getId());
            redirectAttributes.addFlashAttribute("sucesso", "Alerta cancelado.");
        } catch (RecursoNaoEncontradoException | RegraNegocioException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/clinica/alertas";
    }
}
