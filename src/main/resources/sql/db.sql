CREATE DATABASE mini_dish_db;

CREATE USER mini_dish_db_manager WITH PASSWORD '123456';

GRANT CONNECT ON DATABASE mini_dish_db TO mini_dish_db_manager;

\c mini_dish_db

GRANT CREATE ON SCHEMA public TO mini_dish_db_manager;

ALTER DEFAULT PRIVILEGES IN SCHEMA public
    GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO mini_dish_db_manager;

-- Give user permission to use sequences
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO mini_dish_db_manager;
