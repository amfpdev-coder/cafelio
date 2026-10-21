CREATE TABLE annual_goals (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    user_id UUID NOT NULL,

    year INTEGER NOT NULL,

    target INTEGER NOT NULL,

    CONSTRAINT fk_annual_goals_user
        FOREIGN KEY (user_id)
        REFERENCES users(id),

    CONSTRAINT uk_annual_goal_user_year
        UNIQUE (user_id, year)
);
