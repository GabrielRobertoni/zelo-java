package br.com.fiap.zelo.service;

import br.com.fiap.zelo.domain.Clinica;
import br.com.fiap.zelo.domain.Usuario;
import br.com.fiap.zelo.domain.enums.TipoUsuario;
import br.com.fiap.zelo.exception.RecursoNaoEncontradoException;
import br.com.fiap.zelo.exception.RegraNegocioException;
import br.com.fiap.zelo.repository.ClinicaRepository;
import br.com.fiap.zelo.repository.UsuarioRepository;
import br.com.fiap.zelo.web.dto.MembroEquipeForm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EquipeServiceTest {

    @Mock
    private ClinicaRepository clinicaRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private CadastroService cadastroService;

    private EquipeService equipeService;
    private final Usuario gestor = Usuario.builder().id(1L).nome("Ana").tipoUsuario(TipoUsuario.GESTOR).build();
    private final Usuario veterinario = Usuario.builder().id(2L).nome("Bruno").tipoUsuario(TipoUsuario.VETERINARIO).build();
    private Clinica clinica;

    @BeforeEach
    void setUp() {
        equipeService = new EquipeService(clinicaRepository, usuarioRepository, cadastroService);
        clinica = Clinica.builder().id(10L).equipe(new ArrayList<>(List.of(veterinario, gestor))).build();
    }

    @Test
    void listaMembrosEmOrdemAlfabetica() {
        when(clinicaRepository.findComEquipe(10L)).thenReturn(Optional.of(clinica));

        assertThat(equipeService.listarMembros(10L)).extracting(Usuario::getNome).containsExactly("Ana", "Bruno");
    }

    @Test
    void gestorDesativaEReativaAcessoDeMembro() {
        when(clinicaRepository.findComEquipe(10L)).thenReturn(Optional.of(clinica));
        when(usuarioRepository.save(veterinario)).thenReturn(veterinario);

        assertThat(equipeService.alternarAcesso(10L, 2L, 1L).isAtivo()).isFalse();
        assertThat(equipeService.alternarAcesso(10L, 2L, 1L).isAtivo()).isTrue();
    }

    @Test
    void gestorNaoDesativaOProprioAcesso() {
        assertThatThrownBy(() -> equipeService.alternarAcesso(10L, 1L, 1L)).isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void naoAlteraQuemNaoEDaEquipe() {
        when(clinicaRepository.findComEquipe(10L)).thenReturn(Optional.of(clinica));

        assertThatThrownBy(() -> equipeService.alternarAcesso(10L, 99L, 1L)).isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void equipeNaoAceitaPerfilTutor() {
        MembroEquipeForm form = new MembroEquipeForm();
        form.setTipoUsuario(TipoUsuario.TUTOR);

        assertThatThrownBy(() -> equipeService.adicionarMembro(10L, form)).isInstanceOf(RegraNegocioException.class);
        verify(cadastroService, never()).criarUsuario(any(), any());
    }

    @Test
    void novoMembroEntraNaEquipe() {
        MembroEquipeForm form = new MembroEquipeForm();
        Usuario novo = Usuario.builder().id(3L).nome("Carla").tipoUsuario(TipoUsuario.VETERINARIO).build();
        when(clinicaRepository.findComEquipe(10L)).thenReturn(Optional.of(clinica));
        when(cadastroService.criarUsuario(form, TipoUsuario.VETERINARIO)).thenReturn(novo);

        equipeService.adicionarMembro(10L, form);

        assertThat(clinica.getEquipe()).contains(novo);
    }
}
