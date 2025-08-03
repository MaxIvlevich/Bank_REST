CREATE TABLE user_profiles (
                               id UUID PRIMARY KEY,
                               first_name VARCHAR(50),
                               last_name VARCHAR(50),
                               email VARCHAR(255) UNIQUE,
                               phone_number VARCHAR(20),
                               CONSTRAINT fk_user_profiles_on_user FOREIGN KEY (id) REFERENCES users (id)
);