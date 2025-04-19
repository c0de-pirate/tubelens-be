# create databases
-- CREATE DATABASE IF NOT EXISTS `saga_order`;
-- CREATE DATABASE IF NOT EXISTS `saga_account`;
-- CREATE DATABASE IF NOT EXISTS `saga_product`;
--
-- GRANT ALL PRIVILEGES ON saga_order.* TO 'saga-tester'@'%';
-- GRANT ALL PRIVILEGES ON saga_account.* TO 'saga-tester'@'%';
-- GRANT ALL PRIVILEGES ON saga_product.* TO 'saga-tester'@'%';
--
-- create table saga_order.o_order (id integer not null auto_increment, canceled_reason varchar(255), count integer not null, customer_id integer, order_status integer, product_id integer, primary key (id)) engine=InnoDB
--
-- create table saga_product.product (id integer not null auto_increment, count integer not null, name varchar(255), price integer not null, primary key (id)) engine=InnoDB;

