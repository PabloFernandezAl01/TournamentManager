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


INSERT INTO Tournament (id, date, description, entry_price, game, max_teams,
name, prize_pool, rounds, status, type, winner_id)
VALUES (1026, '2023-03-27', 'descripcion del torneo', 3.0, 1, 4, 'Torneo Prueba', 2.0, 3, 0, 1, null);  

INSERT INTO Team (id, name, coach_id)
VALUES (999, 'Primer Equipo', 1);  

INSERT INTO Team (id, name, coach_id)
VALUES (1000, 'Segundo Equipo', 2);  

INSERT INTO Team (id, name, coach_id)
VALUES (1001, 'Tercer Equipo', 3);  

INSERT INTO Team (id, name, coach_id)
VALUES (1002, 'Cuarto Equipo', 3);

INSERT INTO Team_Member (id, role, team_id, user_id)
VALUES (13234, 1, 999, 1);  

INSERT INTO Team_Member (id, role, team_id, user_id)
VALUES (13234, 1, 1000, 2);  

INSERT INTO Team_Member (id, role, team_id, user_id)
VALUES (13234, 1, 1001, 3);  

INSERT INTO Team_Member (id, role, team_id, user_id)
VALUES (13234, 1, 1002, 4);  

INSERT INTO TOURNAMENT_TEAM  (id, team_id, tournament_id)
VALUES (1, 999, 1026);

INSERT INTO TOURNAMENT_TEAM  (id, team_id, tournament_id)
VALUES (2, 1000, 1026); 

INSERT INTO TOURNAMENT_TEAM  (id, team_id, tournament_id)
VALUES (3, 1001, 1026); 

INSERT INTO TOURNAMENT_TEAM  (id, team_id, tournament_id)
VALUES (4, 1002, 1026); 

INSERT INTO MATCH (id, match_number, result, round_number, team1_id, team2_id, tournament_id, winner_id)
VALUES (1, 1, 'TBD', 1, 999, 1000, 1026, null);

INSERT INTO MATCH (id, match_number, result, round_number, team1_id, team2_id, tournament_id, winner_id)
VALUES (2, 1, 'TBD', 1, 1001, 1002, 1026, null);

-- start id numbering from a value that is larger than any assigned above
ALTER SEQUENCE "PUBLIC"."GEN" RESTART WITH 1024;

