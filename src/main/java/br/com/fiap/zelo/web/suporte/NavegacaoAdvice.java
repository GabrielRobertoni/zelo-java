package br.com.fiap.zelo.web.suporte;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/** Expõe o caminho atual às telas: destaca o menu ativo e permite voltar à mesma página após uma ação. */
@ControllerAdvice(basePackages = "br.com.fiap.zelo.web.controller")
public class NavegacaoAdvice {

    @ModelAttribute("caminhoAtual")
    public String caminhoAtual(HttpServletRequest request) {
        String consulta = request.getQueryString();
        return consulta == null ? request.getRequestURI() : request.getRequestURI() + "?" + consulta;
    }
}
