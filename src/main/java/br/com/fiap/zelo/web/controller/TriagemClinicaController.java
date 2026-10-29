package br.com.fiap.zelo.web.controller;

import br.com.fiap.zelo.security.ContextoAtualService;
import br.com.fiap.zelo.security.ZeloUserPrincipal;
import br.com.fiap.zelo.service.TriagemService;
import br.com.fiap.zelo.web.suporte.Feedback;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/clinica/triagens")
public class TriagemClinicaController {

    private static final String FILA = "/clinica/triagens";

    private final TriagemService triagemService;
    private final ContextoAtualService contexto;

    public TriagemClinicaController(TriagemService triagemService, ContextoAtualService contexto) {
        this.triagemService = triagemService;
        this.contexto = contexto;
    }

    @GetMapping
    public String listar(@AuthenticationPrincipal ZeloUserPrincipal principal,
                         @RequestParam(defaultValue = "fila") String aba, Model model) {
        Long clinicaId = contexto.clinicaIdAtual(principal);
        boolean historico = "historico".equals(aba);
        model.addAttribute("aba", historico ? "historico" : "fila");
        model.addAttribute("triagens", historico
                ? triagemService.historicoDaClinica(clinicaId)
                : triagemService.filaDeAtencao(clinicaId));
        return "triagens/fila";
    }

    @PostMapping("/{id}/encaminhar")
    public String encaminhar(@AuthenticationPrincipal ZeloUserPrincipal principal, @PathVariable Long id,
                             @RequestParam(required = false) String voltarPara, RedirectAttributes atributos) {
        return Feedback.executar(atributos, Feedback.destinoSeguro(voltarPara, FILA),
                "Triagem encaminhada para atendimento.",
                () -> triagemService.encaminhar(id, contexto.clinicaIdAtual(principal)));
    }

    @PostMapping("/{id}/concluir")
    public String concluir(@AuthenticationPrincipal ZeloUserPrincipal principal, @PathVariable Long id,
                           @RequestParam(required = false) String voltarPara, RedirectAttributes atributos) {
        return Feedback.executar(atributos, Feedback.destinoSeguro(voltarPara, FILA),
                "Atendimento concluído.",
                () -> triagemService.concluir(id, contexto.clinicaIdAtual(principal)));
    }

    @PostMapping("/{id}/cancelar")
    public String cancelar(@AuthenticationPrincipal ZeloUserPrincipal principal, @PathVariable Long id,
                           @RequestParam(required = false) String voltarPara, RedirectAttributes atributos) {
        return Feedback.executar(atributos, Feedback.destinoSeguro(voltarPara, FILA), "Triagem cancelada.",
                () -> triagemService.cancelarPelaClinica(id, contexto.clinicaIdAtual(principal)));
    }
}
