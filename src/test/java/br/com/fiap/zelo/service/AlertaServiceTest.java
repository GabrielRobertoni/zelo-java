package br.com.fiap.zelo.service;

import br.com.fiap.zelo.domain.Alerta;
import br.com.fiap.zelo.domain.Clinica;
import br.com.fiap.zelo.domain.Pet;
import br.com.fiap.zelo.domain.enums.StatusAlerta;
import br.com.fiap.zelo.domain.enums.TipoAlerta;
import br.com.fiap.zelo.exception.RecursoNaoEncontradoException;
import br.com.fiap.zelo.exception.RegraNegocioException;
import br.com.fiap.zelo.repository.AlertaRepository;
import br.com.fiap.zelo.repository.ClinicaRepository;
import br.com.fiap.zelo.repository.PetRepository;
import br.com.fiap.zelo.web.dto.AlertaForm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Cobre o fluxo completo nº 2 exigido pela Sprint 3 (Alertas / Plano de
 * Cuidado - Ciclo Zelo): criacao pelo tutor e pela clinica, disparo,
 * confirmacao e as transicoes de cancelamento.
 */
@ExtendWith(MockitoExtension.class)
class AlertaServiceTest {

    @Mock
    private AlertaRepository alertaRepository;
    @Mock
    private PetRepository petRepository;
    @Mock
    private ClinicaRepository clinicaRepository;

    private AlertaService alertaService;

    private Pet pet;
    private Clinica clinica;

    @BeforeEach
    void setUp() {
        alertaService = new AlertaService(alertaRepository, petRepository, clinicaRepository);
        pet = Pet.builder().id(1L).nome("Rex").build();
        clinica = Clinica.builder().id(100L).nome("Clinica Amiga Bicho").ativa("S").build();
    }

    private AlertaForm formValido() {
        AlertaForm form = new AlertaForm();
        form.setPetId(1L);
        form.setTipoAlerta(TipoAlerta.VACINA);
        form.setTitulo("Vacina antirrabica");
        form.setDataPrevista(LocalDate.now().plusDays(7));
        return form;
    }

    @Test
    void criarComoTutorExigeQuePetPertencaAoTutor() {
        when(petRepository.existeVinculoComTutor(1L, 10L)).thenReturn(false);

        assertThatThrownBy(() -> alertaService.criarComoTutor(10L, formValido()))
                .isInstanceOf(RecursoNaoEncontradoException.class);

        verifyNoInteractions(alertaRepository);
    }

    @Test
    void criarComoTutorPersisteAlertaSemClinicaEComStatusPendente() {
        when(petRepository.existeVinculoComTutor(1L, 10L)).thenReturn(true);
        when(petRepository.findById(1L)).thenReturn(Optional.of(pet));
        when(alertaRepository.save(any(Alerta.class))).thenAnswer(inv -> inv.getArgument(0));

        Alerta alerta = alertaService.criarComoTutor(10L, formValido());

        assertThat(alerta.getClinica()).isNull();
        assertThat(alerta.getStatusAlerta()).isEqualTo(StatusAlerta.PENDENTE);
        assertThat(alerta.getPet()).isEqualTo(pet);
    }

    @Test
    void criarComoClinicaPersisteAlertaVinculadoAClinica() {
        when(petRepository.findById(1L)).thenReturn(Optional.of(pet));
        when(clinicaRepository.findById(100L)).thenReturn(Optional.of(clinica));
        when(alertaRepository.save(any(Alerta.class))).thenAnswer(inv -> inv.getArgument(0));

        Alerta alerta = alertaService.criarComoClinica(100L, formValido());

        assertThat(alerta.getClinica()).isEqualTo(clinica);
        assertThat(alerta.getStatusAlerta()).isEqualTo(StatusAlerta.PENDENTE);
    }

    @Test
    void marcarComoEnviadoSoPermitidoQuandoPendente() {
        Alerta alerta = alertaComStatus(StatusAlerta.ENVIADO);
        when(alertaRepository.findById(5L)).thenReturn(Optional.of(alerta));

        assertThatThrownBy(() -> alertaService.marcarComoEnviado(5L, 100L))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void marcarComoEnviadoComSucessoRegistraDataDeEnvio() {
        Alerta alerta = alertaComStatus(StatusAlerta.PENDENTE);
        when(alertaRepository.findById(5L)).thenReturn(Optional.of(alerta));
        when(alertaRepository.save(any(Alerta.class))).thenAnswer(inv -> inv.getArgument(0));

        Alerta resultado = alertaService.marcarComoEnviado(5L, 100L);

        assertThat(resultado.getStatusAlerta()).isEqualTo(StatusAlerta.ENVIADO);
        assertThat(resultado.getDataEnvio()).isNotNull();
    }

    @Test
    void confirmarAlertaJaFinalizadoLancaExcecao() {
        Alerta alerta = alertaComStatus(StatusAlerta.CONCLUIDO);
        when(alertaRepository.findById(5L)).thenReturn(Optional.of(alerta));
        when(petRepository.existeVinculoComTutor(1L, 10L)).thenReturn(true);

        assertThatThrownBy(() -> alertaService.confirmar(5L, 10L))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void confirmarAlertaDePetDeOutroTutorLancaExcecao() {
        Alerta alerta = alertaComStatus(StatusAlerta.PENDENTE);
        when(alertaRepository.findById(5L)).thenReturn(Optional.of(alerta));
        when(petRepository.existeVinculoComTutor(1L, 999L)).thenReturn(false);

        assertThatThrownBy(() -> alertaService.confirmar(5L, 999L))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void confirmarComSucessoConcluiOAlerta() {
        Alerta alerta = alertaComStatus(StatusAlerta.ENVIADO);
        when(alertaRepository.findById(5L)).thenReturn(Optional.of(alerta));
        when(petRepository.existeVinculoComTutor(1L, 10L)).thenReturn(true);
        when(alertaRepository.save(any(Alerta.class))).thenAnswer(inv -> inv.getArgument(0));

        Alerta resultado = alertaService.confirmar(5L, 10L);

        assertThat(resultado.getStatusAlerta()).isEqualTo(StatusAlerta.CONCLUIDO);
    }

    @Test
    void cancelarAlertaJaCanceladoLancaExcecao() {
        Alerta alerta = alertaComStatus(StatusAlerta.CANCELADO);
        when(alertaRepository.findById(5L)).thenReturn(Optional.of(alerta));

        assertThatThrownBy(() -> alertaService.cancelarPelaClinica(5L, 100L))
                .isInstanceOf(RegraNegocioException.class);
    }

    private Alerta alertaComStatus(StatusAlerta status) {
        return Alerta.builder()
                .id(5L)
                .pet(pet)
                .clinica(clinica)
                .tipoAlerta(TipoAlerta.VACINA)
                .titulo("Vacina antirrabica")
                .dataPrevista(LocalDate.now().plusDays(7))
                .statusAlerta(status)
                .build();
    }
}
