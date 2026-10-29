package br.com.fiap.zelo.repository;

import br.com.fiap.zelo.domain.Triagem;
import br.com.fiap.zelo.domain.enums.StatusTriagem;
import br.com.fiap.zelo.repository.projecao.ContagemPorStatusTriagem;
import br.com.fiap.zelo.repository.projecao.ContagemPorUrgencia;
import br.com.fiap.zelo.repository.projecao.MomentoTriagem;
import br.com.fiap.zelo.repository.projecao.PacienteResumo;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TriagemRepository extends JpaRepository<Triagem, Long> {

    @Override
    @EntityGraph(attributePaths = {"pet", "clinica", "tutor"})
    Optional<Triagem> findById(Long id);

    @Query("select t from Triagem t join fetch t.pet left join fetch t.tutor tu left join fetch tu.usuario " +
           "where t.clinica.id = :clinicaId and t.statusTriagem in :status")
    List<Triagem> findDaClinica(@Param("clinicaId") Long clinicaId,
                                @Param("status") Collection<StatusTriagem> status, Sort sort);

    @Query("select t from Triagem t join fetch t.pet left join fetch t.clinica " +
           "where t.tutor.id = :tutorId order by t.criadaEm desc")
    List<Triagem> findDoTutor(@Param("tutorId") Long tutorId);

    @Query("select t from Triagem t left join fetch t.clinica where t.pet.id = :petId order by t.criadaEm desc")
    List<Triagem> findDoPet(@Param("petId") Long petId);

    @Query("select t from Triagem t left join fetch t.clinica " +
           "where t.pet.id = :petId and t.clinica.id = :clinicaId order by t.criadaEm desc")
    List<Triagem> findDoPetNaClinica(@Param("petId") Long petId, @Param("clinicaId") Long clinicaId);

    boolean existsByPetId(Long petId);

    boolean existsByPetIdAndClinicaId(Long petId, Long clinicaId);

    long countByClinicaIdAndStatusTriagemIn(Long clinicaId, Collection<StatusTriagem> status);

    long countByTutorIdAndStatusTriagemIn(Long tutorId, Collection<StatusTriagem> status);

    @Query("select new br.com.fiap.zelo.repository.projecao.ContagemPorUrgencia(t.nivelUrgencia, count(t)) " +
           "from Triagem t where t.clinica.id = :clinicaId and t.criadaEm >= :desde group by t.nivelUrgencia")
    List<ContagemPorUrgencia> contarPorUrgenciaNaClinica(@Param("clinicaId") Long clinicaId,
                                                         @Param("desde") LocalDateTime desde);

    @Query("select new br.com.fiap.zelo.repository.projecao.ContagemPorUrgencia(t.nivelUrgencia, count(t)) " +
           "from Triagem t where t.tutor.id = :tutorId and t.criadaEm >= :desde group by t.nivelUrgencia")
    List<ContagemPorUrgencia> contarPorUrgenciaDoTutor(@Param("tutorId") Long tutorId,
                                                       @Param("desde") LocalDateTime desde);

    @Query("select new br.com.fiap.zelo.repository.projecao.ContagemPorStatusTriagem(t.statusTriagem, count(t)) " +
           "from Triagem t where t.clinica.id = :clinicaId and t.criadaEm >= :desde group by t.statusTriagem")
    List<ContagemPorStatusTriagem> contarPorStatusNaClinica(@Param("clinicaId") Long clinicaId,
                                                            @Param("desde") LocalDateTime desde);

    @Query("select new br.com.fiap.zelo.repository.projecao.MomentoTriagem(t.criadaEm, t.nivelUrgencia, t.encaminhadaEm) " +
           "from Triagem t where t.clinica.id = :clinicaId and t.criadaEm >= :desde")
    List<MomentoTriagem> listarMomentosNaClinica(@Param("clinicaId") Long clinicaId,
                                                 @Param("desde") LocalDateTime desde);

    @Query("select new br.com.fiap.zelo.repository.projecao.PacienteResumo(p.id, p.nome, p.especie, count(t), max(t.criadaEm)) " +
           "from Triagem t join t.pet p where t.clinica.id = :clinicaId " +
           "group by p.id, p.nome, p.especie order by max(t.criadaEm) desc")
    List<PacienteResumo> listarPacientesDaClinica(@Param("clinicaId") Long clinicaId);

    @Query("select new br.com.fiap.zelo.repository.projecao.PacienteResumo(p.id, p.nome, p.especie, count(t), max(t.criadaEm)) " +
           "from Triagem t join t.pet p where t.clinica.id = :clinicaId and t.criadaEm >= :desde " +
           "group by p.id, p.nome, p.especie having count(t) > 1 order by count(t) desc, max(t.criadaEm) desc")
    List<PacienteResumo> listarPacientesRecorrentes(@Param("clinicaId") Long clinicaId,
                                                    @Param("desde") LocalDateTime desde, Pageable limite);
}
