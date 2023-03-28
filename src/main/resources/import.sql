-- insert admin (username a, password aa)
INSERT INTO IWUSER (id, earned, enabled, first_name, last_name, password,
reports, roles, username, team_id)
VALUES (1, 0, TRUE, 'a', 'a',
    '{bcrypt}$2a$10$2BpNTbrsarbHjNsUWgzfNubJqBRf.0Vz9924nRSHBqlbPKerkgX.W',
    0,'ADMIN,USER', 'a', null);
INSERT INTO IWUser (id, earned, enabled, first_name, last_name, password,
reports, roles, username, team_id)
VALUES (2, 0, TRUE,'b','b', '{bcrypt}$2a$10$2BpNTbrsarbHjNsUWgzfNubJqBRf.0Vz9924nRSHBqlbPKerkgX.W',0,'USER', 'b',null);  

INSERT INTO Tournament (id, date, description, entry_price, game, max_teams,
name, prize_pool, rounds, status, type, winner_id)
VALUES (1026, '2023-03-27', 'descripcion del torneo', 3.0, 1, 6, 'Torneo Prueba', 2.0, 0, 0, 1, null);  

INSERT INTO Team (id, name, coach_id)
VALUES (999, 'Equipo prueba', 1);  

INSERT INTO Team_Member (id, role, team_id, user_id)
VALUES (13234, 1, 999, 1);  
-- start id numbering from a value that is larger than any assigned above
ALTER SEQUENCE "PUBLIC"."GEN" RESTART WITH 1024;

