package br.com.fiap.zelo.security;

import br.com.fiap.zelo.domain.Usuario;
import br.com.fiap.zelo.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ZeloUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public ZeloUserDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UsernameNotFoundException("Nao existe usuario cadastrado com o e-mail informado."));
        return new ZeloUserPrincipal(usuario);
    }
}
