CREATE TABLE roles (
                       id BIGSERIAL PRIMARY KEY,
                       name VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE users (
                       id BIGSERIAL PRIMARY KEY,
                       username VARCHAR(255) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL
);

CREATE TABLE users_roles (
                             user_id BIGINT NOT NULL,
                             role_id BIGINT NOT NULL,
                             PRIMARY KEY (user_id, role_id),
                             FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
                             FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE
);

CREATE TABLE categories (
                            id BIGSERIAL PRIMARY KEY,
                            name VARCHAR(100) NOT NULL UNIQUE,
                            description TEXT
);

CREATE TABLE accounts (
                          id BIGSERIAL PRIMARY KEY,
                          name VARCHAR(255) NOT NULL,
                          balance DECIMAL(19,2) NOT NULL,
                          user_id BIGINT NOT NULL,
                          FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE transactions (
                              id BIGSERIAL PRIMARY KEY,
                              amount DECIMAL(19,2) NOT NULL,
                              type VARCHAR(20) NOT NULL,
                              description TEXT,
                              date TIMESTAMP NOT NULL,
                              account_id BIGINT NOT NULL,
                              category_id BIGINT,
                              user_id BIGINT NOT NULL,
                              FOREIGN KEY (account_id) REFERENCES accounts (id) ON DELETE CASCADE,
                              FOREIGN KEY (category_id) REFERENCES categories (id) ON DELETE SET NULL,
                              FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE budgets (
                         id BIGSERIAL PRIMARY KEY,
                         amount DECIMAL(19,2) NOT NULL,
                         start_date DATE NOT NULL,
                         end_date DATE NOT NULL,
                         category_id BIGINT NOT NULL,
                         user_id BIGINT NOT NULL,
                         FOREIGN KEY (category_id) REFERENCES categories (id) ON DELETE CASCADE,
                         FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);