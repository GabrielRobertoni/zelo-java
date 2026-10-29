package br.com.fiap.zelo.service.painel;

import br.com.fiap.zelo.domain.Alerta;
import br.com.fiap.zelo.domain.Clinica;
import br.com.fiap.zelo.domain.Triagem;
import br.com.fiap.zelo.repository.projecao.PacienteResumo;

import java.util.List;
import java.util.Map;

public record PainelClinica(
        Clinica clinica,
        Periodo periodo,
        List<Indicador> indicadores,
        Map<String, Grafico> graficos,
        List<Triagem> filaPrioritaria,
        int totalNaFila,
        List<Alerta> alertasAtrasados,
        List<Alerta> proximosAlertas,
        List<PacienteResumo> pacientesRecorrentes,
        int tamanhoEquipe) {
}
