package br.com.fiap.zelo.repository;

import br.com.fiap.zelo.domain.Clinica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClinicaRepository extends JpaRepository<Clinica, Long> {

    List<Clinica> findByAtivaOrderByNomeAsc(String ativa);

    @Query("select c from Clinica c join c.equipe u where u.id = :usuarioId")
    Optional<Clinica> findByMembroEquipeId(@Param("usuarioId") Long usuarioId);
}
