CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE users (
                       id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                       username VARCHAR(255) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       is_account_non_expired BOOLEAN NOT NULL DEFAULT TRUE,
                       is_account_non_locked BOOLEAN NOT NULL DEFAULT TRUE,
                       is_credentials_non_expired BOOLEAN NOT NULL DEFAULT TRUE,
                       is_active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE user_roles (
                            user_id UUID NOT NULL,
                            role VARCHAR(50) NOT NULL,
                            CONSTRAINT pk_user_roles PRIMARY KEY (user_id, role),
                            CONSTRAINT fk_user_roles_on_user FOREIGN KEY (user_id) REFERENCES users (id)
);
