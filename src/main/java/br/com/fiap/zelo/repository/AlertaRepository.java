package br.com.fiap.zelo.repository;

import br.com.fiap.zelo.domain.Alerta;
import br.com.fiap.zelo.domain.enums.StatusAlerta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AlertaRepository extends JpaRepository<Alerta, Long> {

    // "join fetch" em pet/clinica evita consultas N+1 quando a view acessa
    // a.pet.nome / a.clinica.nome para cada linha da lista.
    @Query("select a from Alerta a join fetch a.pet left join fetch a.clinica " +
           "join a.pet.tutores pt where pt.tutor.id = :tutorId and a.statusAlerta in :status " +
           "order by a.dataPrevista asc")
    List<Alerta> findPendentesPorTutor(@Param("tutorId") Long tutorId, @Param("status") List<StatusAlerta> status);

    @Query("select a from Alerta a join fetch a.pet where a.clinica.id = :clinicaId order by a.dataPrevista asc")
    List<Alerta> findByClinicaIdOrderByDataPrevistaAsc(@Param("clinicaId") Long clinicaId);

    @Query("select a from Alerta a join fetch a.pet left join fetch a.clinica " +
           "where a.pet.id = :petId order by a.dataPrevista asc")
    List<Alerta> findByPetIdOrderByDataPrevistaAsc(@Param("petId") Long petId);
}
