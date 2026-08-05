CREATE TABLE IF NOT EXISTS auth_users (
    id              BIGSERIAL PRIMARY KEY,
    username        VARCHAR(100) NOT NULL UNIQUE,
    email           VARCHAR(150) NOT NULL UNIQUE,
    password        VARCHAR(255) NOT NULL,
    role            VARCHAR(50)  NOT NULL
                    CHECK (role IN (
                        'ADMIN', 'HR_USER', 'PROCUREMENT_OFFICER', 'INVENTORY_OFFICER',
                        'SALES_OFFICER', 'FINANCE_OFFICER', 'EMPLOYEE'
                    )),
    enabled         BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP,
    updated_at      TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_auth_users_username ON auth_users (username);
CREATE INDEX IF NOT EXISTS idx_auth_users_email ON auth_users (email);
