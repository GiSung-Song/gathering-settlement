CREATE TABLE gathering (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    name       VARCHAR(100) NOT NULL,
    created_at DATETIME(6)  NOT NULL,
    PRIMARY KEY (id)
);

-- MEMBER는 MySQL 8 예약어라 gathering_member로 둔다
CREATE TABLE gathering_member (
    id           BIGINT      NOT NULL AUTO_INCREMENT,
    gathering_id BIGINT      NOT NULL,
    nickname     VARCHAR(50) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_member_gathering FOREIGN KEY (gathering_id) REFERENCES gathering (id)
);

CREATE TABLE expense (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    gathering_id BIGINT       NOT NULL,
    payer_id     BIGINT       NOT NULL,
    amount       BIGINT       NOT NULL,
    description  VARCHAR(200) NOT NULL,
    created_at   DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_expense_gathering FOREIGN KEY (gathering_id) REFERENCES gathering (id),
    CONSTRAINT fk_expense_payer FOREIGN KEY (payer_id) REFERENCES gathering_member (id)
);

CREATE TABLE expense_participant (
    id         BIGINT NOT NULL AUTO_INCREMENT,
    expense_id BIGINT NOT NULL,
    member_id  BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_expense_participant UNIQUE (expense_id, member_id),
    CONSTRAINT fk_participant_expense FOREIGN KEY (expense_id) REFERENCES expense (id),
    CONSTRAINT fk_participant_member FOREIGN KEY (member_id) REFERENCES gathering_member (id)
);
