package br.com.fiap.zelo.service;

import br.com.fiap.zelo.domain.Clinica;
import br.com.fiap.zelo.domain.Tutor;
import br.com.fiap.zelo.domain.Usuario;
import br.com.fiap.zelo.domain.enums.TipoUsuario;
import br.com.fiap.zelo.exception.RegraNegocioException;
import br.com.fiap.zelo.repository.TutorRepository;
import br.com.fiap.zelo.repository.UsuarioRepository;
import br.com.fiap.zelo.web.dto.DadosAcessoForm;
import br.com.fiap.zelo.web.dto.RegistroGestorForm;
import br.com.fiap.zelo.web.dto.RegistroTutorForm;
import br.com.fiap.zelo.web.dto.RegistroVeterinarioForm;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Cadastro público dos três perfis. Todo usuário criado aqui sai pronto para
 * entrar: o tutor com seu perfil, o gestor com a clínica que criou e o
 * veterinário já na equipe da clínica cujo código de convite informou.
 */
@Service
@Transactional
public class CadastroService {

    private final UsuarioRepository usuarioRepository;
    private final TutorRepository tutorRepository;
    private final ClinicaService clinicaService;
    private final PasswordEncoder passwordEncoder;

    public CadastroService(UsuarioRepository usuarioRepository, TutorRepository tutorRepository,
                           ClinicaService clinicaService, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.tutorRepository = tutorRepository;
        this.clinicaService = clinicaService;
        this.passwordEncoder = passwordEncoder;
    }

    public Usuario registrarTutor(RegistroTutorForm form) {
        Usuario usuario = criarUsuario(form, TipoUsuario.TUTOR);
        tutorRepository.save(Tutor.builder()
                .usuario(usuario)
                .telefone(Normalizador.textoOuNulo(form.getTelefone()))
                .cidade(Normalizador.textoOuNulo(form.getCidade()))
                .estado(Normalizador.uf(form.getEstado()))
                .build());
        return usuario;
    }

    public Usuario registrarGestor(RegistroGestorForm form) {
        Usuario gestor = criarUsuario(form, TipoUsuario.GESTOR);
        clinicaService.criar(form.getClinica()).adicionarMembro(gestor);
        return gestor;
    }

    public Usuario registrarVeterinario(RegistroVeterinarioForm form) {
        Clinica clinica = clinicaService.buscarPorConvite(form.getCodigoConvite());
        Usuario veterinario = criarUsuario(form, TipoUsuario.VETERINARIO);
        clinica.adicionarMembro(veterinario);
        return veterinario;
    }

    /** Também usado pelo gestor ao cadastrar membros da equipe. */
    public Usuario criarUsuario(DadosAcessoForm form, TipoUsuario tipo) {
        if (usuarioRepository.existsByEmailIgnoreCase(form.getEmail().trim())) {
            throw new RegraNegocioException("Já existe uma conta cadastrada com este e-mail.");
        }
        return usuarioRepository.save(
                Usuario.novo(form.getNome(), form.getEmail(), passwordEncoder.encode(form.getSenha()), tipo));
    }
}
