CREATE SCHEMA IF NOT EXISTS data;

CREATE TABLE IF NOT EXISTS data.instruments
(
    instrument_id SERIAL NOT NULL
        CONSTRAINT instrument_pk
            PRIMARY KEY,
    isin           TEXT NOT NULL UNIQUE,
    description    TEXT,
    created_at     TIMESTAMPTZ NOT NULL
);

CREATE TABLE IF NOT EXISTS data.quotes
(
    isin TEXT NOT NULL
        CONSTRAINT instrument_fk
            REFERENCES data.instruments(isin) ON DELETE CASCADE,
    price         NUMERIC(15,4) NOT NULL,
    created_at    TIMESTAMPTZ NOT NULL
);

CREATE INDEX IF NOT EXISTS quotes_isin_created_at_idx
    ON data.quotes (isin, created_at);

SELECT create_hypertable('data.quotes','created_at');
