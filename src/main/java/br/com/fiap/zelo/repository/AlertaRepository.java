package br.com.fiap.zelo.repository;

import br.com.fiap.zelo.domain.Alerta;
import br.com.fiap.zelo.domain.enums.StatusAlerta;
import br.com.fiap.zelo.repository.projecao.ContagemAlertas;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface AlertaRepository extends JpaRepository<Alerta, Long> {

    @Override
    @EntityGraph(attributePaths = {"pet", "clinica"})
    Optional<Alerta> findById(Long id);

    @Query("select a from Alerta a join fetch a.pet left join fetch a.clinica " +
           "join a.pet.tutores pt where pt.tutor.id = :tutorId and a.statusAlerta in :status")
    List<Alerta> findDoTutor(@Param("tutorId") Long tutorId, @Param("status") Collection<StatusAlerta> status, Sort sort);

    @Query("select a from Alerta a join fetch a.pet where a.clinica.id = :clinicaId and a.statusAlerta in :status")
    List<Alerta> findDaClinica(@Param("clinicaId") Long clinicaId, @Param("status") Collection<StatusAlerta> status,
                               Sort sort);

    @Query("select a from Alerta a left join fetch a.clinica where a.pet.id = :petId order by a.dataPrevista desc")
    List<Alerta> findDoPet(@Param("petId") Long petId);

    @Query("select a from Alerta a left join fetch a.clinica " +
           "where a.pet.id = :petId and a.clinica.id = :clinicaId order by a.dataPrevista desc")
    List<Alerta> findDoPetNaClinica(@Param("petId") Long petId, @Param("clinicaId") Long clinicaId);

    boolean existsByPetId(Long petId);

    @Query("select new br.com.fiap.zelo.repository.projecao.ContagemAlertas(a.tipoAlerta, a.statusAlerta, count(a)) " +
           "from Alerta a where a.clinica.id = :clinicaId and a.dataPrevista >= :desde " +
           "group by a.tipoAlerta, a.statusAlerta")
    List<ContagemAlertas> contarPorTipoEStatusNaClinica(@Param("clinicaId") Long clinicaId,
                                                        @Param("desde") LocalDate desde);

    @Query("select new br.com.fiap.zelo.repository.projecao.ContagemAlertas(a.tipoAlerta, a.statusAlerta, count(a)) " +
           "from Alerta a join a.pet.tutores pt where pt.tutor.id = :tutorId group by a.tipoAlerta, a.statusAlerta")
    List<ContagemAlertas> contarPorTipoEStatusDoTutor(@Param("tutorId") Long tutorId);
}
