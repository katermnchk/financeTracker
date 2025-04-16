ALTER TABLE IF EXISTS users
    ADD COLUMN IF NOT EXISTS email VARCHAR(255);

UPDATE users
SET email = 'temp_' || id || '@example.com'
WHERE email IS NULL;

ALTER TABLE users
    ALTER COLUMN email SET NOT NULL;

ALTER TABLE users
    ADD CONSTRAINT unique_email UNIQUE (email);