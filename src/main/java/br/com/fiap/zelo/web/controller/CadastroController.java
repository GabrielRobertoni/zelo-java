package br.com.fiap.zelo.web.controller;

import br.com.fiap.zelo.domain.Usuario;
import br.com.fiap.zelo.service.CadastroService;
import br.com.fiap.zelo.web.dto.RegistroGestorForm;
import br.com.fiap.zelo.web.dto.RegistroTutorForm;
import br.com.fiap.zelo.web.dto.RegistroVeterinarioForm;
import br.com.fiap.zelo.web.suporte.Formulario;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Cadastro público dos três perfis: tutor, gestor (com a clínica) e veterinário (com código de convite). */
@Controller
@RequestMapping("/registro")
public class CadastroController {

    private final CadastroService cadastroService;

    public CadastroController(CadastroService cadastroService) {
        this.cadastroService = cadastroService;
    }

    @GetMapping
    public String escolherPerfil() {
        return "cadastro/escolha";
    }

    @GetMapping("/tutor")
    public String formularioTutor(Model model) {
        model.addAttribute("form", new RegistroTutorForm());
        return "cadastro/tutor";
    }

    @PostMapping("/tutor")
    public String registrarTutor(@Valid @ModelAttribute("form") RegistroTutorForm form, BindingResult validacao,
                                 RedirectAttributes atributos) {
        return Formulario.processar(validacao,
                () -> concluir(atributos, cadastroService.registrarTutor(form), "Conta de tutor criada!"),
                () -> "cadastro/tutor");
    }

    @GetMapping("/gestor")
    public String formularioGestor(Model model) {
        model.addAttribute("form", new RegistroGestorForm());
        return "cadastro/gestor";
    }

    @PostMapping("/gestor")
    public String registrarGestor(@Valid @ModelAttribute("form") RegistroGestorForm form, BindingResult validacao,
                                  RedirectAttributes atributos) {
        return Formulario.processar(validacao,
                () -> concluir(atributos, cadastroService.registrarGestor(form),
                        "Cadastro concluído: " + form.getClinica().getNome().trim() + " foi criada com você como gestor."),
                () -> "cadastro/gestor");
    }

    @GetMapping("/veterinario")
    public String formularioVeterinario(Model model) {
        model.addAttribute("form", new RegistroVeterinarioForm());
        return "cadastro/veterinario";
    }

    @PostMapping("/veterinario")
    public String registrarVeterinario(@Valid @ModelAttribute("form") RegistroVeterinarioForm form,
                                       BindingResult validacao, RedirectAttributes atributos) {
        return Formulario.processar(validacao,
                () -> concluir(atributos, cadastroService.registrarVeterinario(form),
                        "Conta de veterinário criada e vinculada à clínica!"),
                () -> "cadastro/veterinario");
    }

    private String concluir(RedirectAttributes atributos, Usuario usuario, String mensagem) {
        atributos.addFlashAttribute("sucesso", mensagem + " Entre com seu e-mail e senha.");
        atributos.addFlashAttribute("emailCadastrado", usuario.getEmail());
        return "redirect:/login";
    }
}
