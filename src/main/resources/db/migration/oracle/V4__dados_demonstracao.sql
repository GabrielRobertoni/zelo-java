-- Massa de demonstracao para os paineis (perfil oracle): segunda clinica, mais tutores, pets,
-- triagens dos ultimos 30 dias e alertas. Datas relativas ao momento da migracao.
-- Cada INSERT e protegido por WHERE NOT EXISTS: o banco e compartilhado com o time de Banco de
-- Dados, e a migration nao pode quebrar a inicializacao se algum dado ja existir.
-- Pets sao localizados pelo nome E pelo tutor dono: o script do time de BD tem pets com os mesmos nomes.
-- Senha de todas as contas: Zelo@123. Urgencia/score seguem o ClassificadorDeUrgencia.

INSERT INTO T_CH_CLINICA (nm_clinica, nr_cnpj, nr_telefone, ds_endereco, nm_cidade, sg_estado, st_clinica, cd_convite)
SELECT 'Vet Center Paulista', '98765432000110', '11977776666', 'Av. Paulista, 1500', 'São Paulo', 'SP', 'S', 'PAULISTA26' FROM dual
WHERE NOT EXISTS (SELECT 1 FROM T_CH_CLINICA WHERE nr_cnpj = '98765432000110') AND NOT EXISTS (SELECT 1 FROM T_CH_CLINICA WHERE cd_convite = 'PAULISTA26');

INSERT INTO T_CH_USUARIO (nm_usuario, ds_email, ds_senha, tp_usuario, st_usuario)
SELECT 'Diego Gestor', 'gestor2@zelo.com.br', '$2b$10$pHkMRPdWzNsGp0DgpMlEX.E2cQ8deSNwD3ZVulbW3ZvHdMlPijY56', 'GESTOR', 'S' FROM dual WHERE NOT EXISTS (SELECT 1 FROM T_CH_USUARIO WHERE ds_email = 'gestor2@zelo.com.br');
INSERT INTO T_CH_USUARIO (nm_usuario, ds_email, ds_senha, tp_usuario, st_usuario)
SELECT 'Elisa Vet', 'veterinario2@zelo.com.br', '$2b$10$pHkMRPdWzNsGp0DgpMlEX.E2cQ8deSNwD3ZVulbW3ZvHdMlPijY56', 'VETERINARIO', 'S' FROM dual WHERE NOT EXISTS (SELECT 1 FROM T_CH_USUARIO WHERE ds_email = 'veterinario2@zelo.com.br');
INSERT INTO T_CH_USUARIO (nm_usuario, ds_email, ds_senha, tp_usuario, st_usuario)
SELECT 'Fábio Vet', 'veterinario3@zelo.com.br', '$2b$10$pHkMRPdWzNsGp0DgpMlEX.E2cQ8deSNwD3ZVulbW3ZvHdMlPijY56', 'VETERINARIO', 'S' FROM dual WHERE NOT EXISTS (SELECT 1 FROM T_CH_USUARIO WHERE ds_email = 'veterinario3@zelo.com.br');
INSERT INTO T_CH_USUARIO (nm_usuario, ds_email, ds_senha, tp_usuario, st_usuario)
SELECT 'Felipe Tutor', 'tutor2@zelo.com.br', '$2b$10$pHkMRPdWzNsGp0DgpMlEX.E2cQ8deSNwD3ZVulbW3ZvHdMlPijY56', 'TUTOR', 'S' FROM dual WHERE NOT EXISTS (SELECT 1 FROM T_CH_USUARIO WHERE ds_email = 'tutor2@zelo.com.br');
INSERT INTO T_CH_USUARIO (nm_usuario, ds_email, ds_senha, tp_usuario, st_usuario)
SELECT 'Gabriela Tutora', 'tutor3@zelo.com.br', '$2b$10$pHkMRPdWzNsGp0DgpMlEX.E2cQ8deSNwD3ZVulbW3ZvHdMlPijY56', 'TUTOR', 'S' FROM dual WHERE NOT EXISTS (SELECT 1 FROM T_CH_USUARIO WHERE ds_email = 'tutor3@zelo.com.br');

INSERT INTO T_CH_CLINICA_EQUIPE (id_clinica, id_usuario)
SELECT c.id_clinica, u.id_usuario FROM T_CH_CLINICA c, T_CH_USUARIO u WHERE c.nr_cnpj = '98765432000110' AND u.ds_email = 'gestor2@zelo.com.br'
AND NOT EXISTS (SELECT 1 FROM T_CH_CLINICA_EQUIPE e WHERE e.id_clinica = c.id_clinica AND e.id_usuario = u.id_usuario);
INSERT INTO T_CH_CLINICA_EQUIPE (id_clinica, id_usuario)
SELECT c.id_clinica, u.id_usuario FROM T_CH_CLINICA c, T_CH_USUARIO u WHERE c.nr_cnpj = '98765432000110' AND u.ds_email = 'veterinario2@zelo.com.br'
AND NOT EXISTS (SELECT 1 FROM T_CH_CLINICA_EQUIPE e WHERE e.id_clinica = c.id_clinica AND e.id_usuario = u.id_usuario);
INSERT INTO T_CH_CLINICA_EQUIPE (id_clinica, id_usuario)
SELECT c.id_clinica, u.id_usuario FROM T_CH_CLINICA c, T_CH_USUARIO u WHERE c.nr_cnpj = '12345678000190' AND u.ds_email = 'veterinario3@zelo.com.br'
AND NOT EXISTS (SELECT 1 FROM T_CH_CLINICA_EQUIPE e WHERE e.id_clinica = c.id_clinica AND e.id_usuario = u.id_usuario);

INSERT INTO T_CH_TUTOR (id_usuario, nr_telefone, nm_cidade, sg_estado)
SELECT u.id_usuario, '11966665555', 'Osasco', 'SP' FROM T_CH_USUARIO u WHERE u.ds_email = 'tutor2@zelo.com.br'
AND NOT EXISTS (SELECT 1 FROM T_CH_TUTOR t WHERE t.id_usuario = u.id_usuario);
INSERT INTO T_CH_TUTOR (id_usuario, nr_telefone, nm_cidade, sg_estado)
SELECT u.id_usuario, '11955554444', 'Santo André', 'SP' FROM T_CH_USUARIO u WHERE u.ds_email = 'tutor3@zelo.com.br'
AND NOT EXISTS (SELECT 1 FROM T_CH_TUTOR t WHERE t.id_usuario = u.id_usuario);

INSERT INTO T_CH_PET (nm_pet, tp_especie, nm_raca, tp_sexo, dt_nascimento, vl_peso_kg, fl_castrado)
SELECT 'Thor', 'CAO', 'Golden Retriever', 'M', DATE '2019-05-02', 32.00, 'S' FROM dual WHERE NOT EXISTS (SELECT 1 FROM T_CH_PET p2 JOIN T_CH_PET_TUTOR pt2 ON pt2.id_pet = p2.id_pet JOIN T_CH_TUTOR t2 ON t2.id_tutor = pt2.id_tutor JOIN T_CH_USUARIO u2 ON u2.id_usuario = t2.id_usuario WHERE p2.nm_pet = 'Thor' AND u2.ds_email = 'tutor2@zelo.com.br');
INSERT INTO T_CH_PET (nm_pet, tp_especie, nm_raca, tp_sexo, dt_nascimento, vl_peso_kg, fl_castrado)
SELECT 'Luna', 'GATO', 'Siamês', 'F', DATE '2020-11-15', 3.80, 'S' FROM dual WHERE NOT EXISTS (SELECT 1 FROM T_CH_PET p2 JOIN T_CH_PET_TUTOR pt2 ON pt2.id_pet = p2.id_pet JOIN T_CH_TUTOR t2 ON t2.id_tutor = pt2.id_tutor JOIN T_CH_USUARIO u2 ON u2.id_usuario = t2.id_usuario WHERE p2.nm_pet = 'Luna' AND u2.ds_email = 'tutor2@zelo.com.br');
INSERT INTO T_CH_PET (nm_pet, tp_especie, nm_raca, tp_sexo, dt_nascimento, vl_peso_kg, fl_castrado)
SELECT 'Bolt', 'CAO', 'Border Collie', 'M', DATE '2022-02-20', 18.40, 'N' FROM dual WHERE NOT EXISTS (SELECT 1 FROM T_CH_PET p2 JOIN T_CH_PET_TUTOR pt2 ON pt2.id_pet = p2.id_pet JOIN T_CH_TUTOR t2 ON t2.id_tutor = pt2.id_tutor JOIN T_CH_USUARIO u2 ON u2.id_usuario = t2.id_usuario WHERE p2.nm_pet = 'Bolt' AND u2.ds_email = 'tutor3@zelo.com.br');
INSERT INTO T_CH_PET (nm_pet, tp_especie, nm_raca, tp_sexo, dt_nascimento, vl_peso_kg, fl_castrado)
SELECT 'Nina', 'GATO', 'Persa', 'F', DATE '2018-08-08', 4.60, 'S' FROM dual WHERE NOT EXISTS (SELECT 1 FROM T_CH_PET p2 JOIN T_CH_PET_TUTOR pt2 ON pt2.id_pet = p2.id_pet JOIN T_CH_TUTOR t2 ON t2.id_tutor = pt2.id_tutor JOIN T_CH_USUARIO u2 ON u2.id_usuario = t2.id_usuario WHERE p2.nm_pet = 'Nina' AND u2.ds_email = 'tutor3@zelo.com.br');
INSERT INTO T_CH_PET (nm_pet, tp_especie, nm_raca, tp_sexo, dt_nascimento, vl_peso_kg, fl_castrado)
SELECT 'Pipoca', 'OUTRO', 'Coelho mini lop', 'M', DATE '2023-04-01', 1.70, 'N' FROM dual WHERE NOT EXISTS (SELECT 1 FROM T_CH_PET p2 JOIN T_CH_PET_TUTOR pt2 ON pt2.id_pet = p2.id_pet JOIN T_CH_TUTOR t2 ON t2.id_tutor = pt2.id_tutor JOIN T_CH_USUARIO u2 ON u2.id_usuario = t2.id_usuario WHERE p2.nm_pet = 'Pipoca' AND u2.ds_email = 'tutor3@zelo.com.br');
INSERT INTO T_CH_PET_TUTOR (id_pet, id_tutor, fl_principal, dt_vinculo)
SELECT p.id_pet, t.id_tutor, 'S', SYSDATE FROM T_CH_PET p, T_CH_TUTOR t, T_CH_USUARIO u
WHERE p.id_pet = (SELECT MAX(p3.id_pet) FROM T_CH_PET p3 WHERE p3.nm_pet = 'Thor' AND NOT EXISTS (SELECT 1 FROM T_CH_PET_TUTOR y WHERE y.id_pet = p3.id_pet))
AND t.id_usuario = u.id_usuario AND u.ds_email = 'tutor2@zelo.com.br'
AND NOT EXISTS (SELECT 1 FROM T_CH_PET p2 JOIN T_CH_PET_TUTOR pt2 ON pt2.id_pet = p2.id_pet JOIN T_CH_TUTOR t2 ON t2.id_tutor = pt2.id_tutor JOIN T_CH_USUARIO u2 ON u2.id_usuario = t2.id_usuario WHERE p2.nm_pet = 'Thor' AND u2.ds_email = 'tutor2@zelo.com.br');
INSERT INTO T_CH_PET_TUTOR (id_pet, id_tutor, fl_principal, dt_vinculo)
SELECT p.id_pet, t.id_tutor, 'S', SYSDATE FROM T_CH_PET p, T_CH_TUTOR t, T_CH_USUARIO u
WHERE p.id_pet = (SELECT MAX(p3.id_pet) FROM T_CH_PET p3 WHERE p3.nm_pet = 'Luna' AND NOT EXISTS (SELECT 1 FROM T_CH_PET_TUTOR y WHERE y.id_pet = p3.id_pet))
AND t.id_usuario = u.id_usuario AND u.ds_email = 'tutor2@zelo.com.br'
AND NOT EXISTS (SELECT 1 FROM T_CH_PET p2 JOIN T_CH_PET_TUTOR pt2 ON pt2.id_pet = p2.id_pet JOIN T_CH_TUTOR t2 ON t2.id_tutor = pt2.id_tutor JOIN T_CH_USUARIO u2 ON u2.id_usuario = t2.id_usuario WHERE p2.nm_pet = 'Luna' AND u2.ds_email = 'tutor2@zelo.com.br');
INSERT INTO T_CH_PET_TUTOR (id_pet, id_tutor, fl_principal, dt_vinculo)
SELECT p.id_pet, t.id_tutor, 'S', SYSDATE FROM T_CH_PET p, T_CH_TUTOR t, T_CH_USUARIO u
WHERE p.id_pet = (SELECT MAX(p3.id_pet) FROM T_CH_PET p3 WHERE p3.nm_pet = 'Bolt' AND NOT EXISTS (SELECT 1 FROM T_CH_PET_TUTOR y WHERE y.id_pet = p3.id_pet))
AND t.id_usuario = u.id_usuario AND u.ds_email = 'tutor3@zelo.com.br'
AND NOT EXISTS (SELECT 1 FROM T_CH_PET p2 JOIN T_CH_PET_TUTOR pt2 ON pt2.id_pet = p2.id_pet JOIN T_CH_TUTOR t2 ON t2.id_tutor = pt2.id_tutor JOIN T_CH_USUARIO u2 ON u2.id_usuario = t2.id_usuario WHERE p2.nm_pet = 'Bolt' AND u2.ds_email = 'tutor3@zelo.com.br');
INSERT INTO T_CH_PET_TUTOR (id_pet, id_tutor, fl_principal, dt_vinculo)
SELECT p.id_pet, t.id_tutor, 'S', SYSDATE FROM T_CH_PET p, T_CH_TUTOR t, T_CH_USUARIO u
WHERE p.id_pet = (SELECT MAX(p3.id_pet) FROM T_CH_PET p3 WHERE p3.nm_pet = 'Nina' AND NOT EXISTS (SELECT 1 FROM T_CH_PET_TUTOR y WHERE y.id_pet = p3.id_pet))
AND t.id_usuario = u.id_usuario AND u.ds_email = 'tutor3@zelo.com.br'
AND NOT EXISTS (SELECT 1 FROM T_CH_PET p2 JOIN T_CH_PET_TUTOR pt2 ON pt2.id_pet = p2.id_pet JOIN T_CH_TUTOR t2 ON t2.id_tutor = pt2.id_tutor JOIN T_CH_USUARIO u2 ON u2.id_usuario = t2.id_usuario WHERE p2.nm_pet = 'Nina' AND u2.ds_email = 'tutor3@zelo.com.br');
INSERT INTO T_CH_PET_TUTOR (id_pet, id_tutor, fl_principal, dt_vinculo)
SELECT p.id_pet, t.id_tutor, 'S', SYSDATE FROM T_CH_PET p, T_CH_TUTOR t, T_CH_USUARIO u
WHERE p.id_pet = (SELECT MAX(p3.id_pet) FROM T_CH_PET p3 WHERE p3.nm_pet = 'Pipoca' AND NOT EXISTS (SELECT 1 FROM T_CH_PET_TUTOR y WHERE y.id_pet = p3.id_pet))
AND t.id_usuario = u.id_usuario AND u.ds_email = 'tutor3@zelo.com.br'
AND NOT EXISTS (SELECT 1 FROM T_CH_PET p2 JOIN T_CH_PET_TUTOR pt2 ON pt2.id_pet = p2.id_pet JOIN T_CH_TUTOR t2 ON t2.id_tutor = pt2.id_tutor JOIN T_CH_USUARIO u2 ON u2.id_usuario = t2.id_usuario WHERE p2.nm_pet = 'Pipoca' AND u2.ds_email = 'tutor3@zelo.com.br');

INSERT INTO T_CH_TRIAGEM (id_pet, id_clinica, id_tutor, tp_canal, ds_relato, vl_score_risco, tp_urgencia, st_triagem, dh_encaminhamento, dt_criacao)
SELECT p.id_pet, c.id_clinica, pt.id_tutor, 'TEXTO', 'Rex foi atropelado na rua e está sangrando na pata traseira.', 93, 'EMERGENCIA', 'ATENDIDA', CURRENT_TIMESTAMP - INTERVAL '28 04' DAY TO HOUR, CURRENT_TIMESTAMP - INTERVAL '28 05' DAY TO HOUR
FROM T_CH_PET p JOIN T_CH_PET_TUTOR pt ON pt.id_pet = p.id_pet JOIN T_CH_TUTOR tu ON tu.id_tutor = pt.id_tutor JOIN T_CH_USUARIO us ON us.id_usuario = tu.id_usuario, T_CH_CLINICA c WHERE p.nm_pet = 'Rex' AND us.ds_email = 'tutor@zelo.com.br' AND c.nr_cnpj = '12345678000190'
AND NOT EXISTS (SELECT 1 FROM T_CH_TRIAGEM x WHERE x.id_pet = p.id_pet AND x.id_clinica = c.id_clinica AND x.ds_relato = 'Rex foi atropelado na rua e está sangrando na pata traseira.');
INSERT INTO T_CH_TRIAGEM (id_pet, id_clinica, id_tutor, tp_canal, ds_relato, vl_score_risco, tp_urgencia, st_triagem, dh_encaminhamento, dt_criacao)
SELECT p.id_pet, c.id_clinica, pt.id_tutor, 'TEXTO', 'Mia está sem apetite há dois dias e mais quieta que o normal.', 40, 'MEDIA', 'ATENDIDA', CURRENT_TIMESTAMP - INTERVAL '27 01' DAY TO HOUR, CURRENT_TIMESTAMP - INTERVAL '27 06' DAY TO HOUR
FROM T_CH_PET p JOIN T_CH_PET_TUTOR pt ON pt.id_pet = p.id_pet JOIN T_CH_TUTOR tu ON tu.id_tutor = pt.id_tutor JOIN T_CH_USUARIO us ON us.id_usuario = tu.id_usuario, T_CH_CLINICA c WHERE p.nm_pet = 'Mia' AND us.ds_email = 'tutor@zelo.com.br' AND c.nr_cnpj = '12345678000190'
AND NOT EXISTS (SELECT 1 FROM T_CH_TRIAGEM x WHERE x.id_pet = p.id_pet AND x.id_clinica = c.id_clinica AND x.ds_relato = 'Mia está sem apetite há dois dias e mais quieta que o normal.');
INSERT INTO T_CH_TRIAGEM (id_pet, id_clinica, id_tutor, tp_canal, ds_relato, vl_score_risco, tp_urgencia, st_triagem, dh_encaminhamento, dt_criacao)
SELECT p.id_pet, c.id_clinica, pt.id_tutor, 'TEXTO', 'Thor está vomitando desde ontem à noite e parece com dor intensa.', 68, 'ALTA', 'ATENDIDA', CURRENT_TIMESTAMP - INTERVAL '25 00' DAY TO HOUR, CURRENT_TIMESTAMP - INTERVAL '25 02' DAY TO HOUR
FROM T_CH_PET p JOIN T_CH_PET_TUTOR pt ON pt.id_pet = p.id_pet JOIN T_CH_TUTOR tu ON tu.id_tutor = pt.id_tutor JOIN T_CH_USUARIO us ON us.id_usuario = tu.id_usuario, T_CH_CLINICA c WHERE p.nm_pet = 'Thor' AND us.ds_email = 'tutor2@zelo.com.br' AND c.nr_cnpj = '12345678000190'
AND NOT EXISTS (SELECT 1 FROM T_CH_TRIAGEM x WHERE x.id_pet = p.id_pet AND x.id_clinica = c.id_clinica AND x.ds_relato = 'Thor está vomitando desde ontem à noite e parece com dor intensa.');
INSERT INTO T_CH_TRIAGEM (id_pet, id_clinica, id_tutor, tp_canal, ds_relato, vl_score_risco, tp_urgencia, st_triagem, dh_encaminhamento, dt_criacao)
SELECT p.id_pet, c.id_clinica, pt.id_tutor, 'TEXTO', 'Luna precisa de avaliação de rotina para atualizar a carteirinha.', 15, 'BAIXA', 'ATENDIDA', CURRENT_TIMESTAMP - INTERVAL '23 08' DAY TO HOUR, CURRENT_TIMESTAMP - INTERVAL '24 04' DAY TO HOUR
FROM T_CH_PET p JOIN T_CH_PET_TUTOR pt ON pt.id_pet = p.id_pet JOIN T_CH_TUTOR tu ON tu.id_tutor = pt.id_tutor JOIN T_CH_USUARIO us ON us.id_usuario = tu.id_usuario, T_CH_CLINICA c WHERE p.nm_pet = 'Luna' AND us.ds_email = 'tutor2@zelo.com.br' AND c.nr_cnpj = '12345678000190'
AND NOT EXISTS (SELECT 1 FROM T_CH_TRIAGEM x WHERE x.id_pet = p.id_pet AND x.id_clinica = c.id_clinica AND x.ds_relato = 'Luna precisa de avaliação de rotina para atualizar a carteirinha.');
INSERT INTO T_CH_TRIAGEM (id_pet, id_clinica, id_tutor, tp_canal, ds_relato, vl_score_risco, tp_urgencia, st_triagem, dh_encaminhamento, dt_criacao)
SELECT p.id_pet, c.id_clinica, pt.id_tutor, 'TEXTO', 'Bolt está mancando depois do passeio no parque.', 40, 'MEDIA', 'ATENDIDA', CURRENT_TIMESTAMP - INTERVAL '22 02' DAY TO HOUR, CURRENT_TIMESTAMP - INTERVAL '22 08' DAY TO HOUR
FROM T_CH_PET p JOIN T_CH_PET_TUTOR pt ON pt.id_pet = p.id_pet JOIN T_CH_TUTOR tu ON tu.id_tutor = pt.id_tutor JOIN T_CH_USUARIO us ON us.id_usuario = tu.id_usuario, T_CH_CLINICA c WHERE p.nm_pet = 'Bolt' AND us.ds_email = 'tutor3@zelo.com.br' AND c.nr_cnpj = '12345678000190'
AND NOT EXISTS (SELECT 1 FROM T_CH_TRIAGEM x WHERE x.id_pet = p.id_pet AND x.id_clinica = c.id_clinica AND x.ds_relato = 'Bolt está mancando depois do passeio no parque.');
INSERT INTO T_CH_TRIAGEM (id_pet, id_clinica, id_tutor, tp_canal, ds_relato, vl_score_risco, tp_urgencia, st_triagem, dh_encaminhamento, dt_criacao)
SELECT p.id_pet, c.id_clinica, pt.id_tutor, 'TEXTO', 'Nina comeu uma planta e parece intoxicada, está salivando muito.', 90, 'EMERGENCIA', 'ATENDIDA', CURRENT_TIMESTAMP - INTERVAL '20 00' DAY TO HOUR, CURRENT_TIMESTAMP - INTERVAL '20 01' DAY TO HOUR
FROM T_CH_PET p JOIN T_CH_PET_TUTOR pt ON pt.id_pet = p.id_pet JOIN T_CH_TUTOR tu ON tu.id_tutor = pt.id_tutor JOIN T_CH_USUARIO us ON us.id_usuario = tu.id_usuario, T_CH_CLINICA c WHERE p.nm_pet = 'Nina' AND us.ds_email = 'tutor3@zelo.com.br' AND c.nr_cnpj = '12345678000190'
AND NOT EXISTS (SELECT 1 FROM T_CH_TRIAGEM x WHERE x.id_pet = p.id_pet AND x.id_clinica = c.id_clinica AND x.ds_relato = 'Nina comeu uma planta e parece intoxicada, está salivando muito.');
INSERT INTO T_CH_TRIAGEM (id_pet, id_clinica, id_tutor, tp_canal, ds_relato, vl_score_risco, tp_urgencia, st_triagem, dh_encaminhamento, dt_criacao)
SELECT p.id_pet, c.id_clinica, pt.id_tutor, 'TEXTO', 'Rex está com tosse seca e espirros frequentes.', 43, 'MEDIA', 'ATENDIDA', CURRENT_TIMESTAMP - INTERVAL '17 21' DAY TO HOUR, CURRENT_TIMESTAMP - INTERVAL '18 05' DAY TO HOUR
FROM T_CH_PET p JOIN T_CH_PET_TUTOR pt ON pt.id_pet = p.id_pet JOIN T_CH_TUTOR tu ON tu.id_tutor = pt.id_tutor JOIN T_CH_USUARIO us ON us.id_usuario = tu.id_usuario, T_CH_CLINICA c WHERE p.nm_pet = 'Rex' AND us.ds_email = 'tutor@zelo.com.br' AND c.nr_cnpj = '12345678000190'
AND NOT EXISTS (SELECT 1 FROM T_CH_TRIAGEM x WHERE x.id_pet = p.id_pet AND x.id_clinica = c.id_clinica AND x.ds_relato = 'Rex está com tosse seca e espirros frequentes.');
INSERT INTO T_CH_TRIAGEM (id_pet, id_clinica, id_tutor, tp_canal, ds_relato, vl_score_risco, tp_urgencia, st_triagem, dh_encaminhamento, dt_criacao)
SELECT p.id_pet, c.id_clinica, pt.id_tutor, 'TEXTO', 'Pipoca está apático e não come a ração.', 43, 'MEDIA', 'ATENDIDA', CURRENT_TIMESTAMP - INTERVAL '16 03' DAY TO HOUR, CURRENT_TIMESTAMP - INTERVAL '16 07' DAY TO HOUR
FROM T_CH_PET p JOIN T_CH_PET_TUTOR pt ON pt.id_pet = p.id_pet JOIN T_CH_TUTOR tu ON tu.id_tutor = pt.id_tutor JOIN T_CH_USUARIO us ON us.id_usuario = tu.id_usuario, T_CH_CLINICA c WHERE p.nm_pet = 'Pipoca' AND us.ds_email = 'tutor3@zelo.com.br' AND c.nr_cnpj = '12345678000190'
AND NOT EXISTS (SELECT 1 FROM T_CH_TRIAGEM x WHERE x.id_pet = p.id_pet AND x.id_clinica = c.id_clinica AND x.ds_relato = 'Pipoca está apático e não come a ração.');
INSERT INTO T_CH_TRIAGEM (id_pet, id_clinica, id_tutor, tp_canal, ds_relato, vl_score_risco, tp_urgencia, st_triagem, dh_encaminhamento, dt_criacao)
SELECT p.id_pet, c.id_clinica, pt.id_tutor, 'TEXTO', 'Thor voltou a ficar com diarreia com sangue e está fraco.', 96, 'EMERGENCIA', 'ATENDIDA', CURRENT_TIMESTAMP - INTERVAL '14 02' DAY TO HOUR, CURRENT_TIMESTAMP - INTERVAL '14 03' DAY TO HOUR
FROM T_CH_PET p JOIN T_CH_PET_TUTOR pt ON pt.id_pet = p.id_pet JOIN T_CH_TUTOR tu ON tu.id_tutor = pt.id_tutor JOIN T_CH_USUARIO us ON us.id_usuario = tu.id_usuario, T_CH_CLINICA c WHERE p.nm_pet = 'Thor' AND us.ds_email = 'tutor2@zelo.com.br' AND c.nr_cnpj = '12345678000190'
AND NOT EXISTS (SELECT 1 FROM T_CH_TRIAGEM x WHERE x.id_pet = p.id_pet AND x.id_clinica = c.id_clinica AND x.ds_relato = 'Thor voltou a ficar com diarreia com sangue e está fraco.');
INSERT INTO T_CH_TRIAGEM (id_pet, id_clinica, id_tutor, tp_canal, ds_relato, vl_score_risco, tp_urgencia, st_triagem, dh_encaminhamento, dt_criacao)
SELECT p.id_pet, c.id_clinica, pt.id_tutor, 'TEXTO', 'Mia está se coçando muito perto das orelhas.', 40, 'MEDIA', 'CANCELADA', NULL, CURRENT_TIMESTAMP - INTERVAL '13 09' DAY TO HOUR
FROM T_CH_PET p JOIN T_CH_PET_TUTOR pt ON pt.id_pet = p.id_pet JOIN T_CH_TUTOR tu ON tu.id_tutor = pt.id_tutor JOIN T_CH_USUARIO us ON us.id_usuario = tu.id_usuario, T_CH_CLINICA c WHERE p.nm_pet = 'Mia' AND us.ds_email = 'tutor@zelo.com.br' AND c.nr_cnpj = '12345678000190'
AND NOT EXISTS (SELECT 1 FROM T_CH_TRIAGEM x WHERE x.id_pet = p.id_pet AND x.id_clinica = c.id_clinica AND x.ds_relato = 'Mia está se coçando muito perto das orelhas.');
INSERT INTO T_CH_TRIAGEM (id_pet, id_clinica, id_tutor, tp_canal, ds_relato, vl_score_risco, tp_urgencia, st_triagem, dh_encaminhamento, dt_criacao)
SELECT p.id_pet, c.id_clinica, pt.id_tutor, 'TEXTO', 'Luna teve uma convulsão rapida hoje de manha.', 90, 'EMERGENCIA', 'ATENDIDA', CURRENT_TIMESTAMP - INTERVAL '11 01' DAY TO HOUR, CURRENT_TIMESTAMP - INTERVAL '11 02' DAY TO HOUR
FROM T_CH_PET p JOIN T_CH_PET_TUTOR pt ON pt.id_pet = p.id_pet JOIN T_CH_TUTOR tu ON tu.id_tutor = pt.id_tutor JOIN T_CH_USUARIO us ON us.id_usuario = tu.id_usuario, T_CH_CLINICA c WHERE p.nm_pet = 'Luna' AND us.ds_email = 'tutor2@zelo.com.br' AND c.nr_cnpj = '12345678000190'
AND NOT EXISTS (SELECT 1 FROM T_CH_TRIAGEM x WHERE x.id_pet = p.id_pet AND x.id_clinica = c.id_clinica AND x.ds_relato = 'Luna teve uma convulsão rapida hoje de manha.');
INSERT INTO T_CH_TRIAGEM (id_pet, id_clinica, id_tutor, tp_canal, ds_relato, vl_score_risco, tp_urgencia, st_triagem, dh_encaminhamento, dt_criacao)
SELECT p.id_pet, c.id_clinica, pt.id_tutor, 'TEXTO', 'Bolt está com febre alta e não quer levantar.', 65, 'ALTA', 'ATENDIDA', CURRENT_TIMESTAMP - INTERVAL '9 02' DAY TO HOUR, CURRENT_TIMESTAMP - INTERVAL '9 04' DAY TO HOUR
FROM T_CH_PET p JOIN T_CH_PET_TUTOR pt ON pt.id_pet = p.id_pet JOIN T_CH_TUTOR tu ON tu.id_tutor = pt.id_tutor JOIN T_CH_USUARIO us ON us.id_usuario = tu.id_usuario, T_CH_CLINICA c WHERE p.nm_pet = 'Bolt' AND us.ds_email = 'tutor3@zelo.com.br' AND c.nr_cnpj = '12345678000190'
AND NOT EXISTS (SELECT 1 FROM T_CH_TRIAGEM x WHERE x.id_pet = p.id_pet AND x.id_clinica = c.id_clinica AND x.ds_relato = 'Bolt está com febre alta e não quer levantar.');
INSERT INTO T_CH_TRIAGEM (id_pet, id_clinica, id_tutor, tp_canal, ds_relato, vl_score_risco, tp_urgencia, st_triagem, dh_encaminhamento, dt_criacao)
SELECT p.id_pet, c.id_clinica, pt.id_tutor, 'TEXTO', 'Quero agendar o retorno do Rex para revisar o curativo.', 15, 'BAIXA', 'ATENDIDA', CURRENT_TIMESTAMP - INTERVAL '7 12' DAY TO HOUR, CURRENT_TIMESTAMP - INTERVAL '8 10' DAY TO HOUR
FROM T_CH_PET p JOIN T_CH_PET_TUTOR pt ON pt.id_pet = p.id_pet JOIN T_CH_TUTOR tu ON tu.id_tutor = pt.id_tutor JOIN T_CH_USUARIO us ON us.id_usuario = tu.id_usuario, T_CH_CLINICA c WHERE p.nm_pet = 'Rex' AND us.ds_email = 'tutor@zelo.com.br' AND c.nr_cnpj = '12345678000190'
AND NOT EXISTS (SELECT 1 FROM T_CH_TRIAGEM x WHERE x.id_pet = p.id_pet AND x.id_clinica = c.id_clinica AND x.ds_relato = 'Quero agendar o retorno do Rex para revisar o curativo.');
INSERT INTO T_CH_TRIAGEM (id_pet, id_clinica, id_tutor, tp_canal, ds_relato, vl_score_risco, tp_urgencia, st_triagem, dh_encaminhamento, dt_criacao)
SELECT p.id_pet, c.id_clinica, pt.id_tutor, 'TEXTO', 'Nina está espirrando e com o nariz escorrendo.', 40, 'MEDIA', 'CANCELADA', NULL, CURRENT_TIMESTAMP - INTERVAL '7 06' DAY TO HOUR
FROM T_CH_PET p JOIN T_CH_PET_TUTOR pt ON pt.id_pet = p.id_pet JOIN T_CH_TUTOR tu ON tu.id_tutor = pt.id_tutor JOIN T_CH_USUARIO us ON us.id_usuario = tu.id_usuario, T_CH_CLINICA c WHERE p.nm_pet = 'Nina' AND us.ds_email = 'tutor3@zelo.com.br' AND c.nr_cnpj = '12345678000190'
AND NOT EXISTS (SELECT 1 FROM T_CH_TRIAGEM x WHERE x.id_pet = p.id_pet AND x.id_clinica = c.id_clinica AND x.ds_relato = 'Nina está espirrando e com o nariz escorrendo.');
INSERT INTO T_CH_TRIAGEM (id_pet, id_clinica, id_tutor, tp_canal, ds_relato, vl_score_risco, tp_urgencia, st_triagem, dh_encaminhamento, dt_criacao)
SELECT p.id_pet, c.id_clinica, pt.id_tutor, 'TEXTO', 'Pipoca está com inchaço na bochecha esquerda.', 65, 'ALTA', 'ATENDIDA', CURRENT_TIMESTAMP - INTERVAL '6 02' DAY TO HOUR, CURRENT_TIMESTAMP - INTERVAL '6 05' DAY TO HOUR
FROM T_CH_PET p JOIN T_CH_PET_TUTOR pt ON pt.id_pet = p.id_pet JOIN T_CH_TUTOR tu ON tu.id_tutor = pt.id_tutor JOIN T_CH_USUARIO us ON us.id_usuario = tu.id_usuario, T_CH_CLINICA c WHERE p.nm_pet = 'Pipoca' AND us.ds_email = 'tutor3@zelo.com.br' AND c.nr_cnpj = '12345678000190'
AND NOT EXISTS (SELECT 1 FROM T_CH_TRIAGEM x WHERE x.id_pet = p.id_pet AND x.id_clinica = c.id_clinica AND x.ds_relato = 'Pipoca está com inchaço na bochecha esquerda.');
INSERT INTO T_CH_TRIAGEM (id_pet, id_clinica, id_tutor, tp_canal, ds_relato, vl_score_risco, tp_urgencia, st_triagem, dh_encaminhamento, dt_criacao)
SELECT p.id_pet, c.id_clinica, pt.id_tutor, 'TEXTO', 'Thor está sem apetite e com tosse depois da tosa.', 43, 'MEDIA', 'ATENDIDA', CURRENT_TIMESTAMP - INTERVAL '5 02' DAY TO HOUR, CURRENT_TIMESTAMP - INTERVAL '5 08' DAY TO HOUR
FROM T_CH_PET p JOIN T_CH_PET_TUTOR pt ON pt.id_pet = p.id_pet JOIN T_CH_TUTOR tu ON tu.id_tutor = pt.id_tutor JOIN T_CH_USUARIO us ON us.id_usuario = tu.id_usuario, T_CH_CLINICA c WHERE p.nm_pet = 'Thor' AND us.ds_email = 'tutor2@zelo.com.br' AND c.nr_cnpj = '12345678000190'
AND NOT EXISTS (SELECT 1 FROM T_CH_TRIAGEM x WHERE x.id_pet = p.id_pet AND x.id_clinica = c.id_clinica AND x.ds_relato = 'Thor está sem apetite e com tosse depois da tosa.');
INSERT INTO T_CH_TRIAGEM (id_pet, id_clinica, id_tutor, tp_canal, ds_relato, vl_score_risco, tp_urgencia, st_triagem, dh_encaminhamento, dt_criacao)
SELECT p.id_pet, c.id_clinica, pt.id_tutor, 'TEXTO', 'Mia vomitou duas vezes hoje e está apática.', 68, 'ALTA', 'ENCAMINHADA', CURRENT_TIMESTAMP - INTERVAL '3 02' DAY TO HOUR, CURRENT_TIMESTAMP - INTERVAL '3 04' DAY TO HOUR
FROM T_CH_PET p JOIN T_CH_PET_TUTOR pt ON pt.id_pet = p.id_pet JOIN T_CH_TUTOR tu ON tu.id_tutor = pt.id_tutor JOIN T_CH_USUARIO us ON us.id_usuario = tu.id_usuario, T_CH_CLINICA c WHERE p.nm_pet = 'Mia' AND us.ds_email = 'tutor@zelo.com.br' AND c.nr_cnpj = '12345678000190'
AND NOT EXISTS (SELECT 1 FROM T_CH_TRIAGEM x WHERE x.id_pet = p.id_pet AND x.id_clinica = c.id_clinica AND x.ds_relato = 'Mia vomitou duas vezes hoje e está apática.');
INSERT INTO T_CH_TRIAGEM (id_pet, id_clinica, id_tutor, tp_canal, ds_relato, vl_score_risco, tp_urgencia, st_triagem, dh_encaminhamento, dt_criacao)
SELECT p.id_pet, c.id_clinica, pt.id_tutor, 'TEXTO', 'Luna precisa trocar a ração, quero orientação sobre a dieta.', 15, 'BAIXA', 'ENCAMINHADA', CURRENT_TIMESTAMP - INTERVAL '1 18' DAY TO HOUR, CURRENT_TIMESTAMP - INTERVAL '2 06' DAY TO HOUR
FROM T_CH_PET p JOIN T_CH_PET_TUTOR pt ON pt.id_pet = p.id_pet JOIN T_CH_TUTOR tu ON tu.id_tutor = pt.id_tutor JOIN T_CH_USUARIO us ON us.id_usuario = tu.id_usuario, T_CH_CLINICA c WHERE p.nm_pet = 'Luna' AND us.ds_email = 'tutor2@zelo.com.br' AND c.nr_cnpj = '12345678000190'
AND NOT EXISTS (SELECT 1 FROM T_CH_TRIAGEM x WHERE x.id_pet = p.id_pet AND x.id_clinica = c.id_clinica AND x.ds_relato = 'Luna precisa trocar a ração, quero orientação sobre a dieta.');
INSERT INTO T_CH_TRIAGEM (id_pet, id_clinica, id_tutor, tp_canal, ds_relato, vl_score_risco, tp_urgencia, st_triagem, dh_encaminhamento, dt_criacao)
SELECT p.id_pet, c.id_clinica, pt.id_tutor, 'TEXTO', 'Rex engasgou com um osso e está com dificuldade, parece que não consegue respirar direito.', 93, 'EMERGENCIA', 'ABERTA', NULL, CURRENT_TIMESTAMP - INTERVAL '0 01' DAY TO HOUR
FROM T_CH_PET p JOIN T_CH_PET_TUTOR pt ON pt.id_pet = p.id_pet JOIN T_CH_TUTOR tu ON tu.id_tutor = pt.id_tutor JOIN T_CH_USUARIO us ON us.id_usuario = tu.id_usuario, T_CH_CLINICA c WHERE p.nm_pet = 'Rex' AND us.ds_email = 'tutor@zelo.com.br' AND c.nr_cnpj = '12345678000190'
AND NOT EXISTS (SELECT 1 FROM T_CH_TRIAGEM x WHERE x.id_pet = p.id_pet AND x.id_clinica = c.id_clinica AND x.ds_relato = 'Rex engasgou com um osso e está com dificuldade, parece que não consegue respirar direito.');
INSERT INTO T_CH_TRIAGEM (id_pet, id_clinica, id_tutor, tp_canal, ds_relato, vl_score_risco, tp_urgencia, st_triagem, dh_encaminhamento, dt_criacao)
SELECT p.id_pet, c.id_clinica, pt.id_tutor, 'TEXTO', 'Bolt está gemendo de dor quando tocamos na barriga.', 65, 'ALTA', 'ABERTA', NULL, CURRENT_TIMESTAMP - INTERVAL '0 03' DAY TO HOUR
FROM T_CH_PET p JOIN T_CH_PET_TUTOR pt ON pt.id_pet = p.id_pet JOIN T_CH_TUTOR tu ON tu.id_tutor = pt.id_tutor JOIN T_CH_USUARIO us ON us.id_usuario = tu.id_usuario, T_CH_CLINICA c WHERE p.nm_pet = 'Bolt' AND us.ds_email = 'tutor3@zelo.com.br' AND c.nr_cnpj = '12345678000190'
AND NOT EXISTS (SELECT 1 FROM T_CH_TRIAGEM x WHERE x.id_pet = p.id_pet AND x.id_clinica = c.id_clinica AND x.ds_relato = 'Bolt está gemendo de dor quando tocamos na barriga.');
INSERT INTO T_CH_TRIAGEM (id_pet, id_clinica, id_tutor, tp_canal, ds_relato, vl_score_risco, tp_urgencia, st_triagem, dh_encaminhamento, dt_criacao)
SELECT p.id_pet, c.id_clinica, pt.id_tutor, 'TEXTO', 'Nina está com diarreia leve desde ontem.', 40, 'MEDIA', 'ABERTA', NULL, CURRENT_TIMESTAMP - INTERVAL '0 05' DAY TO HOUR
FROM T_CH_PET p JOIN T_CH_PET_TUTOR pt ON pt.id_pet = p.id_pet JOIN T_CH_TUTOR tu ON tu.id_tutor = pt.id_tutor JOIN T_CH_USUARIO us ON us.id_usuario = tu.id_usuario, T_CH_CLINICA c WHERE p.nm_pet = 'Nina' AND us.ds_email = 'tutor3@zelo.com.br' AND c.nr_cnpj = '12345678000190'
AND NOT EXISTS (SELECT 1 FROM T_CH_TRIAGEM x WHERE x.id_pet = p.id_pet AND x.id_clinica = c.id_clinica AND x.ds_relato = 'Nina está com diarreia leve desde ontem.');
INSERT INTO T_CH_TRIAGEM (id_pet, id_clinica, id_tutor, tp_canal, ds_relato, vl_score_risco, tp_urgencia, st_triagem, dh_encaminhamento, dt_criacao)
SELECT p.id_pet, c.id_clinica, pt.id_tutor, 'TEXTO', 'Pipoca está com as unhas grandes, quero agendar o corte.', 15, 'BAIXA', 'ABERTA', NULL, CURRENT_TIMESTAMP - INTERVAL '1 02' DAY TO HOUR
FROM T_CH_PET p JOIN T_CH_PET_TUTOR pt ON pt.id_pet = p.id_pet JOIN T_CH_TUTOR tu ON tu.id_tutor = pt.id_tutor JOIN T_CH_USUARIO us ON us.id_usuario = tu.id_usuario, T_CH_CLINICA c WHERE p.nm_pet = 'Pipoca' AND us.ds_email = 'tutor3@zelo.com.br' AND c.nr_cnpj = '12345678000190'
AND NOT EXISTS (SELECT 1 FROM T_CH_TRIAGEM x WHERE x.id_pet = p.id_pet AND x.id_clinica = c.id_clinica AND x.ds_relato = 'Pipoca está com as unhas grandes, quero agendar o corte.');
INSERT INTO T_CH_TRIAGEM (id_pet, id_clinica, id_tutor, tp_canal, ds_relato, vl_score_risco, tp_urgencia, st_triagem, dh_encaminhamento, dt_criacao)
SELECT p.id_pet, c.id_clinica, pt.id_tutor, 'TEXTO', 'Thor está mancando da pata dianteira direita.', 40, 'MEDIA', 'ATENDIDA', CURRENT_TIMESTAMP - INTERVAL '21 01' DAY TO HOUR, CURRENT_TIMESTAMP - INTERVAL '21 05' DAY TO HOUR
FROM T_CH_PET p JOIN T_CH_PET_TUTOR pt ON pt.id_pet = p.id_pet JOIN T_CH_TUTOR tu ON tu.id_tutor = pt.id_tutor JOIN T_CH_USUARIO us ON us.id_usuario = tu.id_usuario, T_CH_CLINICA c WHERE p.nm_pet = 'Thor' AND us.ds_email = 'tutor2@zelo.com.br' AND c.nr_cnpj = '98765432000110'
AND NOT EXISTS (SELECT 1 FROM T_CH_TRIAGEM x WHERE x.id_pet = p.id_pet AND x.id_clinica = c.id_clinica AND x.ds_relato = 'Thor está mancando da pata dianteira direita.');
INSERT INTO T_CH_TRIAGEM (id_pet, id_clinica, id_tutor, tp_canal, ds_relato, vl_score_risco, tp_urgencia, st_triagem, dh_encaminhamento, dt_criacao)
SELECT p.id_pet, c.id_clinica, pt.id_tutor, 'TEXTO', 'Luna está com sangue na urina desde hoje cedo.', 90, 'EMERGENCIA', 'ATENDIDA', CURRENT_TIMESTAMP - INTERVAL '12 02' DAY TO HOUR, CURRENT_TIMESTAMP - INTERVAL '12 03' DAY TO HOUR
FROM T_CH_PET p JOIN T_CH_PET_TUTOR pt ON pt.id_pet = p.id_pet JOIN T_CH_TUTOR tu ON tu.id_tutor = pt.id_tutor JOIN T_CH_USUARIO us ON us.id_usuario = tu.id_usuario, T_CH_CLINICA c WHERE p.nm_pet = 'Luna' AND us.ds_email = 'tutor2@zelo.com.br' AND c.nr_cnpj = '98765432000110'
AND NOT EXISTS (SELECT 1 FROM T_CH_TRIAGEM x WHERE x.id_pet = p.id_pet AND x.id_clinica = c.id_clinica AND x.ds_relato = 'Luna está com sangue na urina desde hoje cedo.');
INSERT INTO T_CH_TRIAGEM (id_pet, id_clinica, id_tutor, tp_canal, ds_relato, vl_score_risco, tp_urgencia, st_triagem, dh_encaminhamento, dt_criacao)
SELECT p.id_pet, c.id_clinica, pt.id_tutor, 'TEXTO', 'Bolt precisa de consulta de rotina e vermifugação.', 15, 'BAIXA', 'ENCAMINHADA', CURRENT_TIMESTAMP - INTERVAL '3 21' DAY TO HOUR, CURRENT_TIMESTAMP - INTERVAL '4 07' DAY TO HOUR
FROM T_CH_PET p JOIN T_CH_PET_TUTOR pt ON pt.id_pet = p.id_pet JOIN T_CH_TUTOR tu ON tu.id_tutor = pt.id_tutor JOIN T_CH_USUARIO us ON us.id_usuario = tu.id_usuario, T_CH_CLINICA c WHERE p.nm_pet = 'Bolt' AND us.ds_email = 'tutor3@zelo.com.br' AND c.nr_cnpj = '98765432000110'
AND NOT EXISTS (SELECT 1 FROM T_CH_TRIAGEM x WHERE x.id_pet = p.id_pet AND x.id_clinica = c.id_clinica AND x.ds_relato = 'Bolt precisa de consulta de rotina e vermifugação.');
INSERT INTO T_CH_TRIAGEM (id_pet, id_clinica, id_tutor, tp_canal, ds_relato, vl_score_risco, tp_urgencia, st_triagem, dh_encaminhamento, dt_criacao)
SELECT p.id_pet, c.id_clinica, pt.id_tutor, 'TEXTO', 'Nina está vomitando a comida logo depois de comer.', 65, 'ALTA', 'ABERTA', NULL, CURRENT_TIMESTAMP - INTERVAL '0 02' DAY TO HOUR
FROM T_CH_PET p JOIN T_CH_PET_TUTOR pt ON pt.id_pet = p.id_pet JOIN T_CH_TUTOR tu ON tu.id_tutor = pt.id_tutor JOIN T_CH_USUARIO us ON us.id_usuario = tu.id_usuario, T_CH_CLINICA c WHERE p.nm_pet = 'Nina' AND us.ds_email = 'tutor3@zelo.com.br' AND c.nr_cnpj = '98765432000110'
AND NOT EXISTS (SELECT 1 FROM T_CH_TRIAGEM x WHERE x.id_pet = p.id_pet AND x.id_clinica = c.id_clinica AND x.ds_relato = 'Nina está vomitando a comida logo depois de comer.');

INSERT INTO T_CH_ALERTA (id_pet, id_clinica, tp_alerta, nm_alerta, ds_alerta, dt_prevista, dh_envio, st_alerta)
SELECT p.id_pet, c.id_clinica, 'VACINA', 'Reforço da V10', 'Reforço anual da vacina polivalente.', CURRENT_DATE + INTERVAL '5' DAY, NULL, 'PENDENTE' FROM T_CH_PET p JOIN T_CH_PET_TUTOR pt ON pt.id_pet = p.id_pet JOIN T_CH_TUTOR tu ON tu.id_tutor = pt.id_tutor JOIN T_CH_USUARIO us ON us.id_usuario = tu.id_usuario, T_CH_CLINICA c WHERE p.nm_pet = 'Rex' AND us.ds_email = 'tutor@zelo.com.br' AND c.nr_cnpj = '12345678000190' AND NOT EXISTS (SELECT 1 FROM T_CH_ALERTA x WHERE x.id_pet = p.id_pet AND x.nm_alerta = 'Reforço da V10');
INSERT INTO T_CH_ALERTA (id_pet, id_clinica, tp_alerta, nm_alerta, ds_alerta, dt_prevista, dh_envio, st_alerta)
SELECT p.id_pet, c.id_clinica, 'POS_CONSULTA', 'Revisão do curativo', 'Trazer o Rex para revisar a pata traseira.', CURRENT_DATE - INTERVAL '3' DAY, CURRENT_TIMESTAMP - INTERVAL '1' DAY, 'ENVIADO' FROM T_CH_PET p JOIN T_CH_PET_TUTOR pt ON pt.id_pet = p.id_pet JOIN T_CH_TUTOR tu ON tu.id_tutor = pt.id_tutor JOIN T_CH_USUARIO us ON us.id_usuario = tu.id_usuario, T_CH_CLINICA c WHERE p.nm_pet = 'Rex' AND us.ds_email = 'tutor@zelo.com.br' AND c.nr_cnpj = '12345678000190' AND NOT EXISTS (SELECT 1 FROM T_CH_ALERTA x WHERE x.id_pet = p.id_pet AND x.nm_alerta = 'Revisão do curativo');
INSERT INTO T_CH_ALERTA (id_pet, id_clinica, tp_alerta, nm_alerta, ds_alerta, dt_prevista, dh_envio, st_alerta)
SELECT p.id_pet, c.id_clinica, 'VACINA', 'Vacina antirrábica', 'Dose anual da antirrábica.', CURRENT_DATE - INTERVAL '6' DAY, NULL, 'PENDENTE' FROM T_CH_PET p JOIN T_CH_PET_TUTOR pt ON pt.id_pet = p.id_pet JOIN T_CH_TUTOR tu ON tu.id_tutor = pt.id_tutor JOIN T_CH_USUARIO us ON us.id_usuario = tu.id_usuario, T_CH_CLINICA c WHERE p.nm_pet = 'Mia' AND us.ds_email = 'tutor@zelo.com.br' AND c.nr_cnpj = '12345678000190' AND NOT EXISTS (SELECT 1 FROM T_CH_ALERTA x WHERE x.id_pet = p.id_pet AND x.nm_alerta = 'Vacina antirrábica');
INSERT INTO T_CH_ALERTA (id_pet, id_clinica, tp_alerta, nm_alerta, ds_alerta, dt_prevista, dh_envio, st_alerta)
SELECT p.id_pet, c.id_clinica, 'CHECKUP', 'Check-up felino', 'Exames de rotina e pesagem.', CURRENT_DATE + INTERVAL '12' DAY, NULL, 'PENDENTE' FROM T_CH_PET p JOIN T_CH_PET_TUTOR pt ON pt.id_pet = p.id_pet JOIN T_CH_TUTOR tu ON tu.id_tutor = pt.id_tutor JOIN T_CH_USUARIO us ON us.id_usuario = tu.id_usuario, T_CH_CLINICA c WHERE p.nm_pet = 'Mia' AND us.ds_email = 'tutor@zelo.com.br' AND c.nr_cnpj = '12345678000190' AND NOT EXISTS (SELECT 1 FROM T_CH_ALERTA x WHERE x.id_pet = p.id_pet AND x.nm_alerta = 'Check-up felino');
INSERT INTO T_CH_ALERTA (id_pet, id_clinica, tp_alerta, nm_alerta, ds_alerta, dt_prevista, dh_envio, st_alerta)
SELECT p.id_pet, c.id_clinica, 'POS_CONSULTA', 'Retorno gastrointestinal', 'Avaliar evolução da diarreia.', CURRENT_DATE - INTERVAL '10' DAY, CURRENT_TIMESTAMP - INTERVAL '1' DAY, 'CONCLUIDO' FROM T_CH_PET p JOIN T_CH_PET_TUTOR pt ON pt.id_pet = p.id_pet JOIN T_CH_TUTOR tu ON tu.id_tutor = pt.id_tutor JOIN T_CH_USUARIO us ON us.id_usuario = tu.id_usuario, T_CH_CLINICA c WHERE p.nm_pet = 'Thor' AND us.ds_email = 'tutor2@zelo.com.br' AND c.nr_cnpj = '12345678000190' AND NOT EXISTS (SELECT 1 FROM T_CH_ALERTA x WHERE x.id_pet = p.id_pet AND x.nm_alerta = 'Retorno gastrointestinal');
INSERT INTO T_CH_ALERTA (id_pet, id_clinica, tp_alerta, nm_alerta, ds_alerta, dt_prevista, dh_envio, st_alerta)
SELECT p.id_pet, c.id_clinica, 'RETENCAO', 'Sentimos falta do Thor', 'Que tal agendar uma consulta preventiva?', CURRENT_DATE + INTERVAL '2' DAY, CURRENT_TIMESTAMP - INTERVAL '1' DAY, 'ENVIADO' FROM T_CH_PET p JOIN T_CH_PET_TUTOR pt ON pt.id_pet = p.id_pet JOIN T_CH_TUTOR tu ON tu.id_tutor = pt.id_tutor JOIN T_CH_USUARIO us ON us.id_usuario = tu.id_usuario, T_CH_CLINICA c WHERE p.nm_pet = 'Thor' AND us.ds_email = 'tutor2@zelo.com.br' AND c.nr_cnpj = '12345678000190' AND NOT EXISTS (SELECT 1 FROM T_CH_ALERTA x WHERE x.id_pet = p.id_pet AND x.nm_alerta = 'Sentimos falta do Thor');
INSERT INTO T_CH_ALERTA (id_pet, id_clinica, tp_alerta, nm_alerta, ds_alerta, dt_prevista, dh_envio, st_alerta)
SELECT p.id_pet, c.id_clinica, 'POS_CONSULTA', 'Retorno neurológico', 'Acompanhar episódios de convulsão.', CURRENT_DATE - INTERVAL '2' DAY, NULL, 'PENDENTE' FROM T_CH_PET p JOIN T_CH_PET_TUTOR pt ON pt.id_pet = p.id_pet JOIN T_CH_TUTOR tu ON tu.id_tutor = pt.id_tutor JOIN T_CH_USUARIO us ON us.id_usuario = tu.id_usuario, T_CH_CLINICA c WHERE p.nm_pet = 'Luna' AND us.ds_email = 'tutor2@zelo.com.br' AND c.nr_cnpj = '12345678000190' AND NOT EXISTS (SELECT 1 FROM T_CH_ALERTA x WHERE x.id_pet = p.id_pet AND x.nm_alerta = 'Retorno neurológico');
INSERT INTO T_CH_ALERTA (id_pet, id_clinica, tp_alerta, nm_alerta, ds_alerta, dt_prevista, dh_envio, st_alerta)
SELECT p.id_pet, c.id_clinica, 'VACINA', 'Vacina V4 felina', 'Reforço anual da V4.', CURRENT_DATE - INTERVAL '15' DAY, CURRENT_TIMESTAMP - INTERVAL '1' DAY, 'CONCLUIDO' FROM T_CH_PET p JOIN T_CH_PET_TUTOR pt ON pt.id_pet = p.id_pet JOIN T_CH_TUTOR tu ON tu.id_tutor = pt.id_tutor JOIN T_CH_USUARIO us ON us.id_usuario = tu.id_usuario, T_CH_CLINICA c WHERE p.nm_pet = 'Luna' AND us.ds_email = 'tutor2@zelo.com.br' AND c.nr_cnpj = '12345678000190' AND NOT EXISTS (SELECT 1 FROM T_CH_ALERTA x WHERE x.id_pet = p.id_pet AND x.nm_alerta = 'Vacina V4 felina');
INSERT INTO T_CH_ALERTA (id_pet, id_clinica, tp_alerta, nm_alerta, ds_alerta, dt_prevista, dh_envio, st_alerta)
SELECT p.id_pet, c.id_clinica, 'CHECKUP', 'Check-up ortopédico', 'Reavaliar a pata apos o repouso.', CURRENT_DATE + INTERVAL '9' DAY, NULL, 'PENDENTE' FROM T_CH_PET p JOIN T_CH_PET_TUTOR pt ON pt.id_pet = p.id_pet JOIN T_CH_TUTOR tu ON tu.id_tutor = pt.id_tutor JOIN T_CH_USUARIO us ON us.id_usuario = tu.id_usuario, T_CH_CLINICA c WHERE p.nm_pet = 'Bolt' AND us.ds_email = 'tutor3@zelo.com.br' AND c.nr_cnpj = '12345678000190' AND NOT EXISTS (SELECT 1 FROM T_CH_ALERTA x WHERE x.id_pet = p.id_pet AND x.nm_alerta = 'Check-up ortopédico');
INSERT INTO T_CH_ALERTA (id_pet, id_clinica, tp_alerta, nm_alerta, ds_alerta, dt_prevista, dh_envio, st_alerta)
SELECT p.id_pet, c.id_clinica, 'VACINA', 'Vacina de gripe canina', 'Dose anual da vacina de gripe.', CURRENT_DATE - INTERVAL '20' DAY, CURRENT_TIMESTAMP - INTERVAL '1' DAY, 'CONCLUIDO' FROM T_CH_PET p JOIN T_CH_PET_TUTOR pt ON pt.id_pet = p.id_pet JOIN T_CH_TUTOR tu ON tu.id_tutor = pt.id_tutor JOIN T_CH_USUARIO us ON us.id_usuario = tu.id_usuario, T_CH_CLINICA c WHERE p.nm_pet = 'Bolt' AND us.ds_email = 'tutor3@zelo.com.br' AND c.nr_cnpj = '12345678000190' AND NOT EXISTS (SELECT 1 FROM T_CH_ALERTA x WHERE x.id_pet = p.id_pet AND x.nm_alerta = 'Vacina de gripe canina');
INSERT INTO T_CH_ALERTA (id_pet, id_clinica, tp_alerta, nm_alerta, ds_alerta, dt_prevista, dh_envio, st_alerta)
SELECT p.id_pet, c.id_clinica, 'POS_CONSULTA', 'Retorno pós-intoxicação', 'Exame de sangue de controle.', CURRENT_DATE - INTERVAL '12' DAY, NULL, 'CANCELADO' FROM T_CH_PET p JOIN T_CH_PET_TUTOR pt ON pt.id_pet = p.id_pet JOIN T_CH_TUTOR tu ON tu.id_tutor = pt.id_tutor JOIN T_CH_USUARIO us ON us.id_usuario = tu.id_usuario, T_CH_CLINICA c WHERE p.nm_pet = 'Nina' AND us.ds_email = 'tutor3@zelo.com.br' AND c.nr_cnpj = '12345678000190' AND NOT EXISTS (SELECT 1 FROM T_CH_ALERTA x WHERE x.id_pet = p.id_pet AND x.nm_alerta = 'Retorno pós-intoxicação');
INSERT INTO T_CH_ALERTA (id_pet, id_clinica, tp_alerta, nm_alerta, ds_alerta, dt_prevista, dh_envio, st_alerta)
SELECT p.id_pet, c.id_clinica, 'CHECKUP', 'Avaliação odontológica', 'Conferir o crescimento dos dentes.', CURRENT_DATE + INTERVAL '20' DAY, NULL, 'PENDENTE' FROM T_CH_PET p JOIN T_CH_PET_TUTOR pt ON pt.id_pet = p.id_pet JOIN T_CH_TUTOR tu ON tu.id_tutor = pt.id_tutor JOIN T_CH_USUARIO us ON us.id_usuario = tu.id_usuario, T_CH_CLINICA c WHERE p.nm_pet = 'Pipoca' AND us.ds_email = 'tutor3@zelo.com.br' AND c.nr_cnpj = '12345678000190' AND NOT EXISTS (SELECT 1 FROM T_CH_ALERTA x WHERE x.id_pet = p.id_pet AND x.nm_alerta = 'Avaliação odontológica');
INSERT INTO T_CH_ALERTA (id_pet, id_clinica, tp_alerta, nm_alerta, ds_alerta, dt_prevista, dh_envio, st_alerta)
SELECT p.id_pet, c.id_clinica, 'POS_CONSULTA', 'Retorno do inchaço', 'Verificar se o inchaço regrediu.', CURRENT_DATE - INTERVAL '1' DAY, CURRENT_TIMESTAMP - INTERVAL '1' DAY, 'CONCLUIDO' FROM T_CH_PET p JOIN T_CH_PET_TUTOR pt ON pt.id_pet = p.id_pet JOIN T_CH_TUTOR tu ON tu.id_tutor = pt.id_tutor JOIN T_CH_USUARIO us ON us.id_usuario = tu.id_usuario, T_CH_CLINICA c WHERE p.nm_pet = 'Pipoca' AND us.ds_email = 'tutor3@zelo.com.br' AND c.nr_cnpj = '12345678000190' AND NOT EXISTS (SELECT 1 FROM T_CH_ALERTA x WHERE x.id_pet = p.id_pet AND x.nm_alerta = 'Retorno do inchaço');
INSERT INTO T_CH_ALERTA (id_pet, id_clinica, tp_alerta, nm_alerta, ds_alerta, dt_prevista, dh_envio, st_alerta)
SELECT p.id_pet, c.id_clinica, 'CHECKUP', 'Reavaliação da pata', 'Retorno ortopédico.', CURRENT_DATE + INTERVAL '6' DAY, NULL, 'PENDENTE' FROM T_CH_PET p JOIN T_CH_PET_TUTOR pt ON pt.id_pet = p.id_pet JOIN T_CH_TUTOR tu ON tu.id_tutor = pt.id_tutor JOIN T_CH_USUARIO us ON us.id_usuario = tu.id_usuario, T_CH_CLINICA c WHERE p.nm_pet = 'Thor' AND us.ds_email = 'tutor2@zelo.com.br' AND c.nr_cnpj = '98765432000110' AND NOT EXISTS (SELECT 1 FROM T_CH_ALERTA x WHERE x.id_pet = p.id_pet AND x.nm_alerta = 'Reavaliação da pata');
INSERT INTO T_CH_ALERTA (id_pet, id_clinica, tp_alerta, nm_alerta, ds_alerta, dt_prevista, dh_envio, st_alerta)
SELECT p.id_pet, c.id_clinica, 'VACINA', 'Vacina V4 anual', 'Dose anual da V4 felina.', CURRENT_DATE - INTERVAL '25' DAY, CURRENT_TIMESTAMP - INTERVAL '1' DAY, 'CONCLUIDO' FROM T_CH_PET p JOIN T_CH_PET_TUTOR pt ON pt.id_pet = p.id_pet JOIN T_CH_TUTOR tu ON tu.id_tutor = pt.id_tutor JOIN T_CH_USUARIO us ON us.id_usuario = tu.id_usuario, T_CH_CLINICA c WHERE p.nm_pet = 'Mia' AND us.ds_email = 'tutor@zelo.com.br' AND c.nr_cnpj = '12345678000190' AND NOT EXISTS (SELECT 1 FROM T_CH_ALERTA x WHERE x.id_pet = p.id_pet AND x.nm_alerta = 'Vacina V4 anual');
INSERT INTO T_CH_ALERTA (id_pet, id_clinica, tp_alerta, nm_alerta, ds_alerta, dt_prevista, dh_envio, st_alerta)
SELECT p.id_pet, c.id_clinica, 'CHECKUP', 'Check-up anual', 'Exames de sangue e avaliação geral.', CURRENT_DATE - INTERVAL '18' DAY, CURRENT_TIMESTAMP - INTERVAL '1' DAY, 'CONCLUIDO' FROM T_CH_PET p JOIN T_CH_PET_TUTOR pt ON pt.id_pet = p.id_pet JOIN T_CH_TUTOR tu ON tu.id_tutor = pt.id_tutor JOIN T_CH_USUARIO us ON us.id_usuario = tu.id_usuario, T_CH_CLINICA c WHERE p.nm_pet = 'Rex' AND us.ds_email = 'tutor@zelo.com.br' AND c.nr_cnpj = '12345678000190' AND NOT EXISTS (SELECT 1 FROM T_CH_ALERTA x WHERE x.id_pet = p.id_pet AND x.nm_alerta = 'Check-up anual');
INSERT INTO T_CH_ALERTA (id_pet, id_clinica, tp_alerta, nm_alerta, ds_alerta, dt_prevista, dh_envio, st_alerta)
SELECT p.id_pet, NULL, 'VACINA', 'Antipulgas mensal', 'Aplicar a pipeta antipulgas.', CURRENT_DATE + INTERVAL '1' DAY, NULL, 'PENDENTE' FROM T_CH_PET p JOIN T_CH_PET_TUTOR pt ON pt.id_pet = p.id_pet JOIN T_CH_TUTOR tu ON tu.id_tutor = pt.id_tutor JOIN T_CH_USUARIO us ON us.id_usuario = tu.id_usuario WHERE p.nm_pet = 'Rex' AND us.ds_email = 'tutor@zelo.com.br' AND NOT EXISTS (SELECT 1 FROM T_CH_ALERTA x WHERE x.id_pet = p.id_pet AND x.nm_alerta = 'Antipulgas mensal');
INSERT INTO T_CH_ALERTA (id_pet, id_clinica, tp_alerta, nm_alerta, ds_alerta, dt_prevista, dh_envio, st_alerta)
SELECT p.id_pet, NULL, 'CHECKUP', 'Pesar a Mia', 'Controle de peso caseiro.', CURRENT_DATE - INTERVAL '4' DAY, NULL, 'PENDENTE' FROM T_CH_PET p JOIN T_CH_PET_TUTOR pt ON pt.id_pet = p.id_pet JOIN T_CH_TUTOR tu ON tu.id_tutor = pt.id_tutor JOIN T_CH_USUARIO us ON us.id_usuario = tu.id_usuario WHERE p.nm_pet = 'Mia' AND us.ds_email = 'tutor@zelo.com.br' AND NOT EXISTS (SELECT 1 FROM T_CH_ALERTA x WHERE x.id_pet = p.id_pet AND x.nm_alerta = 'Pesar a Mia');
INSERT INTO T_CH_ALERTA (id_pet, id_clinica, tp_alerta, nm_alerta, ds_alerta, dt_prevista, dh_envio, st_alerta)
SELECT p.id_pet, NULL, 'VACINA', 'Vermífugo', 'Dar o comprimido de vermífugo.', CURRENT_DATE + INTERVAL '3' DAY, NULL, 'PENDENTE' FROM T_CH_PET p JOIN T_CH_PET_TUTOR pt ON pt.id_pet = p.id_pet JOIN T_CH_TUTOR tu ON tu.id_tutor = pt.id_tutor JOIN T_CH_USUARIO us ON us.id_usuario = tu.id_usuario WHERE p.nm_pet = 'Luna' AND us.ds_email = 'tutor2@zelo.com.br' AND NOT EXISTS (SELECT 1 FROM T_CH_ALERTA x WHERE x.id_pet = p.id_pet AND x.nm_alerta = 'Vermífugo');
