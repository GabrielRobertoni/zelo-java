-- Codigo que o GESTOR compartilha para veterinarios entrarem na equipe da clinica.
ALTER TABLE T_CH_CLINICA ADD cd_convite VARCHAR2(12);

UPDATE T_CH_CLINICA SET cd_convite = 'AMIGA2026' WHERE nr_cnpj = '12345678000190';

CREATE UNIQUE INDEX UK_T_CH_CLINICA_CONVITE ON T_CH_CLINICA (cd_convite);
