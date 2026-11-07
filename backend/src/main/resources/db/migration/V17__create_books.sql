-- O acervo: um livro existe uma vez, e todas as estantes apontam para ele.
-- Sem sinopse de proposito: um TEXT de sinopse pode pesar metade da linha,
-- e o free tier do Supabase tem 500 MB. Sinopse vem sob demanda na tela do livro.

CREATE TABLE books (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    isbn13 VARCHAR(13),

    isbn10 VARCHAR(10),

    -- Chave da obra, nao da edicao. A Open Library fornece no dump.
    -- Fica nula por enquanto; serve para agrupar edicoes no Ranking.
    work_key VARCHAR(40),

    title VARCHAR(300) NOT NULL,

    -- Sem acento, minusculo, sem subtitulo. Segunda rede contra duplicata,
    -- para quem cadastra sem ISBN.
    title_normalized VARCHAR(300) NOT NULL,

    subtitle VARCHAR(300),

    publisher VARCHAR(160),

    -- Ano DESTA edicao
    published_year INTEGER,

    -- Ano da obra original. Pode ser muito anterior ao published_year.
    first_publish_year INTEGER,

    pages INTEGER,

    language VARCHAR(8),

    cover_url VARCHAR(500),

    country VARCHAR(2),

    literary_genre VARCHAR(40),

    -- OPEN_LIBRARY, GOOGLE_BOOKS ou USER
    source VARCHAR(20) NOT NULL,

    -- Id no sistema de origem. Nulo quando o livro nasceu aqui.
    external_id VARCHAR(80),

    -- APPROVED ou PENDING. Livro cadastrado por leitora entra PENDING.
    review_status VARCHAR(20) NOT NULL DEFAULT 'APPROVED',

    -- Nulo nos livros que vieram de importacao
    created_by UUID,

    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    -- No Postgres, UNIQUE aceita varios nulos. Livro sem ISBN nao conflita.
    CONSTRAINT uk_books_isbn13
        UNIQUE (isbn13),

    CONSTRAINT fk_books_created_by
        FOREIGN KEY (created_by)
        REFERENCES users(id)
);


CREATE INDEX idx_books_title_normalized
    ON books (title_normalized);

CREATE INDEX idx_books_work_key
    ON books (work_key);

-- Impede importar o mesmo registro de origem duas vezes
CREATE UNIQUE INDEX uk_books_source_external
    ON books (source, external_id)
    WHERE external_id IS NOT NULL;


CREATE TABLE book_authors (
    book_id UUID NOT NULL,

    author VARCHAR(255) NOT NULL,

    CONSTRAINT fk_book_authors_book
        FOREIGN KEY (book_id)
        REFERENCES books(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_book_authors_book
    ON book_authors (book_id);
