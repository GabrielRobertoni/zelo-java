package br.com.fiap.zelo.web.controller;

import br.com.fiap.zelo.domain.Pet;
import br.com.fiap.zelo.security.ContextoAtualService;
import br.com.fiap.zelo.security.ZeloUserPrincipal;
import br.com.fiap.zelo.service.PetService;
import br.com.fiap.zelo.service.linhadotempo.LinhaDoTempoService;
import br.com.fiap.zelo.web.dto.PetForm;
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

@Controller
@RequestMapping("/tutor/pets")
public class PetController {

    private static final String LISTA = "/tutor/pets";
    private static final String FORMULARIO = "pets/form";

    private final PetService petService;
    private final LinhaDoTempoService linhaDoTempoService;
    private final ContextoAtualService contexto;

    public PetController(PetService petService, LinhaDoTempoService linhaDoTempoService, ContextoAtualService contexto) {
        this.petService = petService;
        this.linhaDoTempoService = linhaDoTempoService;
        this.contexto = contexto;
    }

    @GetMapping
    public String listar(@AuthenticationPrincipal ZeloUserPrincipal principal, Model model) {
        model.addAttribute("pets", petService.listarDoTutor(contexto.tutorIdAtual(principal)));
        return "pets/lista";
    }

    @GetMapping("/{id}")
    public String detalhar(@AuthenticationPrincipal ZeloUserPrincipal principal, @PathVariable Long id, Model model) {
        model.addAttribute("pet", petService.buscarDoTutor(id, contexto.tutorIdAtual(principal)));
        model.addAttribute("eventos", linhaDoTempoService.doPet(id));
        return "pets/detalhe";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        return exibirFormulario(model, new PetForm(), LISTA, "Cadastrar pet");
    }

    @PostMapping
    public String cadastrar(@AuthenticationPrincipal ZeloUserPrincipal principal,
                            @Valid @ModelAttribute("form") PetForm form, BindingResult validacao, Model model,
                            RedirectAttributes atributos) {
        return Formulario.processar(validacao, () -> {
            Pet pet = petService.cadastrar(contexto.tutorIdAtual(principal), form);
            atributos.addFlashAttribute("sucesso", pet.getNome() + " foi cadastrado(a) com sucesso.");
            return "redirect:" + LISTA + "/" + pet.getId();
        }, () -> exibirFormulario(model, form, LISTA, "Cadastrar pet"));
    }

    @GetMapping("/{id}/editar")
    public String editar(@AuthenticationPrincipal ZeloUserPrincipal principal, @PathVariable Long id, Model model) {
        Pet pet = petService.buscarDoTutor(id, contexto.tutorIdAtual(principal));
        return exibirFormulario(model, PetForm.de(pet), LISTA + "/" + id, "Editar " + pet.getNome());
    }

    @PostMapping("/{id}")
    public String atualizar(@AuthenticationPrincipal ZeloUserPrincipal principal, @PathVariable Long id,
                            @Valid @ModelAttribute("form") PetForm form, BindingResult validacao, Model model,
                            RedirectAttributes atributos) {
        return Formulario.processar(validacao, () -> {
            petService.atualizar(id, contexto.tutorIdAtual(principal), form);
            atributos.addFlashAttribute("sucesso", "Dados do pet atualizados.");
            return "redirect:" + LISTA + "/" + id;
        }, () -> exibirFormulario(model, form, LISTA + "/" + id, "Editar pet"));
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@AuthenticationPrincipal ZeloUserPrincipal principal, @PathVariable Long id,
                          RedirectAttributes atributos) {
        return Feedback.executar(atributos, LISTA, "Pet removido.",
                () -> petService.excluir(id, contexto.tutorIdAtual(principal)));
    }

    private String exibirFormulario(Model model, PetForm form, String acao, String titulo) {
        model.addAttribute("form", form);
        model.addAttribute("acao", acao);
        model.addAttribute("titulo", titulo);
        return FORMULARIO;
    }
}
