CREATE TABLE reading_days (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    user_id UUID NOT NULL,

    day DATE NOT NULL,

    CONSTRAINT fk_reading_days_user
        FOREIGN KEY (user_id)
        REFERENCES users(id),

    CONSTRAINT uk_reading_day_user
        UNIQUE (user_id, day)
);