CREATE TABLE cards (
                       id UUID PRIMARY KEY,
                       card_number VARCHAR(255) NOT NULL,
                       card_number_hash VARCHAR(255) NOT NULL,
                       expiration_date DATE NOT NULL,
                       status VARCHAR(20) NOT NULL,
                       balance NUMERIC(19, 2) NOT NULL DEFAULT 0.00,
                       active BOOLEAN NOT NULL DEFAULT TRUE,
                       user_id UUID NOT NULL,
                       CONSTRAINT fk_cards_on_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE UNIQUE INDEX idx_card_number_hash_unique ON cards(card_number_hash);