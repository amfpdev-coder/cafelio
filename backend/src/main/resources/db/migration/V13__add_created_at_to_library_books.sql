-- Os livros que ja existem recebem a data da migration: nao e a verdade,
-- mas poe todos na mesma ordem relativa em vez de deixar o campo vazio
ALTER TABLE library_books
    ADD COLUMN created_at TIMESTAMPTZ NOT NULL DEFAULT now();
