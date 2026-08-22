ALTER TABLE tb_users
    ADD CONSTRAINT uk_tb_users_email UNIQUE (email);
