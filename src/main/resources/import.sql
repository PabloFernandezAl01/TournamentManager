
    -- TEAMS

    INSERT INTO Team (id, name)
    VALUES (1, 'Primer Equipo');

    INSERT INTO Team (id, name)
    VALUES (2, 'Segundo Equipo');
    
    -- Usuarios

    INSERT INTO IWUSER (id, coins, enabled, first_name, last_name, password, reports, roles, username, team_id)
    VALUES (1, 0, TRUE, 'User', '1', '{bcrypt}$2a$10$2BpNTbrsarbHjNsUWgzfNubJqBRf.0Vz9924nRSHBqlbPKerkgX.W', 0, 'ADMIN,USER', 'User1', 1);

    INSERT INTO IWUSER (id, coins, enabled, first_name, last_name, password, reports, roles, username, team_id)
    VALUES (2, 0, TRUE, 'User', '2', '{bcrypt}$2a$10$2BpNTbrsarbHjNsUWgzfNubJqBRf.0Vz9924nRSHBqlbPKerkgX.W', 0, 'USER', 'User2', 1);

    INSERT INTO IWUSER (id, coins, enabled, first_name, last_name, password, reports, roles, username, team_id)
    VALUES (3, 0, TRUE, 'User', '3', '{bcrypt}$2a$10$2BpNTbrsarbHjNsUWgzfNubJqBRf.0Vz9924nRSHBqlbPKerkgX.W', 0, 'USER', 'User3', 1);

    INSERT INTO IWUSER (id, coins, enabled, first_name, last_name, password, reports, roles, username, team_id)
    VALUES (4, 0, TRUE, 'User', '4', '{bcrypt}$2a$10$2BpNTbrsarbHjNsUWgzfNubJqBRf.0Vz9924nRSHBqlbPKerkgX.W', 0, 'USER', 'User4', 1);

    INSERT INTO IWUSER (id, coins, enabled, first_name, last_name, password, reports, roles, username, team_id)
    VALUES (5, 0, TRUE, 'User', '5', '{bcrypt}$2a$10$2BpNTbrsarbHjNsUWgzfNubJqBRf.0Vz9924nRSHBqlbPKerkgX.W', 0, 'USER', 'User5', 2);

    INSERT INTO IWUSER (id, coins, enabled, first_name, last_name, password, reports, roles, username, team_id)
    VALUES (6, 0, TRUE, 'User', '6', '{bcrypt}$2a$10$2BpNTbrsarbHjNsUWgzfNubJqBRf.0Vz9924nRSHBqlbPKerkgX.W', 0, 'USER', 'User6', 2);

    INSERT INTO IWUSER (id, coins, enabled, first_name, last_name, password, reports, roles, username, team_id)
    VALUES (7, 0, TRUE, 'User', '7', '{bcrypt}$2a$10$2BpNTbrsarbHjNsUWgzfNubJqBRf.0Vz9924nRSHBqlbPKerkgX.W', 0, 'USER', 'User7', 2);

    INSERT INTO IWUSER (id, coins, enabled, first_name, last_name, password, reports, roles, username, team_id)
    VALUES (8, 0, TRUE, 'User', '8', '{bcrypt}$2a$10$2BpNTbrsarbHjNsUWgzfNubJqBRf.0Vz9924nRSHBqlbPKerkgX.W', 0, 'USER', 'User8', 2);

    INSERT INTO IWUSER (id, coins, enabled, first_name, last_name, password, reports, roles, username, team_id)
    VALUES (9, 0, TRUE, 'User', '9', '{bcrypt}$2a$10$2BpNTbrsarbHjNsUWgzfNubJqBRf.0Vz9924nRSHBqlbPKerkgX.W', 0, 'USER', 'User9', null);

    -- TEAM MEMBERS
        -- TEAM 1

        INSERT INTO Team_Member (id, is_coach, team_id, user_id)
        VALUES (1, 1, 1, 1);  

        INSERT INTO Team_Member (id, is_coach, team_id, user_id)
        VALUES (2, 0, 1, 2);  

        INSERT INTO Team_Member (id, is_coach, team_id, user_id)
        VALUES (3, 0, 1, 3);  

        INSERT INTO Team_Member (id, is_coach, team_id, user_id)
        VALUES (4, 0, 1, 4);  

        -- TEAM 2

        INSERT INTO Team_Member (id, is_coach, team_id, user_id)
        VALUES (5, 1, 2, 5);  

        INSERT INTO Team_Member (id, is_coach, team_id, user_id)
        VALUES (6, 0, 2, 6);  

        INSERT INTO Team_Member (id, is_coach, team_id, user_id)
        VALUES (7, 0, 2, 7);  

        INSERT INTO Team_Member (id, is_coach, team_id, user_id)
        VALUES (8, 0, 2, 8);  


    -- MESSAGE TOPICS

    INSERT INTO Message_Topic (id, topic_id)
    VALUES (1, '123456');

    INSERT INTO Message_Topic (id, topic_id)
    VALUES (2, '123457');

    INSERT INTO Message_Topic (id, topic_id)
    VALUES (3, '123458');

    INSERT INTO Message_Topic (id, topic_id)
    VALUES (4, '123459');

    -- TORNEOS

        -- NO EMPEZADOS

        INSERT INTO Tournament (id, date, description, entry_price, game, max_teams, name, prize_pool, rounds, starting_hour, status, type, message_topic_id, winner_id)
        VALUES (1, '2023-09-27', 'Torneo de Valorant', 3.0, 'Valorant', 2, 'VCT', 2.0, 2, '12:00', 0, 0, 1, null);   

        INSERT INTO Tournament (id, date, description, entry_price, game, max_teams, name, prize_pool, rounds, starting_hour, status, type, message_topic_id, winner_id)
        VALUES (2, '2023-09-28', 'Torneo de Apex', 3.0, 'Apex', 2, 'APX', 2.0, 3, '13:00', 0, 1, 2, null);

        -- EMPEZADOS

        INSERT INTO Tournament (id, date, description, entry_price, game, max_teams, name, prize_pool, rounds, starting_hour, status, type, message_topic_id, winner_id)
        VALUES (3, '2023-03-27', 'Torneo de COD', 3.0, 'Call of duty', 2, 'COD', 2.0, 2, '12:00', 0, 0, 3, null);   

        INSERT INTO Tournament (id, date, description, entry_price, game, max_teams, name, prize_pool, rounds, starting_hour, status, type, message_topic_id, winner_id)
        VALUES (4, '2023-03-28', 'Torneo de LoL', 3.0, 'League of Legends', 2, 'LOL', 2.0, 3, '13:00', 0, 1, 4, null);


    --TOURNAMENT TEAMS

    INSERT INTO TOURNAMENT_TEAM (id, derrotas, empates, puntuacion, victorias, team_id, tournament_id)
    VALUES (1, 0, 0, 0, 0, 1, 3);

    INSERT INTO TOURNAMENT_TEAM (id, derrotas, empates, puntuacion, victorias, team_id, tournament_id)
    VALUES (2, 0, 0, 0, 0, 2, 3);

    INSERT INTO TOURNAMENT_TEAM (id, derrotas, empates, puntuacion, victorias, team_id, tournament_id)
    VALUES (3, 0, 0, 0, 0, 1, 4);

    INSERT INTO TOURNAMENT_TEAM (id, derrotas, empates, puntuacion, victorias, team_id, tournament_id)
    VALUES (4, 0, 0, 0, 0, 2, 4);

    -- start id numbering from a value that is larger than any assigned above
    ALTER SEQUENCE "PUBLIC"."GEN" RESTART WITH 1024;