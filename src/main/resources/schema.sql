
CREATE TABLE IF NOT EXISTS USERS(
                                    id  INT GENERATED ALWAYS AS IDENTITY,
                                    username VARCHAR(32) NOT NULL,
    password VARCHAR(255) NOT NULL,
    PRIMARY KEY(id)
    );

CREATE TABLE IF NOT EXISTS ROLES(
                                    id  INT GENERATED ALWAYS AS IDENTITY,
                                    name VARCHAR(32) NOT NULL,
    PRIMARY KEY(id)
    );

CREATE TABLE IF NOT EXISTS ROLE_USER(
                                        user_id INT NOT NULL,
                                        role_id INT NOT NULL,
                                        PRIMARY KEY(user_id, role_id),
    FOREIGN KEY(user_id) REFERENCES USERS(id)
    );

