package br.com.fiap.zelo.repository;

import br.com.fiap.zelo.domain.Triagem;
import br.com.fiap.zelo.domain.enums.StatusTriagem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TriagemRepository extends JpaRepository<Triagem, Long> {

    // A ordenacao por nivel de urgencia e feita em memoria pelo TriagemService
    // (via Comparator), evitando depender de comparacao de enum dentro do JPQL.
    // "join fetch" em pet/tutor evita consultas N+1 quando a view acessa
    // t.pet.nome / t.tutor.nome para cada linha da fila.
    @Query("select t from Triagem t join fetch t.pet left join fetch t.tutor " +
           "where t.clinica.id = :clinicaId and t.statusTriagem in :status " +
           "order by t.criadaEm asc")
    List<Triagem> findFilaDeAtencao(@Param("clinicaId") Long clinicaId, @Param("status") List<StatusTriagem> status);

    @Query("select t from Triagem t join fetch t.pet left join fetch t.clinica " +
           "where t.tutor.id = :tutorId order by t.criadaEm desc")
    List<Triagem> findByTutorIdOrderByCriadaEmDesc(@Param("tutorId") Long tutorId);

    @Query("select t from Triagem t join fetch t.pet left join fetch t.clinica " +
           "where t.pet.id = :petId order by t.criadaEm desc")
    List<Triagem> findByPetIdOrderByCriadaEmDesc(@Param("petId") Long petId);
}
