-- Apagar uma usuaria passa a levar junto os dados dela.
-- As tres tabelas antigas bloqueavam a exclusao; password_reset_tokens e
-- email_change_requests ja nasceram com cascata.

ALTER TABLE library_books DROP CONSTRAINT fk_library_books_user;
ALTER TABLE library_books ADD CONSTRAINT fk_library_books_user
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

ALTER TABLE reading_days DROP CONSTRAINT fk_reading_days_user;
ALTER TABLE reading_days ADD CONSTRAINT fk_reading_days_user
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

ALTER TABLE annual_goals DROP CONSTRAINT fk_annual_goals_user;
ALTER TABLE annual_goals ADD CONSTRAINT fk_annual_goals_user
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
