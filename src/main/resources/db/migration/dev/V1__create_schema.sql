/* =============================================================================
   Projeto Zelo - Java Advanced (Sprint 3)
   Migration V1 (perfis dev/test - H2 em memoria): cria o schema operacional
   usado pela aplicacao Spring, ESPELHANDO EXATAMENTE o DDL real entregue
   pelo time de Banco de Dados (arquivo "zelo_criar.sql", disciplina Mastering
   Relational and Non-Relational Database) - mesmos nomes de tabela/coluna
   (convencao T_CH_<ENTIDADE>, prefixos id_/nm_/ds_/tp_/st_/dt_/dh_/vl_/fl_),
   mesmos tipos e as mesmas CHECK constraints.

   Por que uma copia em vez de so usar o Flyway contra o Oracle real: em
   dev/test nao ha (e nao deveria haver) acesso a instancia Oracle real do
   time de Banco de Dados - o H2 em modo de compatibilidade Oracle permite
   compilar/testar/gravar o video de demonstracao offline. Contra o Oracle
   real (perfil "oracle"), o time de Banco de Dados ja roda o proprio script
   e cria essas tabelas - ali o Flyway do Java so aplica db/migration/oracle
   (ver README, secao "Integracao com o time de Banco de Dados").

   Diferencas conscientes em relacao ao schema "zelo_*" usado antes de
   recebermos o DDL real:
     - Nomes de tabela/coluna trocados para T_CH_* / id_/nm_/ds_/tp_/st_/... .
     - StatusTriagem perdeu o estado "EM_ANALISE" (o CHECK real so permite
       ABERTA/ENCAMINHADA/ATENDIDA/CANCELADA) - ver enum StatusTriagem.java.
     - Especie: CANINO/FELINO viraram CAO/GATO.
     - TipoAlerta: RETORNO/MEDICAMENTO/OUTRO viraram RETENCAO/POS_CONSULTA.
     - T_CH_PET_TUTOR agora tem chave primaria substituta (id_pet_tutor)
       em vez de chave composta (id_pet, id_tutor) - o par vira so uma
       constraint UNIQUE.

   IMPORTANTE sobre "NUMBER" sem precisao (bare) nas colunas de identificador:
   e exatamente assim que o time de Banco de Dados declarou (GENERATED ALWAYS
   AS IDENTITY, sem precisao). Isso e reproduzido aqui de proposito, em vez
   de "corrigido" para NUMBER(19) como fizemos numa iteracao anterior deste
   projeto (quando ainda nao tinhamos o DDL real): o H2 em modo Oracle trata
   esse "NUMBER" bare como um tipo de precisao arbitraria ("decfloat"),
   diferente do BIGINT que o Hibernate esperaria em modo de validacao de
   schema - e foi exatamente esse tipo de erro (SchemaManagementException)
   que motivou a mudanca de "spring.jpa.hibernate.ddl-auto" para "none" em
   todos os perfis (ver application.yml). Com "none" o Hibernate nunca tenta
   validar essas colunas, entao reproduzir o DDL real aqui - inclusive esse
   detalhe - funciona sem erro e da a maior fidelidade possivel ao ambiente
   real para o qual este codigo tambem precisa rodar.

   Tabelas T_CH_FATO_CUIDADO e T_CH_AUDITORIA_DML, functions/procedures e a
   trigger de auditoria continuam fora desta migration: sao de
   responsabilidade exclusiva do script do time de Banco de Dados.
   ============================================================================= */

-- 1) T_CH_USUARIO ------------------------------------------------------------
CREATE TABLE T_CH_USUARIO (
    id_usuario       NUMBER GENERATED ALWAYS AS IDENTITY,
    nm_usuario       VARCHAR2(120) NOT NULL,
    ds_email         VARCHAR2(160) NOT NULL,
    ds_senha         VARCHAR2(255),
    tp_usuario       VARCHAR2(20) NOT NULL,
    st_usuario       CHAR(1) DEFAULT 'S' NOT NULL,
    dt_criacao       TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
    CONSTRAINT PK_T_CH_USUARIO PRIMARY KEY (id_usuario),
    CONSTRAINT UK_T_CH_USUARIO_EMAIL UNIQUE (ds_email),
    CONSTRAINT CK_T_CH_USUARIO_TIPO CHECK (tp_usuario IN ('TUTOR','VETERINARIO','GESTOR')),
    CONSTRAINT CK_T_CH_USUARIO_STATUS CHECK (st_usuario IN ('S','N'))
);

-- 2) T_CH_TUTOR ----------------------------------------------------------
CREATE TABLE T_CH_TUTOR (
    id_tutor         NUMBER GENERATED ALWAYS AS IDENTITY,
    id_usuario       NUMBER NOT NULL,
    nr_telefone      VARCHAR2(25),
    nm_cidade        VARCHAR2(80),
    sg_estado        CHAR(2),
    dt_criacao       TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
    CONSTRAINT PK_T_CH_TUTOR PRIMARY KEY (id_tutor),
    CONSTRAINT UK_T_CH_TUTOR_USUARIO UNIQUE (id_usuario),
    CONSTRAINT FK_T_CH_TUTOR_USUARIO FOREIGN KEY (id_usuario)
        REFERENCES T_CH_USUARIO (id_usuario)
);

-- 3) T_CH_CLINICA ----------------------------------------------------------
CREATE TABLE T_CH_CLINICA (
    id_clinica       NUMBER GENERATED ALWAYS AS IDENTITY,
    nm_clinica       VARCHAR2(160) NOT NULL,
    nr_cnpj          VARCHAR2(18),
    nr_telefone      VARCHAR2(25),
    ds_endereco      VARCHAR2(220),
    nm_cidade        VARCHAR2(80),
    sg_estado        CHAR(2),
    st_clinica       CHAR(1) DEFAULT 'S' NOT NULL,
    dt_criacao       TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
    CONSTRAINT PK_T_CH_CLINICA PRIMARY KEY (id_clinica),
    CONSTRAINT UK_T_CH_CLINICA_CNPJ UNIQUE (nr_cnpj),
    CONSTRAINT CK_T_CH_CLINICA_STATUS CHECK (st_clinica IN ('S','N'))
);

-- Tabela adicional, NAO prevista no DDL do time de Banco de Dados: vincula
-- usuarios VETERINARIO/GESTOR a clinica em que atuam. Necessaria para o
-- Spring Security resolver "qual clinica este usuario administra" - ver
-- README, secao "Decisoes assumidas". Roda tambem no perfil "oracle"
-- (db/migration/oracle/V1__create_equipe_table.sql), ja que o time de Banco
-- de Dados nao a cria.
CREATE TABLE T_CH_CLINICA_EQUIPE (
    id_clinica      NUMBER NOT NULL,
    id_usuario      NUMBER NOT NULL,
    CONSTRAINT PK_T_CH_CLINICA_EQUIPE PRIMARY KEY (id_clinica, id_usuario),
    CONSTRAINT FK_TCE_CLINICA FOREIGN KEY (id_clinica) REFERENCES T_CH_CLINICA (id_clinica),
    CONSTRAINT FK_TCE_USUARIO FOREIGN KEY (id_usuario) REFERENCES T_CH_USUARIO (id_usuario)
);

-- 4) T_CH_PET ---------------------------------------------------------------
CREATE TABLE T_CH_PET (
    id_pet           NUMBER GENERATED ALWAYS AS IDENTITY,
    nm_pet           VARCHAR2(100) NOT NULL,
    tp_especie       VARCHAR2(30) NOT NULL,
    nm_raca          VARCHAR2(80),
    tp_sexo          CHAR(1),
    dt_nascimento    DATE,
    vl_peso_kg       NUMBER(6,2),
    fl_castrado      CHAR(1),
    dt_criacao       TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
    CONSTRAINT PK_T_CH_PET PRIMARY KEY (id_pet),
    CONSTRAINT CK_T_CH_PET_ESPECIE CHECK (tp_especie IN ('CAO','GATO','OUTRO')),
    CONSTRAINT CK_T_CH_PET_SEXO CHECK (tp_sexo IN ('M','F','I')),
    CONSTRAINT CK_T_CH_PET_PESO CHECK (vl_peso_kg IS NULL OR vl_peso_kg > 0),
    CONSTRAINT CK_T_CH_PET_CASTRADO CHECK (fl_castrado IN ('S','N'))
);

-- 5) T_CH_PET_TUTOR (N:N - recurso Multi-Tutor) -----------------------------
CREATE TABLE T_CH_PET_TUTOR (
    id_pet_tutor     NUMBER GENERATED ALWAYS AS IDENTITY,
    id_pet           NUMBER NOT NULL,
    id_tutor         NUMBER NOT NULL,
    fl_principal     CHAR(1) DEFAULT 'N' NOT NULL,
    dt_vinculo       DATE DEFAULT SYSDATE NOT NULL,
    CONSTRAINT PK_T_CH_PET_TUTOR PRIMARY KEY (id_pet_tutor),
    CONSTRAINT UK_T_CH_PET_TUTOR UNIQUE (id_pet, id_tutor),
    CONSTRAINT FK_T_CH_PT_PET FOREIGN KEY (id_pet)
        REFERENCES T_CH_PET (id_pet),
    CONSTRAINT FK_T_CH_PT_TUTOR FOREIGN KEY (id_tutor)
        REFERENCES T_CH_TUTOR (id_tutor),
    CONSTRAINT CK_T_CH_PT_PRINCIPAL CHECK (fl_principal IN ('S','N'))
);

-- 6) T_CH_TRIAGEM ------------------------------------------------------------
-- Tabela mais importante do fluxo "Triagem e Encaminhamento". Referenciada
-- pela trigger de auditoria da disciplina de Banco de Dados (T_CH_AUDITORIA_DML).
CREATE TABLE T_CH_TRIAGEM (
    id_triagem       NUMBER GENERATED ALWAYS AS IDENTITY,
    id_pet           NUMBER NOT NULL,
    id_clinica       NUMBER,
    id_tutor         NUMBER,
    tp_canal         VARCHAR2(15) NOT NULL,
    ds_relato        VARCHAR2(2000) NOT NULL,
    vl_score_risco   NUMBER(5,2) NOT NULL,
    tp_urgencia      VARCHAR2(15) NOT NULL,
    ds_analise_visual VARCHAR2(1000),
    dh_encaminhamento TIMESTAMP,
    st_triagem       VARCHAR2(20) DEFAULT 'ABERTA' NOT NULL,
    dt_criacao       TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
    CONSTRAINT PK_T_CH_TRIAGEM PRIMARY KEY (id_triagem),
    CONSTRAINT FK_T_CH_TRIAGEM_PET FOREIGN KEY (id_pet)
        REFERENCES T_CH_PET (id_pet),
    CONSTRAINT FK_T_CH_TRIAGEM_CLINICA FOREIGN KEY (id_clinica)
        REFERENCES T_CH_CLINICA (id_clinica),
    CONSTRAINT FK_T_CH_TRIAGEM_TUTOR FOREIGN KEY (id_tutor)
        REFERENCES T_CH_TUTOR (id_tutor),
    CONSTRAINT CK_T_CH_TRIAGEM_CANAL CHECK (tp_canal IN ('TEXTO','AUDIO','IMAGEM')),
    CONSTRAINT CK_T_CH_TRIAGEM_SCORE CHECK (vl_score_risco BETWEEN 0 AND 100),
    CONSTRAINT CK_T_CH_TRIAGEM_URGENCIA CHECK (tp_urgencia IN ('BAIXA','MEDIA','ALTA','EMERGENCIA')),
    CONSTRAINT CK_T_CH_TRIAGEM_STATUS CHECK (st_triagem IN ('ABERTA','ENCAMINHADA','ATENDIDA','CANCELADA'))
);

CREATE INDEX IX_T_CH_TRIAGEM_CLINICA_STATUS ON T_CH_TRIAGEM (id_clinica, st_triagem);
CREATE INDEX IX_T_CH_TRIAGEM_PET ON T_CH_TRIAGEM (id_pet);

-- 7) T_CH_ALERTA ---------------------------------------------------------------
-- Sustenta o fluxo "Plano de Cuidado / Ciclo Zelo".
CREATE TABLE T_CH_ALERTA (
    id_alerta        NUMBER GENERATED ALWAYS AS IDENTITY,
    id_pet           NUMBER NOT NULL,
    id_clinica       NUMBER,
    tp_alerta        VARCHAR2(25) NOT NULL,
    nm_alerta        VARCHAR2(160) NOT NULL,
    ds_alerta        VARCHAR2(1000),
    dt_prevista      DATE NOT NULL,
    dh_envio         TIMESTAMP,
    st_alerta        VARCHAR2(15) DEFAULT 'PENDENTE' NOT NULL,
    dt_criacao       TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
    CONSTRAINT PK_T_CH_ALERTA PRIMARY KEY (id_alerta),
    CONSTRAINT FK_T_CH_ALERTA_PET FOREIGN KEY (id_pet)
        REFERENCES T_CH_PET (id_pet),
    CONSTRAINT FK_T_CH_ALERTA_CLINICA FOREIGN KEY (id_clinica)
        REFERENCES T_CH_CLINICA (id_clinica),
    CONSTRAINT CK_T_CH_ALERTA_TIPO CHECK (tp_alerta IN ('VACINA','CHECKUP','RETENCAO','POS_CONSULTA')),
    CONSTRAINT CK_T_CH_ALERTA_STATUS CHECK (st_alerta IN ('PENDENTE','ENVIADO','CONCLUIDO','CANCELADO'))
);

CREATE INDEX IX_T_CH_ALERTA_PET_STATUS ON T_CH_ALERTA (id_pet, st_alerta);
