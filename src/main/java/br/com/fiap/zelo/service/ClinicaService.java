package br.com.fiap.zelo.service;

import br.com.fiap.zelo.domain.Clinica;
import br.com.fiap.zelo.domain.CodigoConvite;
import br.com.fiap.zelo.exception.RecursoNaoEncontradoException;
import br.com.fiap.zelo.exception.RegraNegocioException;
import br.com.fiap.zelo.repository.ClinicaRepository;
import br.com.fiap.zelo.web.dto.ClinicaForm;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ClinicaService {

    private final ClinicaRepository clinicaRepository;

    public ClinicaService(ClinicaRepository clinicaRepository) {
        this.clinicaRepository = clinicaRepository;
    }

    @Transactional(readOnly = true)
    public List<Clinica> listarAtivas() {
        return clinicaRepository.findByAtivaOrderByNomeAsc("S");
    }

    @Transactional(readOnly = true)
    public Clinica obterClinicaDoUsuario(Long usuarioId) {
        return clinicaRepository.findByMembroEquipeId(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Seu usuário não está vinculado a nenhuma clínica."));
    }

    @Transactional(readOnly = true)
    public Clinica buscarAtiva(Long clinicaId) {
        Clinica clinica = clinicaRepository.findById(clinicaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Clínica não encontrada."));
        if (!clinica.isAtiva()) {
            throw new RegraNegocioException("A clínica selecionada não está recebendo triagens no momento.");
        }
        return clinica;
    }

    /** Nova clínica já nasce ativa e com código de convite para a equipe. */
    public Clinica criar(ClinicaForm form) {
        exigirCnpjDisponivel(form.getCnpj(), null);
        Clinica clinica = aplicar(Clinica.builder().build(), form);
        definirConviteUnico(clinica);
        return clinicaRepository.save(clinica);
    }

    public Clinica atualizar(Long clinicaId, ClinicaForm form) {
        exigirCnpjDisponivel(form.getCnpj(), clinicaId);
        return clinicaRepository.save(aplicar(buscar(clinicaId), form));
    }

    /** Alterna ativa/indisponível em vez de excluir, preservando o histórico de triagens e alertas. */
    public Clinica alternarDisponibilidade(Long clinicaId) {
        Clinica clinica = buscar(clinicaId);
        clinica.alternarDisponibilidade();
        return clinicaRepository.save(clinica);
    }

    public Clinica renovarConvite(Long clinicaId) {
        Clinica clinica = buscar(clinicaId);
        definirConviteUnico(clinica);
        return clinicaRepository.save(clinica);
    }

    @Transactional(readOnly = true)
    public Clinica buscarPorConvite(String codigoConvite) {
        return clinicaRepository.findByCodigoConviteIgnoreCase(codigoConvite.trim())
                .orElseThrow(() -> new RegraNegocioException(
                        "Código de convite inválido. Peça o código atualizado ao gestor da clínica."));
    }

    private Clinica buscar(Long clinicaId) {
        return clinicaRepository.findById(clinicaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Clínica não encontrada."));
    }

    /** O candidato só é atribuído depois de conferido; assim a consulta nunca encontra a própria clínica. */
    private void definirConviteUnico(Clinica clinica) {
        String codigo;
        do {
            codigo = CodigoConvite.gerar();
        } while (clinicaRepository.existsByCodigoConvite(codigo));
        clinica.setCodigoConvite(codigo);
    }

    private void exigirCnpjDisponivel(String cnpj, Long clinicaId) {
        String cnpjNormalizado = Normalizador.textoOuNulo(cnpj);
        if (cnpjNormalizado == null) {
            return;
        }
        boolean emUso = clinicaId == null
                ? clinicaRepository.existsByCnpj(cnpjNormalizado)
                : clinicaRepository.existsByCnpjAndIdNot(cnpjNormalizado, clinicaId);
        if (emUso) {
            throw new RegraNegocioException("Já existe uma clínica cadastrada com este CNPJ.");
        }
    }

    private Clinica aplicar(Clinica clinica, ClinicaForm form) {
        clinica.setNome(form.getNome().trim());
        clinica.setCnpj(Normalizador.textoOuNulo(form.getCnpj()));
        clinica.setTelefone(Normalizador.textoOuNulo(form.getTelefone()));
        clinica.setEndereco(Normalizador.textoOuNulo(form.getEndereco()));
        clinica.setCidade(Normalizador.textoOuNulo(form.getCidade()));
        clinica.setEstado(Normalizador.uf(form.getEstado()));
        return clinica;
    }
}
