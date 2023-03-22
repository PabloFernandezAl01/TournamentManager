-- insert admin (username a, password aa)
INSERT INTO IWUSER (id, earned, enabled, first_name, last_name, password,
reports, roles, username, team_id)
VALUES (1, 0, TRUE, 'a', 'a',
    '{bcrypt}$2a$10$2BpNTbrsarbHjNsUWgzfNubJqBRf.0Vz9924nRSHBqlbPKerkgX.W',
    0,'ADMIN,USER', 'a', null);
INSERT INTO IWUser (id, earned, enabled, first_name, last_name, password,
reports, roles, username, team_id)
VALUES (2, 0, TRUE,'b','b', '{bcrypt}$2a$10$2BpNTbrsarbHjNsUWgzfNubJqBRf.0Vz9924nRSHBqlbPKerkgX.W',0,'USER', 'b',null);  

-- start id numbering from a value that is larger than any assigned above
ALTER SEQUENCE "PUBLIC"."GEN" RESTART WITH 1024;

