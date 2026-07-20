CREATE TABLE user_credentials
(
    user_id                     UUID PRIMARY KEY,

    password_hash               TEXT NOT NULL,

    failed_login_attempts       INTEGER NOT NULL DEFAULT 0,

    CONSTRAINT fk_user_credentials_user
        FOREIGN KEY (user_id)
            REFERENCES users(id)
            ON DELETE CASCADE
);