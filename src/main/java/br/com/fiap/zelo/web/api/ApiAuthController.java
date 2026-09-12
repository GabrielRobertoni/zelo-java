package br.com.fiap.zelo.web.api;

import br.com.fiap.zelo.domain.Usuario;
import br.com.fiap.zelo.repository.TutorRepository;
import br.com.fiap.zelo.security.ApiTokenService;
import br.com.fiap.zelo.security.ZeloUserPrincipal;
import br.com.fiap.zelo.service.AuthService;
import br.com.fiap.zelo.web.dto.RegistroTutorForm;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class ApiAuthController {
    private final AuthenticationManager authenticationManager;
    private final ApiTokenService tokenService;
    private final AuthService authService;
    private final TutorRepository tutorRepository;

    public ApiAuthController(AuthenticationManager authenticationManager, ApiTokenService tokenService,
                             AuthService authService, TutorRepository tutorRepository) {
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
        this.authService = authService;
        this.tutorRepository = tutorRepository;
    }

    @PostMapping("/login")
    public ApiModels.AuthResponse login(@RequestBody LoginRequest request) {
        var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.senha()));
        var principal = (ZeloUserPrincipal) authentication.getPrincipal();
        return new ApiModels.AuthResponse(tokenService.issue(principal.getUsername()), response(principal.getUsuario()));
    }

    @PostMapping("/register")
    public ApiModels.AuthResponse register(@Valid @RequestBody RegistroTutorForm form) {
        if (!form.senhasConferem()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "As senhas informadas nao conferem.");
        Usuario usuario = authService.registrarTutor(form);
        return new ApiModels.AuthResponse(tokenService.issue(usuario.getEmail()), response(usuario));
    }

    @GetMapping("/me")
    public ApiModels.UserResponse me(@AuthenticationPrincipal ZeloUserPrincipal principal) {
        if (principal == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Autenticacao necessaria.");
        return response(principal.getUsuario());
    }

    private ApiModels.UserResponse response(Usuario usuario) {
        var tutor = tutorRepository.findByUsuarioId(usuario.getId()).orElse(null);
        return new ApiModels.UserResponse(usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getTipoUsuario().name(),
                tutor == null ? null : tutor.getTelefone(), tutor == null ? null : tutor.getCidade(), tutor == null ? null : tutor.getEstado());
    }

    public record LoginRequest(String email, String senha) {}
}
