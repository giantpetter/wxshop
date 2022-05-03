create table USER(
    ID bigint primary key auto_increment,
    NAME varchar(100),
    TEL varchar(20) unique,
    ADDRESS varchar(1024),
    AVATAR_URL varchar(1024),
    CREATED_AT timestamp NOT NULL DEFAULT NOW(),
    UPDATED_AT timestamp NOT NULL DEFAULT NOW()
)ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

INSERT INTO USER(NAME, TEL, AVATAR_URL, ADDRESS)
VALUES ('user1', '13800000000', 'http://url', '火星')