CREATE TABLE refresh_tokens (
                                id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                token VARCHAR(255) NOT NULL UNIQUE,
                                expiry_date TIMESTAMP WITH TIME ZONE NOT NULL,
                                user_id UUID NOT NULL UNIQUE,
                                CONSTRAINT fk_refresh_tokens_on_user FOREIGN KEY (user_id) REFERENCES users (id)
);