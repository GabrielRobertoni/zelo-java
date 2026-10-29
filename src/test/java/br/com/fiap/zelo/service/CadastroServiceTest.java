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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CadastroServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private TutorRepository tutorRepository;
    @Mock
    private ClinicaService clinicaService;

    private CadastroService cadastroService;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @BeforeEach
    void setUp() {
        cadastroService = new CadastroService(usuarioRepository, tutorRepository, clinicaService, encoder);
    }

    private <T extends DadosAcessoForm> T preencher(T form, String email) {
        form.setNome("  Maria Silva ");
        form.setEmail(email);
        form.setSenha("segredo123");
        form.setConfirmarSenha("segredo123");
        return form;
    }

    private void salvarUsuarioDevolvendoOMesmo() {
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocacao -> invocacao.getArgument(0));
    }

    @Test
    void tutorTemSenhaCriptografadaEEmailNormalizado() {
        salvarUsuarioDevolvendoOMesmo();
        RegistroTutorForm form = preencher(new RegistroTutorForm(), " Maria@Exemplo.com ");
        form.setEstado("sp");

        Usuario usuario = cadastroService.registrarTutor(form);

        assertThat(usuario.getTipoUsuario()).isEqualTo(TipoUsuario.TUTOR);
        assertThat(usuario.getEmail()).isEqualTo("maria@exemplo.com");
        assertThat(usuario.getNome()).isEqualTo("Maria Silva");
        assertThat(encoder.matches("segredo123", usuario.getSenhaHash())).isTrue();
        ArgumentCaptor<Tutor> tutor = ArgumentCaptor.forClass(Tutor.class);
        verify(tutorRepository).save(tutor.capture());
        assertThat(tutor.getValue().getEstado()).isEqualTo("SP");
    }

    @Test
    void gestorCriaAClinicaEEntraNaEquipe() {
        salvarUsuarioDevolvendoOMesmo();
        Clinica clinica = Clinica.builder().id(1L).build();
        RegistroGestorForm form = preencher(new RegistroGestorForm(), "gestora@exemplo.com");
        when(clinicaService.criar(form.getClinica())).thenReturn(clinica);

        Usuario gestor = cadastroService.registrarGestor(form);

        assertThat(gestor.getTipoUsuario()).isEqualTo(TipoUsuario.GESTOR);
        assertThat(clinica.getEquipe()).containsExactly(gestor);
    }

    @Test
    void veterinarioEntraNaClinicaDoCodigoDeConvite() {
        salvarUsuarioDevolvendoOMesmo();
        Clinica clinica = Clinica.builder().id(1L).build();
        RegistroVeterinarioForm form = preencher(new RegistroVeterinarioForm(), "vet@exemplo.com");
        form.setCodigoConvite("amiga2026");
        when(clinicaService.buscarPorConvite("amiga2026")).thenReturn(clinica);

        Usuario veterinario = cadastroService.registrarVeterinario(form);

        assertThat(veterinario.getTipoUsuario()).isEqualTo(TipoUsuario.VETERINARIO);
        assertThat(clinica.getEquipe()).containsExactly(veterinario);
    }

    @Test
    void codigoDeConviteInvalidoNaoCriaUsuario() {
        RegistroVeterinarioForm form = preencher(new RegistroVeterinarioForm(), "vet@exemplo.com");
        form.setCodigoConvite("ERRADO");
        when(clinicaService.buscarPorConvite("ERRADO")).thenThrow(new RegraNegocioException("Código inválido"));

        assertThatThrownBy(() -> cadastroService.registrarVeterinario(form)).isInstanceOf(RegraNegocioException.class);
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void emailJaCadastradoEhRecusado() {
        when(usuarioRepository.existsByEmailIgnoreCase("tutor@zelo.com.br")).thenReturn(true);

        assertThatThrownBy(() -> cadastroService.registrarTutor(preencher(new RegistroTutorForm(), "tutor@zelo.com.br")))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("e-mail");
        verify(usuarioRepository, never()).save(any());
    }
}
