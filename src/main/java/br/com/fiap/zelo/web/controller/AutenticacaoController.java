package br.com.fiap.zelo.web.controller;

import br.com.fiap.zelo.security.PaginaInicialPorPerfil;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AutenticacaoController {

    @GetMapping("/login")
    public String login(Authentication autenticacao) {
        return PaginaInicialPorPerfil.estaAutenticado(autenticacao)
                ? "redirect:" + PaginaInicialPorPerfil.de(autenticacao)
                : "auth/login";
    }
}
