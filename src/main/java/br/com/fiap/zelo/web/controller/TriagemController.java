package br.com.fiap.zelo.web.controller;

import br.com.fiap.zelo.domain.Clinica;
import br.com.fiap.zelo.domain.Tutor;
import br.com.fiap.zelo.domain.enums.CanalTriagem;
import br.com.fiap.zelo.exception.RecursoNaoEncontradoException;
import br.com.fiap.zelo.exception.RegraNegocioException;
import br.com.fiap.zelo.security.ContextoAtualService;
import br.com.fiap.zelo.security.ZeloUserPrincipal;
import br.com.fiap.zelo.service.ClinicaService;
import br.com.fiap.zelo.service.PetService;
import br.com.fiap.zelo.service.TriagemService;
import br.com.fiap.zelo.web.dto.TriagemForm;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class TriagemController {

    private final TriagemService triagemService;
    private final PetService petService;
    private final ClinicaService clinicaService;
    private final ContextoAtualService contexto;

    public TriagemController(TriagemService triagemService, PetService petService,
                              ClinicaService clinicaService, ContextoAtualService contexto) {
        this.triagemService = triagemService;
        this.petService = petService;
        this.clinicaService = clinicaService;
        this.contexto = contexto;
    }

    // ---------------------------------------------------------------- Tutor
    @GetMapping("/tutor/triagens")
    public String historico(@AuthenticationPrincipal ZeloUserPrincipal principal, Model model) {
        Tutor tutor = contexto.tutorAtual(principal);
        model.addAttribute("triagens", triagemService.historicoDoTutor(tutor.getId()));
        return "triagens/historico";
    }

    @GetMapping("/tutor/triagens/nova")
    public String nova(@AuthenticationPrincipal ZeloUserPrincipal principal, Model model) {
        Tutor tutor = contexto.tutorAtual(principal);
        model.addAttribute("form", new TriagemForm());
        model.addAttribute("pets", petService.listarDoTutor(tutor.getId()));
        model.addAttribute("clinicas", clinicaService.listarAtivas());
        model.addAttribute("canais", CanalTriagem.values());
        return "triagens/nova";
    }

    @PostMapping("/tutor/triagens")
    public String abrir(@AuthenticationPrincipal ZeloUserPrincipal principal,
                         @Valid @ModelAttribute("form") TriagemForm form, BindingResult bindingResult,
                         Model model, RedirectAttributes redirectAttributes) {
        Tutor tutor = contexto.tutorAtual(principal);
        if (bindingResult.hasErrors()) {
            model.addAttribute("pets", petService.listarDoTutor(tutor.getId()));
            model.addAttribute("clinicas", clinicaService.listarAtivas());
            model.addAttribute("canais", CanalTriagem.values());
            return "triagens/nova";
        }
        try {
            triagemService.abrirTriagem(tutor.getId(), form);
            redirectAttributes.addFlashAttribute("sucesso",
                    "Triagem registrada! A clinica ja recebeu seu relato classificado por urgencia.");
        } catch (RecursoNaoEncontradoException | RegraNegocioException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/tutor/triagens";
    }

    @PostMapping("/tutor/triagens/{id}/cancelar")
    public String cancelarPeloTutor(@AuthenticationPrincipal ZeloUserPrincipal principal, @PathVariable Long id,
                                     RedirectAttributes redirectAttributes) {
        Tutor tutor = contexto.tutorAtual(principal);
        try {
            triagemService.cancelarPeloTutor(id, tutor.getId());
            redirectAttributes.addFlashAttribute("sucesso", "Triagem cancelada.");
        } catch (RecursoNaoEncontradoException | RegraNegocioException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/tutor/triagens";
    }

    // -------------------------------------------------------------- Clinica
    @GetMapping("/clinica/triagens")
    public String fila(@AuthenticationPrincipal ZeloUserPrincipal principal, Model model) {
        Clinica clinica = contexto.clinicaAtual(principal);
        model.addAttribute("triagens", triagemService.filaDeAtencao(clinica.getId()));
        return "triagens/fila";
    }

    @PostMapping("/clinica/triagens/{id}/encaminhar")
    public String encaminhar(@AuthenticationPrincipal ZeloUserPrincipal principal, @PathVariable Long id,
                              RedirectAttributes redirectAttributes) {
        Clinica clinica = contexto.clinicaAtual(principal);
        try {
            triagemService.encaminhar(id, clinica.getId());
            redirectAttributes.addFlashAttribute("sucesso", "Triagem encaminhada para atendimento.");
        } catch (RecursoNaoEncontradoException | RegraNegocioException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/clinica/triagens";
    }

    @PostMapping("/clinica/triagens/{id}/concluir")
    public String concluir(@AuthenticationPrincipal ZeloUserPrincipal principal, @PathVariable Long id,
                            RedirectAttributes redirectAttributes) {
        Clinica clinica = contexto.clinicaAtual(principal);
        try {
            triagemService.concluir(id, clinica.getId());
            redirectAttributes.addFlashAttribute("sucesso", "Triagem concluida.");
        } catch (RecursoNaoEncontradoException | RegraNegocioException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/clinica/triagens";
    }

    @PostMapping("/clinica/triagens/{id}/cancelar")
    public String cancelarPelaClinica(@AuthenticationPrincipal ZeloUserPrincipal principal, @PathVariable Long id,
                                       RedirectAttributes redirectAttributes) {
        Clinica clinica = contexto.clinicaAtual(principal);
        try {
            triagemService.cancelarPelaClinica(id, clinica.getId());
            redirectAttributes.addFlashAttribute("sucesso", "Triagem cancelada.");
        } catch (RecursoNaoEncontradoException | RegraNegocioException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/clinica/triagens";
    }
}
