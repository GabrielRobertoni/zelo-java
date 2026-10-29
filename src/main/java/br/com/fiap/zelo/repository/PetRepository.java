package br.com.fiap.zelo.repository;

import br.com.fiap.zelo.domain.Pet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PetRepository extends JpaRepository<Pet, Long> {

    @Query("select p from Pet p join p.tutores pt where pt.tutor.id = :tutorId order by p.nome asc")
    List<Pet> findByTutorId(@Param("tutorId") Long tutorId);

    @Query("select distinct p from Triagem t join t.pet p where t.clinica.id = :clinicaId order by p.nome asc")
    List<Pet> findPacientesDaClinica(@Param("clinicaId") Long clinicaId);

    @Query("select distinct p from Pet p left join fetch p.tutores pt left join fetch pt.tutor t " +
           "left join fetch t.usuario where p.id = :petId")
    Optional<Pet> findComTutores(@Param("petId") Long petId);

    @Query("select case when count(pt) > 0 then true else false end from PetTutor pt " +
           "where pt.pet.id = :petId and pt.tutor.id = :tutorId")
    boolean existeVinculoComTutor(@Param("petId") Long petId, @Param("tutorId") Long tutorId);
}
