package br.com.fiap.zelo.web.api;

import br.com.fiap.zelo.domain.enums.CanalTriagem;
import br.com.fiap.zelo.domain.enums.Especie;
import br.com.fiap.zelo.domain.enums.Sexo;
import br.com.fiap.zelo.domain.enums.TipoAlerta;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

public final class ApiModels {
    private ApiModels() {}

    public record UserResponse(Long id, String nome, String email, String tipo, String telefone, String cidade, String estado) {}
    public record AuthResponse(String token, UserResponse usuario) {}
    public record ApiErrorResponse(String message, int status, Map<String, String> errors) {}

    public record PetInput(
            @NotBlank @Size(max = 100) String nome,
            @NotNull Especie especie,
            @Size(max = 80) String raca,
            Sexo sexo,
            @PastOrPresent LocalDate dataNascimento,
            @DecimalMin("0.01") @DecimalMax("999.99") BigDecimal pesoKg,
            Boolean castrado) {}

    public record PetResponse(Long id, String nome, Especie especie, String raca, Sexo sexo, LocalDate dataNascimento,
                              BigDecimal pesoKg, boolean castrado) {}

    public record TriageInput(@NotNull Long petId, Long clinicaId, @NotNull CanalTriagem canal,
                               @NotBlank @Size(min = 10, max = 2000) String relato) {}
    public record TriageResponse(Long id, Long petId, String petNome, String clinicaNome, CanalTriagem canal,
                                 String relato, BigDecimal scoreRisco, String urgencia, String analiseVisual,
                                 String status, LocalDateTime criadaEm) {}

    public record AlertInput(@NotNull Long petId, @NotNull TipoAlerta tipo, @NotBlank @Size(max = 160) String titulo,
                             @Size(max = 1000) String descricao, @NotNull LocalDate dataPrevista) {}
    public record AlertResponse(Long id, Long petId, String petNome, String clinicaNome, TipoAlerta tipo,
                                String titulo, String descricao, LocalDate dataPrevista, String status) {}
    public record ClinicResponse(Long id, String nome, String cidade, String estado) {}
}
