-- Contas de demonstracao (senha de todas: Zelo@123, hash BCrypt custo 10).

INSERT INTO T_CH_CLINICA (nm_clinica, nr_cnpj, nr_telefone, ds_endereco, nm_cidade, sg_estado, st_clinica)
VALUES ('Clínica Amiga Bicho', '12345678000190', '11999990000', 'Rua das Flores, 100', 'São Paulo', 'SP', 'S');

INSERT INTO T_CH_USUARIO (nm_usuario, ds_email, ds_senha, tp_usuario, st_usuario) VALUES
    ('Ana Gestora',   'gestor@zelo.com.br',      '$2b$10$pHkMRPdWzNsGp0DgpMlEX.E2cQ8deSNwD3ZVulbW3ZvHdMlPijY56', 'GESTOR',      'S'),
    ('Bruno Vet',      'veterinario@zelo.com.br', '$2b$10$pHkMRPdWzNsGp0DgpMlEX.E2cQ8deSNwD3ZVulbW3ZvHdMlPijY56', 'VETERINARIO', 'S'),
    ('Carla Tutora',   'tutor@zelo.com.br',       '$2b$10$pHkMRPdWzNsGp0DgpMlEX.E2cQ8deSNwD3ZVulbW3ZvHdMlPijY56', 'TUTOR',       'S');

INSERT INTO T_CH_CLINICA_EQUIPE (id_clinica, id_usuario)
SELECT c.id_clinica, u.id_usuario
FROM T_CH_CLINICA c, T_CH_USUARIO u
WHERE c.nm_clinica = 'Clínica Amiga Bicho' AND u.ds_email IN ('gestor@zelo.com.br', 'veterinario@zelo.com.br');

INSERT INTO T_CH_TUTOR (id_usuario, nr_telefone, nm_cidade, sg_estado)
SELECT id_usuario, '11988887777', 'São Paulo', 'SP'
FROM T_CH_USUARIO WHERE ds_email = 'tutor@zelo.com.br';

INSERT INTO T_CH_PET (nm_pet, tp_especie, nm_raca, tp_sexo, dt_nascimento, vl_peso_kg, fl_castrado) VALUES
    ('Rex', 'CAO',  'Labrador', 'M', DATE '2021-03-10', 28.50, 'S'),
    ('Mia', 'GATO', 'SRD',      'F', DATE '2022-07-22', 4.20,  'N');

INSERT INTO T_CH_PET_TUTOR (id_pet, id_tutor, fl_principal, dt_vinculo)
SELECT p.id_pet, t.id_tutor, 'S', CURRENT_DATE
FROM T_CH_PET p, T_CH_TUTOR t
WHERE p.nm_pet IN ('Rex', 'Mia');
