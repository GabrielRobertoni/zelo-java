package br.com.fiap.zelo.domain.enums;

/**
 * Ciclo de vida de um alerta (plano de cuidado / Ciclo Zelo):
 *
 *   PENDENTE --(clinica dispara a notificacao)--> ENVIADO --(tutor confirma)--> CONCLUIDO
 *      \_______________________(cancelado pelo tutor ou pela clinica)_______________________--> CANCELADO
 */
public enum StatusAlerta {
    PENDENTE,
    ENVIADO,
    CONCLUIDO,
    CANCELADO
}
