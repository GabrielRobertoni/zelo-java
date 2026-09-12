package br.com.fiap.zelo.domain.enums;

/**
 * Ciclo de vida de uma triagem. Valores alinhados ao CHECK
 * CK_T_CH_TRIAGEM_STATUS do DDL real entregue pelo time de Banco de Dados
 * (zelo_criar.sql), que so permite ABERTA/ENCAMINHADA/ATENDIDA/CANCELADA -
 * sem um estado "EM_ANALISE" separado. O que antes era EM_ANALISE (triagem
 * criada, aguardando a clinica decidir) agora e representado por ABERTA; o
 * que antes era CONCLUIDA agora e ATENDIDA. A maquina de estados em si nao
 * mudou, so os nomes:
 *
 *   ABERTA --(clinica encaminha)--> ENCAMINHADA --(atendimento)--> ATENDIDA
 *      \_________________________(tutor ou clinica cancela)_________________________/--> CANCELADA
 */
public enum StatusTriagem {
    ABERTA,
    ENCAMINHADA,
    ATENDIDA,
    CANCELADA
}
