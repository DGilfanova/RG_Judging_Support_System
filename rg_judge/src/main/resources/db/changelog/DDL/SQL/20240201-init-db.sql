CREATE TABLE IF NOT EXISTS element
(
    id                  INT PRIMARY KEY,
    official_number     VARCHAR(7) NOT NULL,
    name                VARCHAR    NOT NULL,
    value               REAL       NOT NULL,
    type_by_support_leg VARCHAR(5) NOT NULL,
    created_at          TIMESTAMP DEFAULT now(),
    updated_at          TIMESTAMP DEFAULT now()
);


CREATE TABLE IF NOT EXISTS leg_split_criteria
(
    element_id INT UNIQUE,
    min_degree REAL CHECK ( min_degree >= 0 AND min_degree <= 360 ),
    max_degree REAL CHECK ( min_degree >= 0 AND min_degree <= 360 ),
    added_at   TIMESTAMP DEFAULT now(),
    CHECK ( min_degree <= max_degree ),
    FOREIGN KEY (element_id) REFERENCES element (id)
);


CREATE TABLE IF NOT EXISTS body_posture_criteria
(
    element_id INT UNIQUE,
    min_degree REAL CHECK ( min_degree >= 0 AND min_degree <= 360 ),
    max_degree REAL CHECK ( min_degree >= 0 AND min_degree <= 360 ),
    added_at   TIMESTAMP DEFAULT now(),
    CHECK ( min_degree <= max_degree ),
    FOREIGN KEY (element_id) REFERENCES element (id)
);


CREATE TABLE IF NOT EXISTS leg_position_criteria
(
    element_id      INT,
    side            VARCHAR(10),
    position_type   VARCHAR(30),
    estimation_type VARCHAR(20),
    added_at        TIMESTAMP DEFAULT now(),
    PRIMARY KEY (element_id, side),
    FOREIGN KEY (element_id) REFERENCES element (id)
);


CREATE TABLE IF NOT EXISTS releve_criteria
(
    element_id      INT PRIMARY KEY,
    is_releve       BOOLEAN,
    added_at        TIMESTAMP DEFAULT now(),
    FOREIGN KEY (element_id) REFERENCES element (id)
);


CREATE TABLE IF NOT EXISTS hand_to_leg_touch_criteria
(
    element_id      INT PRIMARY KEY,
    type            VARCHAR(30),
    is_touch        BOOLEAN,
    added_at        TIMESTAMP DEFAULT now(),
    FOREIGN KEY (element_id) REFERENCES element (id)
);


CREATE TABLE IF NOT EXISTS head_to_leg_touch_criteria
(
    element_id      INT PRIMARY KEY,
    type            VARCHAR(30),
    is_touch        BOOLEAN,
    added_at        TIMESTAMP DEFAULT now(),
    FOREIGN KEY (element_id) REFERENCES element (id)
);


CREATE TABLE IF NOT EXISTS leg_to_leg_touch_criteria
(
    element_id      INT PRIMARY KEY,
    is_touch        BOOLEAN,
    added_at        TIMESTAMP DEFAULT now(),
    FOREIGN KEY (element_id) REFERENCES element (id)
);
