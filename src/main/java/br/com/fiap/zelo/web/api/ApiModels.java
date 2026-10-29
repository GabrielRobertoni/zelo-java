package br.com.fiap.zelo.web.api;

import br.com.fiap.zelo.domain.Alerta;
import br.com.fiap.zelo.domain.Clinica;
import br.com.fiap.zelo.domain.Pet;
import br.com.fiap.zelo.domain.Triagem;
import br.com.fiap.zelo.domain.Tutor;
import br.com.fiap.zelo.domain.Usuario;
import br.com.fiap.zelo.domain.enums.CanalTriagem;
import br.com.fiap.zelo.domain.enums.Especie;
import br.com.fiap.zelo.domain.enums.Sexo;
import br.com.fiap.zelo.domain.enums.TipoAlerta;
import br.com.fiap.zelo.web.dto.AlertaForm;
import br.com.fiap.zelo.web.dto.TriagemForm;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

/** Contratos JSON da API consumida pelo aplicativo mobile. */
public final class ApiModels {

    private ApiModels() {
    }

    public record LoginRequest(@NotBlank String email, @NotBlank String senha) {
    }

    public record AuthResponse(String token, UserResponse usuario) {
    }

    public record ApiErrorResponse(String message, int status, Map<String, String> errors) {
    }

    public record UserResponse(Long id, String nome, String email, String tipo, String telefone, String cidade,
                               String estado) {
        static UserResponse de(Usuario usuario, Tutor tutor) {
            return new UserResponse(usuario.getId(), usuario.getNome(), usuario.getEmail(),
                    usuario.getTipoUsuario().name(),
                    tutor == null ? null : tutor.getTelefone(),
                    tutor == null ? null : tutor.getCidade(),
                    tutor == null ? null : tutor.getEstado());
        }
    }

    public record PetResponse(Long id, String nome, Especie especie, String raca, Sexo sexo, LocalDate dataNascimento,
                              BigDecimal pesoKg, boolean castrado) {
        static PetResponse de(Pet pet) {
            return new PetResponse(pet.getId(), pet.getNome(), pet.getEspecie(), pet.getRaca(), pet.getSexo(),
                    pet.getDataNascimento(), pet.getPesoKg(), pet.isCastrado());
        }
    }

    /** O aplicativo pode omitir a clínica: a triagem vai para a primeira clínica ativa. */
    public record TriageInput(@NotNull Long petId, Long clinicaId, CanalTriagem canal, String relato) {
        TriagemForm paraFormulario(Long clinicaPadraoId) {
            TriagemForm form = new TriagemForm();
            form.setPetId(petId);
            form.setClinicaId(clinicaId == null ? clinicaPadraoId : clinicaId);
            form.setCanal(canal == null ? CanalTriagem.TEXTO : canal);
            form.setRelato(relato);
            return form;
        }
    }

    public record TriageResponse(Long id, Long petId, String petNome, String clinicaNome, CanalTriagem canal,
                                 String relato, BigDecimal scoreRisco, String urgencia, String analiseVisual,
                                 String status, LocalDateTime criadaEm) {
        static TriageResponse de(Triagem triagem) {
            Clinica clinica = triagem.getClinica();
            return new TriageResponse(triagem.getId(), triagem.getPet().getId(), triagem.getPet().getNome(),
                    clinica == null ? null : clinica.getNome(), triagem.getCanal(), triagem.getRelato(),
                    triagem.getScoreRisco(), triagem.getNivelUrgencia().name(), triagem.getAnaliseVisual(),
                    triagem.getStatusTriagem().name(), triagem.getCriadaEm());
        }
    }

    public record AlertInput(Long petId, TipoAlerta tipo, String titulo, String descricao, LocalDate dataPrevista) {
        AlertaForm paraFormulario() {
            AlertaForm form = new AlertaForm();
            form.setPetId(petId);
            form.setTipoAlerta(tipo);
            form.setTitulo(titulo);
            form.setMensagem(descricao);
            form.setDataPrevista(dataPrevista);
            return form;
        }
    }

    public record AlertResponse(Long id, Long petId, String petNome, String clinicaNome, TipoAlerta tipo,
                                String titulo, String descricao, LocalDate dataPrevista, String status) {
        static AlertResponse de(Alerta alerta) {
            Clinica clinica = alerta.getClinica();
            return new AlertResponse(alerta.getId(), alerta.getPet().getId(), alerta.getPet().getNome(),
                    clinica == null ? null : clinica.getNome(), alerta.getTipoAlerta(), alerta.getTitulo(),
                    alerta.getMensagem(), alerta.getDataPrevista(), alerta.getStatusAlerta().name());
        }
    }

    public record ClinicResponse(Long id, String nome, String cidade, String estado) {
        static ClinicResponse de(Clinica clinica) {
            return new ClinicResponse(clinica.getId(), clinica.getNome(), clinica.getCidade(), clinica.getEstado());
        }
    }
}
