package br.com.fiap.zelo.repository;

import br.com.fiap.zelo.domain.Pet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PetRepository extends JpaRepository<Pet, Long> {

    @Query("select p from Pet p join p.tutores pt where pt.tutor.id = :tutorId order by p.nome asc")
    List<Pet> findByTutorId(@Param("tutorId") Long tutorId);

    @Query("select case when count(pt) > 0 then true else false end from PetTutor pt " +
           "where pt.pet.id = :petId and pt.tutor.id = :tutorId")
    boolean existeVinculoComTutor(@Param("petId") Long petId, @Param("tutorId") Long tutorId);
}
