-- insert admin (username a, password aa)
INSERT INTO IWUSER (id, earned, enabled, first_name, last_name, password,
reports, roles, username, team_id)
VALUES (1, 0, TRUE, 'a', 'a',
    '{bcrypt}$2a$10$2BpNTbrsarbHjNsUWgzfNubJqBRf.0Vz9924nRSHBqlbPKerkgX.W',
    0,'ADMIN,USER', 'a', null);
INSERT INTO IWUser (id, earned, enabled, first_name, last_name, password,
reports, roles, username, team_id)
VALUES (2, 0, TRUE,'b','b', '{bcrypt}$2a$10$2BpNTbrsarbHjNsUWgzfNubJqBRf.0Vz9924nRSHBqlbPKerkgX.W',0,'USER', 'b',null);  
INSERT INTO IWUser (id, earned, enabled, first_name, last_name, password,
reports, roles, username, team_id)
VALUES (3, 0, TRUE,'c','c', '{bcrypt}$2a$10$2BpNTbrsarbHjNsUWgzfNubJqBRf.0Vz9924nRSHBqlbPKerkgX.W',0,'USER', 'c',null);
INSERT INTO IWUser (id, earned, enabled, first_name, last_name, password,
reports, roles, username, team_id)
VALUES (4, 0, TRUE,'d','d', '{bcrypt}$2a$10$2BpNTbrsarbHjNsUWgzfNubJqBRf.0Vz9924nRSHBqlbPKerkgX.W',0,'USER', 'd',null);
INSERT INTO IWUser (id, earned, enabled, first_name, last_name, password,
reports, roles, username, team_id)
VALUES (5, 0, TRUE,'e','e', '{bcrypt}$2a$10$2BpNTbrsarbHjNsUWgzfNubJqBRf.0Vz9924nRSHBqlbPKerkgX.W',0,'USER', 'e',null);
INSERT INTO IWUser (id, earned, enabled, first_name, last_name, password,
reports, roles, username, team_id)
VALUES (6, 0, TRUE,'f','f', '{bcrypt}$2a$10$2BpNTbrsarbHjNsUWgzfNubJqBRf.0Vz9924nRSHBqlbPKerkgX.W',0,'USER', 'f',null);

INSERT INTO Tournament (id, date, description, entry_price, game, max_teams,
name, prize_pool, rounds, status, type, winner_id)
VALUES (1026, '2023-03-27', 'descripcion del torneo', 3.0, 1, 4, 'Torneo Prueba', 2.0, 3, 1, 0, null);  

INSERT INTO Tournament (id, date, description, entry_price, game, max_teams,
name, prize_pool, rounds, status, type, winner_id)
VALUES (1027, '2023-04-27', 'descripcion del torneo 2', 3.0, 1, 4, 'Torneo Prueba 2', 2.0, 3, 1, 2, null);  

INSERT INTO Team (id, name, coach_id)
VALUES (999, 'Primer Equipo', 1);  

INSERT INTO Team (id, name, coach_id)
VALUES (1000, 'Segundo Equipo', 2);  

INSERT INTO Team (id, name, coach_id)
VALUES (1001, 'Tercer Equipo', 3);  

INSERT INTO Team (id, name, coach_id)
VALUES (1002, 'Cuarto Equipo', 4);

INSERT INTO Team (id, name, coach_id)
VALUES (1003, 'Quinto Equipo', 5);

INSERT INTO Team (id, name, coach_id)
VALUES (1004, 'Sexto Equipo', 6);

INSERT INTO Team_Member (id, role, team_id, user_id)
VALUES (13234, 1, 999, 1);  

INSERT INTO Team_Member (id, role, team_id, user_id)
VALUES (13235, 1, 1000, 2);  

INSERT INTO Team_Member (id, role, team_id, user_id)
VALUES (13236, 1, 1001, 3);  

INSERT INTO Team_Member (id, role, team_id, user_id)
VALUES (13237, 1, 1002, 4);  

INSERT INTO Team_Member (id, role, team_id, user_id)
VALUES (13238, 1, 1003, 5);  

INSERT INTO Team_Member (id, role, team_id, user_id)
VALUES (13238, 1, 1004, 6);  

UPDATE IWUSER
SET TEAM_ID = 999
WHERE ID = 1;

UPDATE IWUSER
SET TEAM_ID = 1000
WHERE ID = 2;

UPDATE IWUSER
SET TEAM_ID = 1001
WHERE ID = 3;

UPDATE IWUSER
SET TEAM_ID = 1002
WHERE ID = 4;

UPDATE IWUSER
SET TEAM_ID = 1003
WHERE ID = 5;

UPDATE IWUSER
SET TEAM_ID = 1004
WHERE ID = 6;


INSERT INTO TOURNAMENT_TEAM  (id, team_id, tournament_id , puntuacion, victorias, empates, derrotas)
VALUES (1, 999, 1026, null, null, null, null);

INSERT INTO TOURNAMENT_TEAM  (id, team_id, tournament_id , puntuacion, victorias, empates, derrotas)
VALUES (2, 1000, 1026, null, null, null, null); 

INSERT INTO TOURNAMENT_TEAM  (id, team_id, tournament_id, puntuacion, victorias, empates, derrotas)
VALUES (3, 1001, 1026, null, null, null, null); 

INSERT INTO TOURNAMENT_TEAM  (id, team_id, tournament_id , puntuacion, victorias, empates, derrotas)
VALUES (4, 1002, 1026, null, null, null, null); 

INSERT INTO TOURNAMENT_TEAM  (id, team_id, tournament_id , puntuacion, victorias, empates, derrotas)
VALUES (5, 999, 1027, 0, 0, 0 ,0); 

INSERT INTO TOURNAMENT_TEAM  (id, team_id, tournament_id, puntuacion, victorias, empates, derrotas)
VALUES (6, 1000, 1027, 0, 0, 0 ,0); 

INSERT INTO TOURNAMENT_TEAM  (id, team_id, tournament_id, puntuacion, victorias, empates, derrotas)
VALUES (7, 1001, 1027, 0, 0, 0 ,0); 

INSERT INTO TOURNAMENT_TEAM  (id, team_id, tournament_id, puntuacion, victorias, empates, derrotas)
VALUES (8, 1003, 1027, 0, 0, 0 ,0); 

INSERT INTO TOURNAMENT_TEAM  (id, team_id, tournament_id, puntuacion, victorias, empates, derrotas)
VALUES (9, 1004, 1026,null, null, null, null); 

-- INSERT INTO MATCH (id, match_number, result, round_number, team1_id, team2_id, tournament_id, winner_id)
-- VALUES (1, 1, 'TBD', 1, 999, 1000, 1026, null);

-- INSERT INTO MATCH (id, match_number, result, round_number, team1_id, team2_id, tournament_id, winner_id)
-- VALUES (2, 1, 'TBD', 1, 1001, 1002, 1026, null);

-- start id numbering from a value that is larger than any assigned above
ALTER SEQUENCE "PUBLIC"."GEN" RESTART WITH 1024;

