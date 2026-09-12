package br.com.fiap.zelo.web.api;

import br.com.fiap.zelo.service.ClinicaService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/clinicas")
public class ApiClinicController {
    private final ClinicaService clinicaService;
    public ApiClinicController(ClinicaService clinicaService) { this.clinicaService = clinicaService; }
    @GetMapping public List<ApiModels.ClinicResponse> list() { return clinicaService.listarAtivas().stream().map(clinica -> new ApiModels.ClinicResponse(clinica.getId(), clinica.getNome(), clinica.getCidade(), clinica.getEstado())).toList(); }
}
