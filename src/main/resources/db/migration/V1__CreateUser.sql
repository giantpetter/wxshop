create table USER(
    ID bigint primary key auto_increment,
    NAME varchar(100),
    TEL varchar(20) unique,
    AVATAR_URL varchar(1024),
    CREATED_AT timestamp ,
    UPDATED_AT timestamp
)ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;