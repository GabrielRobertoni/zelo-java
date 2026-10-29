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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlertaServiceTest {

    @Mock
    private AlertaRepository alertaRepository;
    @Mock
    private PetRepository petRepository;
    @Mock
    private ClinicaRepository clinicaRepository;
    @Mock
    private PetService petService;
    @Mock
    private PacienteService pacienteService;

    private AlertaService alertaService;
    private final Pet pet = Pet.builder().id(1L).nome("Rex").build();
    private final Clinica clinica = Clinica.builder().id(100L).nome("Clínica Amiga Bicho").build();

    @BeforeEach
    void setUp() {
        alertaService = new AlertaService(alertaRepository, petRepository, clinicaRepository, petService, pacienteService);
    }

    private AlertaForm formulario() {
        AlertaForm form = new AlertaForm();
        form.setPetId(1L);
        form.setTipoAlerta(TipoAlerta.VACINA);
        form.setTitulo("  Reforço da V10  ");
        form.setMensagem("   ");
        form.setDataPrevista(LocalDate.now().plusDays(10));
        return form;
    }

    private Alerta alertaDaClinica(StatusAlerta status) {
        return Alerta.builder().id(5L).pet(pet).clinica(clinica).tipoAlerta(TipoAlerta.VACINA).titulo("V10")
                .dataPrevista(LocalDate.now()).statusAlerta(status).build();
    }

    @Test
    void tutorCriaLembretePessoalSemClinica() {
        when(petService.buscarDoTutor(1L, 10L)).thenReturn(pet);
        when(alertaRepository.save(any(Alerta.class))).thenAnswer(invocacao -> invocacao.getArgument(0));

        Alerta alerta = alertaService.criarComoTutor(10L, formulario());

        assertThat(alerta.getClinica()).isNull();
        assertThat(alerta.getStatusAlerta()).isEqualTo(StatusAlerta.PENDENTE);
        assertThat(alerta.getTitulo()).isEqualTo("Reforço da V10");
        assertThat(alerta.getMensagem()).isNull();
    }

    @Test
    void clinicaRecomendaCuidadoAoProprioPaciente() {
        when(pacienteService.buscarPaciente(1L, 100L)).thenReturn(pet);
        when(clinicaRepository.findById(100L)).thenReturn(Optional.of(clinica));
        when(alertaRepository.save(any(Alerta.class))).thenAnswer(invocacao -> invocacao.getArgument(0));

        assertThat(alertaService.criarComoClinica(100L, formulario()).getClinica()).isEqualTo(clinica);
    }

    @Test
    void clinicaNaoRecomendaCuidadoAPetQueNaoEPaciente() {
        when(pacienteService.buscarPaciente(1L, 100L)).thenThrow(new RecursoNaoEncontradoException("Não é paciente"));

        assertThatThrownBy(() -> alertaService.criarComoClinica(100L, formulario()))
                .isInstanceOf(RecursoNaoEncontradoException.class);
        verify(alertaRepository, never()).save(any());
    }

    @Test
    void clinicaEnviaLembretePendente() {
        Alerta alerta = alertaDaClinica(StatusAlerta.PENDENTE);
        when(alertaRepository.findById(5L)).thenReturn(Optional.of(alerta));
        when(alertaRepository.save(alerta)).thenReturn(alerta);

        assertThat(alertaService.marcarComoEnviado(5L, 100L).getStatusAlerta()).isEqualTo(StatusAlerta.ENVIADO);
    }

    @Test
    void clinicaNaoMexeEmAlertaDeOutraClinica() {
        when(alertaRepository.findById(5L)).thenReturn(Optional.of(alertaDaClinica(StatusAlerta.PENDENTE)));

        assertThatThrownBy(() -> alertaService.cancelarPelaClinica(5L, 200L)).isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void tutorConfirmaCuidadoDoProprioPet() {
        Alerta alerta = alertaDaClinica(StatusAlerta.ENVIADO);
        when(alertaRepository.findById(5L)).thenReturn(Optional.of(alerta));
        when(petRepository.existeVinculoComTutor(1L, 10L)).thenReturn(true);
        when(alertaRepository.save(alerta)).thenReturn(alerta);

        assertThat(alertaService.confirmar(5L, 10L).getStatusAlerta()).isEqualTo(StatusAlerta.CONCLUIDO);
    }

    @Test
    void tutorNaoConfirmaCuidadoDePetDeOutroTutor() {
        when(alertaRepository.findById(5L)).thenReturn(Optional.of(alertaDaClinica(StatusAlerta.PENDENTE)));
        when(petRepository.existeVinculoComTutor(1L, 11L)).thenReturn(false);

        assertThatThrownBy(() -> alertaService.confirmar(5L, 11L)).isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void naoConfirmaCuidadoJaCancelado() {
        when(alertaRepository.findById(5L)).thenReturn(Optional.of(alertaDaClinica(StatusAlerta.CANCELADO)));
        when(petRepository.existeVinculoComTutor(1L, 10L)).thenReturn(true);

        assertThatThrownBy(() -> alertaService.confirmar(5L, 10L)).isInstanceOf(RegraNegocioException.class);
    }
}
