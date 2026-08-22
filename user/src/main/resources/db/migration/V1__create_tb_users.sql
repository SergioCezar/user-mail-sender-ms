CREATE TABLE IF NOT EXISTS tb_users (
    id UUID NOT NULL,
    name VARCHAR(255),
    email VARCHAR(255),
    CONSTRAINT pk_tb_users PRIMARY KEY (id)
);
