package br.com.fiap.zelo.web.api;

import br.com.fiap.zelo.domain.Usuario;
import br.com.fiap.zelo.repository.TutorRepository;
import br.com.fiap.zelo.security.TokenApiService;
import br.com.fiap.zelo.security.ZeloUserPrincipal;
import br.com.fiap.zelo.service.CadastroService;
import br.com.fiap.zelo.web.dto.RegistroTutorForm;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class ApiAuthController {

    private final AuthenticationManager authenticationManager;
    private final TokenApiService tokenApiService;
    private final CadastroService cadastroService;
    private final TutorRepository tutorRepository;

    public ApiAuthController(AuthenticationManager authenticationManager, TokenApiService tokenApiService,
                             CadastroService cadastroService, TutorRepository tutorRepository) {
        this.authenticationManager = authenticationManager;
        this.tokenApiService = tokenApiService;
        this.cadastroService = cadastroService;
        this.tutorRepository = tutorRepository;
    }

    @PostMapping("/login")
    public ApiModels.AuthResponse login(@Valid @RequestBody ApiModels.LoginRequest request) {
        var autenticacao = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(request.email().trim(), request.senha()));
        return autenticar((ZeloUserPrincipal) autenticacao.getPrincipal());
    }

    @PostMapping("/register")
    public ApiModels.AuthResponse registrar(@Valid @RequestBody RegistroTutorForm form) {
        return autenticar(new ZeloUserPrincipal(cadastroService.registrarTutor(form)));
    }

    @GetMapping("/me")
    public ApiModels.UserResponse usuarioAtual(@AuthenticationPrincipal ZeloUserPrincipal principal) {
        return usuario(principal.getUsuario());
    }

    private ApiModels.AuthResponse autenticar(ZeloUserPrincipal principal) {
        return new ApiModels.AuthResponse(tokenApiService.emitir(principal), usuario(principal.getUsuario()));
    }

    private ApiModels.UserResponse usuario(Usuario usuario) {
        return ApiModels.UserResponse.de(usuario, tutorRepository.findByUsuarioId(usuario.getId()).orElse(null));
    }
}
