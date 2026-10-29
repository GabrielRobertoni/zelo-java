package br.com.fiap.zelo.integracao;

import br.com.fiap.zelo.domain.Pet;
import br.com.fiap.zelo.repository.ClinicaRepository;
import br.com.fiap.zelo.repository.PetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

/**
 * Base dos testes de integração: sobe a aplicação inteira sobre um H2 novo, criado e
 * populado pelas mesmas migrations do Flyway usadas na execução normal. Os testes não
 * usam transação, para reproduzir o comportamento real (inclusive o carregamento lazy).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
abstract class IntegracaoBase {

    static final String CNPJ_AMIGA = "12345678000190";
    static final String CNPJ_PAULISTA = "98765432000110";
    static final String SENHA_DEMO = "Zelo@123";

    @Autowired
    protected MockMvc mvc;
    @Autowired
    protected UserDetailsService userDetailsService;
    @Autowired
    protected PetRepository petRepository;
    @Autowired
    protected ClinicaRepository clinicaRepository;

    protected RequestPostProcessor como(String email) {
        return user(userDetailsService.loadUserByUsername(email));
    }

    protected Long idDoPet(String nome) {
        return petRepository.findAll().stream()
                .filter(pet -> pet.getNome().equals(nome))
                .map(Pet::getId)
                .findFirst()
                .orElseThrow();
    }

    protected Long idDaClinica(String cnpj) {
        return clinicaRepository.findAll().stream()
                .filter(clinica -> cnpj.equals(clinica.getCnpj()))
                .findFirst()
                .orElseThrow()
                .getId();
    }
}
