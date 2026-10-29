package br.com.fiap.zelo.service;

import br.com.fiap.zelo.domain.Clinica;
import br.com.fiap.zelo.domain.Usuario;
import br.com.fiap.zelo.exception.RecursoNaoEncontradoException;
import br.com.fiap.zelo.exception.RegraNegocioException;
import br.com.fiap.zelo.repository.ClinicaRepository;
import br.com.fiap.zelo.repository.UsuarioRepository;
import br.com.fiap.zelo.web.dto.MembroEquipeForm;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

/** Administração da equipe da clínica pelo GESTOR. */
@Service
@Transactional
public class EquipeService {

    private final ClinicaRepository clinicaRepository;
    private final UsuarioRepository usuarioRepository;
    private final CadastroService cadastroService;

    public EquipeService(ClinicaRepository clinicaRepository, UsuarioRepository usuarioRepository,
                         CadastroService cadastroService) {
        this.clinicaRepository = clinicaRepository;
        this.usuarioRepository = usuarioRepository;
        this.cadastroService = cadastroService;
    }

    @Transactional(readOnly = true)
    public List<Usuario> listarMembros(Long clinicaId) {
        return buscarComEquipe(clinicaId).getEquipe().stream()
                .sorted(Comparator.comparing(Usuario::getNome, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    public Usuario adicionarMembro(Long clinicaId, MembroEquipeForm form) {
        if (!form.getTipoUsuario().isEquipeClinica()) {
            throw new RegraNegocioException("A equipe da clínica aceita apenas veterinários e gestores.");
        }
        Clinica clinica = buscarComEquipe(clinicaId);
        Usuario membro = cadastroService.criarUsuario(form, form.getTipoUsuario());
        clinica.adicionarMembro(membro);
        return membro;
    }

    /** Desativar impede o login sem apagar o histórico do profissional. */
    public Usuario alternarAcesso(Long clinicaId, Long membroId, Long gestorId) {
        if (membroId.equals(gestorId)) {
            throw new RegraNegocioException("Você não pode desativar o seu próprio acesso.");
        }
        Usuario membro = buscarComEquipe(clinicaId).getEquipe().stream()
                .filter(usuario -> usuario.getId().equals(membroId))
                .findFirst()
                .orElseThrow(() -> new RecursoNaoEncontradoException("Membro não encontrado na equipe desta clínica."));
        membro.alternarAtivo();
        return usuarioRepository.save(membro);
    }

    private Clinica buscarComEquipe(Long clinicaId) {
        return clinicaRepository.findComEquipe(clinicaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Clínica não encontrada."));
    }
}
