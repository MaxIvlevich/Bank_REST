CREATE TABLE cards (
                       id UUID PRIMARY KEY,
                       card_number VARCHAR(255) NOT NULL UNIQUE,
                       expiration_date DATE NOT NULL,
                       status VARCHAR(20) NOT NULL,
                       balance NUMERIC(19, 2) NOT NULL DEFAULT 0.00,
                       active BOOLEAN NOT NULL DEFAULT TRUE,
                       user_id UUID NOT NULL,
                       CONSTRAINT fk_cards_on_user FOREIGN KEY (user_id) REFERENCES users (id)
);