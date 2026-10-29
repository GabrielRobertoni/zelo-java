package br.com.fiap.zelo;

import br.com.fiap.zelo.config.SelecaoDeBanco;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Zelo: cuidado contínuo para pets, conectando tutores e clínicas veterinárias. */
@SpringBootApplication
public class ZeloApplication {

    public static void main(String[] args) {
        SelecaoDeBanco.iniciar(ZeloApplication.class, args);
    }
}
