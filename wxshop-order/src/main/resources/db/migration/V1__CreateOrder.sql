create table `ORDER_TABLE`
(
    ID              BIGINT PRIMARY KEY AUTO_INCREMENT,
    USER_ID         BIGINT,
    TOTAL_PRICE     DECIMAL,
    ADDRESS         VARCHAR(1024),
    EXPRESS_COMPANY VARCHAR(16),
    EXPRESS_ID      VARCHAR(128),
    STATUS          VARCHAR(16),
    CREATED_AT      TIMESTAMP NOT NULL DEFAULT NOW(),
    UPDATED_AT      TIMESTAMP NOT NULL DEFAULT NOW()
);

create TABLE `ORDER_GOODS`
(
    ID          BIGINT PRIMARY KEY AUTO_INCREMENT,
    Order_ID    BIGINT,
    GOODS_ID    BIGINT,
    NUMBER      BIGINT
);
insert into `ORDER_TABLE`(USER_ID,TOTAL_PRICE,ADDRESS,EXPRESS_COMPANY,EXPRESS_ID,STATUS)
values (1, 1300, 'address1', 'company1', '1', 'ok');

insert into `ORDER_GOODS`(ORDER_ID, GOODS_ID, `NUMBER`)
values (1, 2, 3),
        (1, 3, 4);

insert into `ORDER_TABLE`(USER_ID,TOTAL_PRICE,ADDRESS,EXPRESS_COMPANY,EXPRESS_ID,STATUS)
values (1, 500, 'address2', 'company2', '2', 'pending');

insert into `ORDER_GOODS`(ORDER_ID, GOODS_ID, `NUMBER`)
values (2, 2, 5),
        (2, 3, 7);