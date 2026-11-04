SET NAMES utf8mb4;
USE csp;
ALTER TABLE notify_message
  ADD COLUMN deliver_status varchar(64) DEFAULT NULL COMMENT '投递状态：delivered / pending:原因';
