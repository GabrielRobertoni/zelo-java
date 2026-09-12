-- =============================================================================
-- Projeto Zelo - Java Advanced (Sprint 3)
-- Migration V2 (perfil "oracle"): massa de dados minima para permitir login
-- e demonstracao dos fluxos contra a instancia Oracle real, ja que o script
-- do time de Banco de Dados (zelo_criar.sql) so cria as tabelas - nao insere
-- nenhuma linha.
--
-- ATENCAO - COORDENAR COM O TIME DE BANCO DE DADOS antes de rodar contra a
-- instancia compartilhada: se o script deles (ou alguem do time) tambem
-- inserir um usuario com um destes emails, a constraint UNIQUE de
-- T_CH_USUARIO.ds_email vai rejeitar o insert duplicado. Cada INSERT abaixo
-- e protegido com "WHERE NOT EXISTS" exatamente por causa disso: rodar esta
-- migration de novo (ou depois de alguem ja ter inserido esses e-mails) nao
-- deve quebrar o boot, so deixa de inserir o que ja existir.
--
-- Senha em texto puro de TODOS os usuarios de demonstracao: Zelo@123
-- =============================================================================

INSERT INTO T_CH_CLINICA (nm_clinica, nr_cnpj, nr_telefone, ds_endereco, nm_cidade, sg_estado, st_clinica)
SELECT 'Clinica Amiga Bicho', '12345678000190', '11999990000', 'Rua das Flores, 100', 'Sao Paulo', 'SP', 'S'
FROM dual
WHERE NOT EXISTS (SELECT 1 FROM T_CH_CLINICA WHERE nr_cnpj = '12345678000190');

INSERT INTO T_CH_USUARIO (nm_usuario, ds_email, ds_senha, tp_usuario, st_usuario)
SELECT 'Ana Gestora', 'gestor@zelo.com.br', '$2b$10$pHkMRPdWzNsGp0DgpMlEX.E2cQ8deSNwD3ZVulbW3ZvHdMlPijY56', 'GESTOR', 'S' FROM dual
WHERE NOT EXISTS (SELECT 1 FROM T_CH_USUARIO WHERE ds_email = 'gestor@zelo.com.br');

INSERT INTO T_CH_USUARIO (nm_usuario, ds_email, ds_senha, tp_usuario, st_usuario)
SELECT 'Bruno Vet', 'veterinario@zelo.com.br', '$2b$10$pHkMRPdWzNsGp0DgpMlEX.E2cQ8deSNwD3ZVulbW3ZvHdMlPijY56', 'VETERINARIO', 'S' FROM dual
WHERE NOT EXISTS (SELECT 1 FROM T_CH_USUARIO WHERE ds_email = 'veterinario@zelo.com.br');

INSERT INTO T_CH_USUARIO (nm_usuario, ds_email, ds_senha, tp_usuario, st_usuario)
SELECT 'Carla Tutora', 'tutor@zelo.com.br', '$2b$10$pHkMRPdWzNsGp0DgpMlEX.E2cQ8deSNwD3ZVulbW3ZvHdMlPijY56', 'TUTOR', 'S' FROM dual
WHERE NOT EXISTS (SELECT 1 FROM T_CH_USUARIO WHERE ds_email = 'tutor@zelo.com.br');

INSERT INTO T_CH_CLINICA_EQUIPE (id_clinica, id_usuario)
SELECT c.id_clinica, u.id_usuario
FROM T_CH_CLINICA c, T_CH_USUARIO u
WHERE c.nr_cnpj = '12345678000190' AND u.ds_email IN ('gestor@zelo.com.br', 'veterinario@zelo.com.br')
AND NOT EXISTS (
    SELECT 1 FROM T_CH_CLINICA_EQUIPE tce
    WHERE tce.id_clinica = c.id_clinica AND tce.id_usuario = u.id_usuario
);

INSERT INTO T_CH_TUTOR (id_usuario, nr_telefone, nm_cidade, sg_estado)
SELECT u.id_usuario, '11988887777', 'Sao Paulo', 'SP'
FROM T_CH_USUARIO u
WHERE u.ds_email = 'tutor@zelo.com.br'
AND NOT EXISTS (SELECT 1 FROM T_CH_TUTOR t WHERE t.id_usuario = u.id_usuario);

INSERT INTO T_CH_PET (nm_pet, tp_especie, nm_raca, tp_sexo, dt_nascimento, vl_peso_kg, fl_castrado)
SELECT 'Rex', 'CAO', 'Labrador', 'M', DATE '2021-03-10', 28.50, 'S' FROM dual
WHERE NOT EXISTS (SELECT 1 FROM T_CH_PET WHERE nm_pet = 'Rex');

INSERT INTO T_CH_PET (nm_pet, tp_especie, nm_raca, tp_sexo, dt_nascimento, vl_peso_kg, fl_castrado)
SELECT 'Mia', 'GATO', 'SRD', 'F', DATE '2022-07-22', 4.20, 'N' FROM dual
WHERE NOT EXISTS (SELECT 1 FROM T_CH_PET WHERE nm_pet = 'Mia');

INSERT INTO T_CH_PET_TUTOR (id_pet, id_tutor, fl_principal, dt_vinculo)
SELECT p.id_pet, t.id_tutor, 'S', SYSDATE
FROM T_CH_PET p, T_CH_TUTOR t, T_CH_USUARIO u
WHERE p.nm_pet IN ('Rex', 'Mia') AND t.id_usuario = u.id_usuario AND u.ds_email = 'tutor@zelo.com.br'
AND NOT EXISTS (
    SELECT 1 FROM T_CH_PET_TUTOR pt WHERE pt.id_pet = p.id_pet AND pt.id_tutor = t.id_tutor
);
