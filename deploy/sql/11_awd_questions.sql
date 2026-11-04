SET NAMES utf8mb4;
USE csp;
-- AWD 靶场题（关联 csp_arena 模板镜像），供 AWD 赛自动为每支队伍拉起独立容器
INSERT INTO question (id,bank_type,category,title,content,answer,flag,score,difficulty,image_name,cpu_limit,mem_limit,writeup,status,ext,deleted,created_at,updated_at) VALUES
(2300000000000000001,'AWD','WEB','AWD Web 基础靶场','Nginx 静态站点，存在配置弱点。攻破后读取 /flag.txt。',NULL,'flag{awd_basic_2026}',300,'EASY','csp/arena-basic-web:1.0','1.0','512m',NULL,1,'{"templateCode":"awd-web-basic"}',0,NOW(),NOW()),
(2300000000000000002,'AWD','WEB','AWD SQL 注入靶场','业务站存在注入点，修补后 check 脚本验证防御效果。',NULL,'flag{awd_sqli_2026}',300,'MEDIUM','csp/arena-basic-web:1.0','1.0','512m',NULL,1,'{"templateCode":"awd-web-sqli"}',0,NOW(),NOW()),
(2300000000000000003,'AWD','WEB','AWD 文件上传靶场','上传校验存在缺陷，需定位并修补。',NULL,'flag{awd_upload_2026}',300,'MEDIUM','csp/arena-basic-web:1.0','1.0','512m',NULL,1,'{"templateCode":"awd-web-upload"}',0,NOW(),NOW()),
(2300000000000000004,'AWD','MISC','AWD 流量分析靶场','给定流量数据，识别外带行为。',NULL,'flag{awd_misc_2026}',300,'EASY','csp/arena-basic-web:1.0','1.0','512m',NULL,1,'{"templateCode":"awd-misc-forensic"}',0,NOW(),NOW())
ON DUPLICATE KEY UPDATE title=VALUES(title),image_name=VALUES(image_name),status=1;
SELECT bank_type,COUNT(*) FROM question WHERE deleted=0 GROUP BY bank_type;
