
-- Usuarios

    INSERT INTO IWUSER (id, coins, enabled, first_name, last_name, password, reports, roles, username, team_id)
    VALUES (1, 0, TRUE, 'a', 'a', '{bcrypt}$2a$10$2BpNTbrsarbHjNsUWgzfNubJqBRf.0Vz9924nRSHBqlbPKerkgX.W', 0, 'ADMIN,USER', 'a', null);

    INSERT INTO IWUSER (id, coins, enabled, first_name, last_name, password, reports, roles, username, team_id)
    VALUES (2, 0, TRUE, 'b', 'b', '{bcrypt}$2a$10$2BpNTbrsarbHjNsUWgzfNubJqBRf.0Vz9924nRSHBqlbPKerkgX.W', 0, 'USER', 'b', null);

    INSERT INTO IWUSER (id, coins, enabled, first_name, last_name, password, reports, roles, username, team_id)
    VALUES (3, 0, TRUE, 'c', 'c', '{bcrypt}$2a$10$2BpNTbrsarbHjNsUWgzfNubJqBRf.0Vz9924nRSHBqlbPKerkgX.W', 0, 'USER', 'c', null);

    INSERT INTO IWUSER (id, coins, enabled, first_name, last_name, password, reports, roles, username, team_id)
    VALUES (4, 0, TRUE, 'd', 'd', '{bcrypt}$2a$10$2BpNTbrsarbHjNsUWgzfNubJqBRf.0Vz9924nRSHBqlbPKerkgX.W', 0, 'USER', 'd', null);

    INSERT INTO IWUSER (id, coins, enabled, first_name, last_name, password, reports, roles, username, team_id)
    VALUES (5, 0, TRUE, 'e', 'e', '{bcrypt}$2a$10$2BpNTbrsarbHjNsUWgzfNubJqBRf.0Vz9924nRSHBqlbPKerkgX.W', 0, 'USER', 'e', null);

    INSERT INTO IWUSER (id, coins, enabled, first_name, last_name, password, reports, roles, username, team_id)
    VALUES (6, 0, TRUE, 'f', 'f', '{bcrypt}$2a$10$2BpNTbrsarbHjNsUWgzfNubJqBRf.0Vz9924nRSHBqlbPKerkgX.W', 0, 'USER', 'f', null);

    INSERT INTO IWUSER (id, coins, enabled, first_name, last_name, password, reports, roles, username, team_id)
    VALUES (7, 0, TRUE, 'g', 'g', '{bcrypt}$2a$10$2BpNTbrsarbHjNsUWgzfNubJqBRf.0Vz9924nRSHBqlbPKerkgX.W', 0, 'USER', 'g', null);


-- Topics

    INSERT INTO "PUBLIC"."MESSAGE_TOPIC" VALUES(1975, 'PixE9Gxf');  

    INSERT INTO "PUBLIC"."MESSAGE_TOPIC" VALUES(1976, 'PixE9Gxa');  

    INSERT INTO "PUBLIC"."MESSAGE_TOPIC" VALUES(1977, 'PixE9Gxd');


-- Tournaments

    INSERT INTO Tournament (id, date, description, entry_price, game, max_teams, name, prize_pool, rounds, starting_hour, status, type, message_topic_id, winner_id)
    VALUES (10, '2023-04-27', 'Torneo de Valorant', 3.0, 'Valorant', 4, 'VCT', 2.0, 3, '12:00', 0, 0, 1975, null);   

    INSERT INTO Tournament (id, date, description, entry_price, game, max_teams, name, prize_pool, rounds, starting_hour, status, type, message_topic_id, winner_id)
    VALUES (11, '2023-04-28', 'Torneo de Apex', 3.0, 'Apex', 4, 'APX', 2.0, 3, '13:00', 1, 0, 1976, null);

    INSERT INTO Tournament (id, date, description, entry_price, game, max_teams, name, prize_pool, rounds, starting_hour, status, type, message_topic_id, winner_id)
    VALUES (12, '2023-04-29', 'Torneo de COD', 3.0, 'COD', 4, 'COD', 2.0, 3, '14:00', 2, 0, 1977, null);


-- Teams

    INSERT INTO Team (id, name)
    VALUES (20, 'Primer Equipo');

    INSERT INTO Team (id, name)
    VALUES (21, 'Segundo Equipo');

    INSERT INTO Team (id, name)
    VALUES (22, 'Tercer Equipo');

    INSERT INTO Team (id, name)
    VALUES (23, 'Cuarto Equipo');

    INSERT INTO Team (id, name)
    VALUES (24, 'Quinto Equipo');

    INSERT INTO Team (id, name)
    VALUES (25, 'Sexto Equipo');  


-- Relaciones (TeamMember y TournamentTeam)

    -- TeamMembers
    
    INSERT INTO Team_Member (id, is_coach, team_id, user_id)
    VALUES (30, 1, 20, 1);  

    INSERT INTO Team_Member (id, is_coach, team_id, user_id)
    VALUES (31, 1, 21, 2);  

    INSERT INTO Team_Member (id, is_coach, team_id, user_id)
    VALUES (32, 1, 22, 3);  

    INSERT INTO Team_Member (id, is_coach, team_id, user_id)
    VALUES (33, 1, 23, 4);  

    INSERT INTO Team_Member (id, is_coach, team_id, user_id)
    VALUES (34, 1, 24, 5);  

    INSERT INTO Team_Member (id, is_coach, team_id, user_id)
    VALUES (35, 0, 25, 6);  

    INSERT INTO Team_Member (id, is_coach, team_id, user_id)
    VALUES (36, 0, 20, 7);  


    -- TournamentTeams

    INSERT INTO TOURNAMENT_TEAM (id, derrotas, empates, puntuacion, victorias, team_id, tournament_id)
    VALUES (40, null, null, null, null, 20, 10);

    INSERT INTO TOURNAMENT_TEAM (id, derrotas, empates, puntuacion, victorias, team_id, tournament_id)
    VALUES (41, null, null, null, null, 21, 10);

    INSERT INTO TOURNAMENT_TEAM (id, derrotas, empates, puntuacion, victorias, team_id, tournament_id)
    VALUES (42, null, null, null, null, 22, 10);

    INSERT INTO TOURNAMENT_TEAM (id, derrotas, empates, puntuacion, victorias, team_id, tournament_id)
    VALUES (43, null, null, null, null, 23, 10);

    INSERT INTO TOURNAMENT_TEAM (id, derrotas, empates, puntuacion, victorias, team_id, tournament_id)
    VALUES (44, null, null, null, null, 24, 11);

    INSERT INTO TOURNAMENT_TEAM (id, derrotas, empates, puntuacion, victorias, team_id, tournament_id)
    VALUES (45, null, null, null, null, 25, 11);

    INSERT INTO TOURNAMENT_TEAM (id, derrotas, empates, puntuacion, victorias, team_id, tournament_id)
    VALUES (46, null, null, null, null, 20, 12);

    INSERT INTO TOURNAMENT_TEAM (id, derrotas, empates, puntuacion, victorias, team_id, tournament_id)
    VALUES (47, null, null, null, null, 22, 12);

    INSERT INTO TOURNAMENT_TEAM (id, derrotas, empates, puntuacion, victorias, team_id, tournament_id)
    VALUES (48, null, null, null, null, 24, 12);


-- Apaños

    UPDATE IWUSER
    SET TEAM_ID = 20
    WHERE ID = 1;

    UPDATE IWUSER
    SET TEAM_ID = 21
    WHERE ID = 2;

    UPDATE IWUSER
    SET TEAM_ID = 22
    WHERE ID = 3;

    UPDATE IWUSER
    SET TEAM_ID = 23
    WHERE ID = 4;

    UPDATE IWUSER
    SET TEAM_ID = 24
    WHERE ID = 5;

    UPDATE IWUSER
    SET TEAM_ID = 25
    WHERE ID = 6;

    UPDATE IWUSER
    SET TEAM_ID = 20
    WHERE ID = 7;

    -- start id numbering from a value that is larger than any assigned above
    ALTER SEQUENCE "PUBLIC"."GEN" RESTART WITH 1024;