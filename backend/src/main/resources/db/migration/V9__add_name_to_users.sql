ALTER TABLE users ADD COLUMN name VARCHAR(80);

-- As contas que já existem herdam o nome de usuário como nome
UPDATE users SET name = username WHERE name IS NULL;

ALTER TABLE users ALTER COLUMN name SET NOT NULL;
