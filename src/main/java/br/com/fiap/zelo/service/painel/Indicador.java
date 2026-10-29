package br.com.fiap.zelo.service.painel;

/**
 * Card de indicador do painel.
 *
 * @param tom estilo visual do card: "neutro", "positivo", "atencao" ou "critico"
 */
public record Indicador(String titulo, String valor, String detalhe, String icone, String tom) {
}
