package br.com.fiap.zelo.web.controller;

import br.com.fiap.zelo.exception.RegraNegocioException;
import br.com.fiap.zelo.service.AuthService;
import br.com.fiap.zelo.web.dto.RegistroTutorForm;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/registro")
    public String formularioRegistro(Model model) {
        model.addAttribute("form", new RegistroTutorForm());
        return "auth/registro";
    }

    @PostMapping("/registro")
    public String registrar(@Valid @ModelAttribute("form") RegistroTutorForm form, BindingResult bindingResult,
                             RedirectAttributes redirectAttributes) {
        if (!form.senhasConferem()) {
            bindingResult.rejectValue("confirmarSenha", "senhas.diferentes", "As senhas informadas nao conferem.");
        }
        if (bindingResult.hasErrors()) {
            return "auth/registro";
        }
        try {
            authService.registrarTutor(form);
        } catch (RegraNegocioException e) {
            bindingResult.rejectValue("email", "email.duplicado", e.getMessage());
            return "auth/registro";
        }
        redirectAttributes.addFlashAttribute("sucesso", "Conta criada com sucesso!");
        return "redirect:/login?registrado";
    }
}
