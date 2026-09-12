package br.com.fiap.zelo.repository;

import br.com.fiap.zelo.domain.Tutor;
import br.com.fiap.zelo.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TutorRepository extends JpaRepository<Tutor, Long> {

    Optional<Tutor> findByUsuario(Usuario usuario);

    Optional<Tutor> findByUsuarioId(Long usuarioId);
}
