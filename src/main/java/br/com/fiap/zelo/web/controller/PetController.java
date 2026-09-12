package br.com.fiap.zelo.web.controller;

import br.com.fiap.zelo.domain.Pet;
import br.com.fiap.zelo.domain.Tutor;
import br.com.fiap.zelo.domain.enums.Especie;
import br.com.fiap.zelo.domain.enums.Sexo;
import br.com.fiap.zelo.exception.RecursoNaoEncontradoException;
import br.com.fiap.zelo.security.ContextoAtualService;
import br.com.fiap.zelo.security.ZeloUserPrincipal;
import br.com.fiap.zelo.service.PetService;
import br.com.fiap.zelo.web.dto.PetForm;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/tutor/pets")
public class PetController {

    private final PetService petService;
    private final ContextoAtualService contexto;

    public PetController(PetService petService, ContextoAtualService contexto) {
        this.petService = petService;
        this.contexto = contexto;
    }

    @GetMapping
    public String listar(@AuthenticationPrincipal ZeloUserPrincipal principal, Model model) {
        Tutor tutor = contexto.tutorAtual(principal);
        model.addAttribute("pets", petService.listarDoTutor(tutor.getId()));
        return "pets/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("form", new PetForm());
        model.addAttribute("especies", Especie.values());
        model.addAttribute("sexos", Sexo.values());
        model.addAttribute("acao", "/tutor/pets");
        model.addAttribute("titulo", "Cadastrar pet");
        return "pets/form";
    }

    @PostMapping
    public String cadastrar(@AuthenticationPrincipal ZeloUserPrincipal principal,
                             @Valid @ModelAttribute("form") PetForm form, BindingResult bindingResult,
                             Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("especies", Especie.values());
            model.addAttribute("sexos", Sexo.values());
            model.addAttribute("acao", "/tutor/pets");
            model.addAttribute("titulo", "Cadastrar pet");
            return "pets/form";
        }
        Tutor tutor = contexto.tutorAtual(principal);
        petService.cadastrar(tutor.getId(), form);
        redirectAttributes.addFlashAttribute("sucesso", "Pet cadastrado com sucesso.");
        return "redirect:/tutor/pets";
    }

    @GetMapping("/{id}/editar")
    public String editar(@AuthenticationPrincipal ZeloUserPrincipal principal, @PathVariable Long id, Model model) {
        Tutor tutor = contexto.tutorAtual(principal);
        Pet pet = petService.buscarDoTutor(id, tutor.getId());

        PetForm form = new PetForm();
        form.setNome(pet.getNome());
        form.setEspecie(pet.getEspecie());
        form.setRaca(pet.getRaca());
        form.setSexo(pet.getSexo());
        form.setDataNascimento(pet.getDataNascimento());
        form.setPesoKg(pet.getPesoKg());
        form.setCastrado(pet.isCastrado());

        model.addAttribute("form", form);
        model.addAttribute("especies", Especie.values());
        model.addAttribute("sexos", Sexo.values());
        model.addAttribute("acao", "/tutor/pets/" + id);
        model.addAttribute("titulo", "Editar " + pet.getNome());
        return "pets/form";
    }

    @PostMapping("/{id}")
    public String atualizar(@AuthenticationPrincipal ZeloUserPrincipal principal, @PathVariable Long id,
                             @Valid @ModelAttribute("form") PetForm form, BindingResult bindingResult,
                             Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("especies", Especie.values());
            model.addAttribute("sexos", Sexo.values());
            model.addAttribute("acao", "/tutor/pets/" + id);
            model.addAttribute("titulo", "Editar pet");
            return "pets/form";
        }
        Tutor tutor = contexto.tutorAtual(principal);
        petService.atualizar(id, tutor.getId(), form);
        redirectAttributes.addFlashAttribute("sucesso", "Dados do pet atualizados.");
        return "redirect:/tutor/pets";
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@AuthenticationPrincipal ZeloUserPrincipal principal, @PathVariable Long id,
                           RedirectAttributes redirectAttributes) {
        Tutor tutor = contexto.tutorAtual(principal);
        try {
            petService.excluir(id, tutor.getId());
            redirectAttributes.addFlashAttribute("sucesso", "Pet removido.");
        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("erro",
                    "Nao e possivel excluir este pet: ja existem triagens ou alertas vinculados a ele.");
        } catch (RecursoNaoEncontradoException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/tutor/pets";
    }
}
