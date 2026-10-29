package br.com.fiap.zelo.web.controller;

import br.com.fiap.zelo.domain.Clinica;
import br.com.fiap.zelo.security.ContextoAtualService;
import br.com.fiap.zelo.security.ZeloUserPrincipal;
import br.com.fiap.zelo.service.EquipeService;
import br.com.fiap.zelo.web.dto.MembroEquipeForm;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Área exclusiva do GESTOR: cadastro e controle de acesso de veterinários e gestores da clínica. */
@Controller
@RequestMapping("/clinica/gerenciar/equipe")
public class EquipeController {

    private static final String PAGINA = "/clinica/gerenciar/equipe";

    private final EquipeService equipeService;
    private final ContextoAtualService contexto;

    public EquipeController(EquipeService equipeService, ContextoAtualService contexto) {
        this.equipeService = equipeService;
        this.contexto = contexto;
    }

    @GetMapping
    public String listar(@AuthenticationPrincipal ZeloUserPrincipal principal, Model model) {
        return exibir(principal, model, new MembroEquipeForm());
    }

    @PostMapping
    public String adicionar(@AuthenticationPrincipal ZeloUserPrincipal principal,
                            @Valid @ModelAttribute("form") MembroEquipeForm form, BindingResult validacao, Model model,
                            RedirectAttributes atributos) {
        return Formulario.processar(validacao, () -> {
            var membro = equipeService.adicionarMembro(contexto.clinicaIdAtual(principal), form);
            atributos.addFlashAttribute("sucesso", membro.getNome() + " entrou na equipe como "
                    + membro.getTipoUsuario().getRotulo().toLowerCase() + ". Compartilhe o e-mail e a senha inicial.");
            return "redirect:" + PAGINA;
        }, () -> exibir(principal, model, form));
    }

    @PostMapping("/{membroId}/alternar")
    public String alternarAcesso(@AuthenticationPrincipal ZeloUserPrincipal principal, @PathVariable Long membroId,
                                 RedirectAttributes atributos) {
        return Feedback.executar(atributos, PAGINA, () -> {
            var membro = equipeService.alternarAcesso(contexto.clinicaIdAtual(principal), membroId,
                    principal.getUsuarioId());
            return membro.isAtivo() ? "Acesso de " + membro.getNome() + " reativado."
                    : "Acesso de " + membro.getNome() + " desativado.";
        });
    }

    private String exibir(ZeloUserPrincipal principal, Model model, MembroEquipeForm form) {
        Clinica clinica = contexto.clinicaAtual(principal);
        model.addAttribute("clinica", clinica);
        model.addAttribute("membros", equipeService.listarMembros(clinica.getId()));
        model.addAttribute("form", form);
        return "clinicas/equipe";
    }
}
