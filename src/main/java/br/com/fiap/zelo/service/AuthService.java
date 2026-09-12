package br.com.fiap.zelo.service;

import br.com.fiap.zelo.domain.Tutor;
import br.com.fiap.zelo.domain.Usuario;
import br.com.fiap.zelo.domain.enums.TipoUsuario;
import br.com.fiap.zelo.exception.RegraNegocioException;
import br.com.fiap.zelo.repository.TutorRepository;
import br.com.fiap.zelo.repository.UsuarioRepository;
import br.com.fiap.zelo.web.dto.RegistroTutorForm;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final TutorRepository tutorRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UsuarioRepository usuarioRepository, TutorRepository tutorRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.tutorRepository = tutorRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Autoatendimento de cadastro: qualquer visitante pode se registrar como
     * TUTOR. Perfis de clinica (VETERINARIO/GESTOR) sao provisionados via
     * massa de dados (V2__seed_data.sql) ou por um GESTOR ja existente -
     * nao ha auto-cadastro publico para esses perfis, para evitar que
     * qualquer pessoa se autopromova a integrante de uma clinica.
     */
    public Usuario registrarTutor(RegistroTutorForm form) {
        if (usuarioRepository.existsByEmailIgnoreCase(form.getEmail())) {
            throw new RegraNegocioException("Ja existe uma conta cadastrada com este e-mail.");
        }

        Usuario usuario = Usuario.builder()
                .nome(form.getNome())
                .email(form.getEmail().toLowerCase())
                .senhaHash(passwordEncoder.encode(form.getSenha()))
                .tipoUsuario(TipoUsuario.TUTOR)
                .ativo("S")
                .build();
        usuario = usuarioRepository.save(usuario);

        Tutor tutor = Tutor.builder()
                .usuario(usuario)
                .telefone(form.getTelefone())
                .cidade(form.getCidade())
                .estado(form.getEstado())
                .build();
        tutorRepository.save(tutor);

        return usuario;
    }
}
