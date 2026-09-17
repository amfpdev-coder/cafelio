CREATE TABLE library_books (

    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    user_id UUID NOT NULL,

    open_library_id VARCHAR(255) NOT NULL,

    title VARCHAR(255) NOT NULL,

    first_publish_year INTEGER,

    cover_url VARCHAR(255),

    status VARCHAR(255) NOT NULL,

    CONSTRAINT fk_library_books_user
        FOREIGN KEY (user_id)
        REFERENCES users(id),

    CONSTRAINT uk_library_book_user_open_library
        UNIQUE (user_id, open_library_id)
);


CREATE TABLE library_book_authors (
    library_book_id UUID NOT NULL,

    author VARCHAR(255) NOT NULL,

    CONSTRAINT fk_library_book_authors_book
        FOREIGN KEY (library_book_id)
        REFERENCES library_books(id)
        ON DELETE CASCADE
);


CREATE TABLE library_book_tags (
    library_book_id UUID NOT NULL,

    tag VARCHAR(255) NOT NULL,

    CONSTRAINT fk_library_book_tags_book
        FOREIGN KEY (library_book_id)
        REFERENCES library_books(id)
        ON DELETE CASCADE,

    CONSTRAINT uk_library_book_tag
        UNIQUE (library_book_id, tag)
);