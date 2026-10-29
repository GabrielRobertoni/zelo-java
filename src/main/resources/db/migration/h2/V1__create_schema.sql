/* Schema operacional do Zelo (H2 em modo Oracle). Espelha o DDL do time de
   Banco de Dados (2TDSR_CodigoSql_Zelo.sql): mesmos nomes T_CH_*, tipos e CHECKs.
   T_CH_CLINICA_EQUIPE e a unica tabela propria da aplicacao Java. */

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

-- Vinculo de VETERINARIO/GESTOR com a clinica em que atuam.
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
