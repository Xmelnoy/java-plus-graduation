CREATE TABLE IF NOT EXISTS categories (
                                          id   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                          name VARCHAR(50) NOT NULL,
                                          CONSTRAINT uq_category_name UNIQUE (name)
);