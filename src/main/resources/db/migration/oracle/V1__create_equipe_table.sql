-- =============================================================================
-- Projeto Zelo - Java Advanced (Sprint 3)
-- Migration V1 (perfil "oracle" - instancia Oracle real do time de Banco de
-- Dados): NAO recria as tabelas principais - elas ja sao criadas pelo script
-- proprio do time de Banco de Dados (zelo_criar.sql), que roda ANTES desta
-- migration (ver README, secao "Integracao com o time de Banco de Dados").
--
-- A UNICA tabela criada aqui e T_CH_CLINICA_EQUIPE, que nao faz parte do DDL
-- do time de Banco de Dados: foi adicionada pela aplicacao Java para
-- viabilizar o Spring Security (resolver a qual clinica um usuario
-- VETERINARIO/GESTOR pertence). Como e uma tabela nova, sem sobreposicao com
-- as deles, e seguro cria-la aqui sem interferir no schema que o time de
-- Banco de Dados controla.
--
-- Pre-requisito: T_CH_CLINICA e T_CH_USUARIO (do script deles) ja precisam
-- existir no schema antes desta migration rodar - se o boot falhar aqui com
-- "table or view does not exist", o script deles ainda nao rodou.
-- =============================================================================

CREATE TABLE T_CH_CLINICA_EQUIPE (
    id_clinica      NUMBER NOT NULL,
    id_usuario      NUMBER NOT NULL,
    CONSTRAINT PK_T_CH_CLINICA_EQUIPE PRIMARY KEY (id_clinica, id_usuario),
    CONSTRAINT FK_TCE_CLINICA FOREIGN KEY (id_clinica) REFERENCES T_CH_CLINICA (id_clinica),
    CONSTRAINT FK_TCE_USUARIO FOREIGN KEY (id_usuario) REFERENCES T_CH_USUARIO (id_usuario)
);
