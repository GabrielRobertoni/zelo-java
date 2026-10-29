package br.com.fiap.zelo.web.suporte;

import br.com.fiap.zelo.domain.enums.Especie;
import br.com.fiap.zelo.domain.enums.Sexo;
import br.com.fiap.zelo.domain.enums.TipoAlerta;
import br.com.fiap.zelo.domain.enums.TipoUsuario;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

/** Opções fixas dos selects, disponíveis para todas as telas sem repetição nos controllers. */
@ControllerAdvice(basePackages = "br.com.fiap.zelo.web.controller")
public class OpcoesFormularioAdvice {

    @ModelAttribute("especies")
    public Especie[] especies() {
        return Especie.values();
    }

    @ModelAttribute("sexos")
    public Sexo[] sexos() {
        return Sexo.values();
    }

    @ModelAttribute("tiposAlerta")
    public TipoAlerta[] tiposAlerta() {
        return TipoAlerta.values();
    }

    @ModelAttribute("perfisEquipe")
    public List<TipoUsuario> perfisEquipe() {
        return List.of(TipoUsuario.VETERINARIO, TipoUsuario.GESTOR);
    }
}
