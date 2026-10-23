-- Local replica of PokeAPI data plus proprietary fields (localized_name, region, tags, notes).
CREATE TABLE pokemon (
    id              INTEGER PRIMARY KEY,          -- national Pokedex number
    name            VARCHAR(60)  NOT NULL UNIQUE,
    category        VARCHAR(60),
    description     VARCHAR(1000),
    height_m        DOUBLE PRECISION,
    weight_kg       DOUBLE PRECISION,
    types           VARCHAR(200),                 -- comma-separated
    abilities       VARCHAR(500),                 -- comma-separated
    hp              INTEGER,
    attack          INTEGER,
    defense         INTEGER,
    special_attack  INTEGER,
    special_defense INTEGER,
    speed           INTEGER,
    sprite_url      VARCHAR(300),
    image_url       VARCHAR(300),
    localized_name  VARCHAR(60),
    region          VARCHAR(40),
    tags            VARCHAR(400),                 -- comma-separated
    notes           VARCHAR(500),
    synced_at       TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_pokemon_localized_name ON pokemon (localized_name);
