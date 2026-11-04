-- 独立靶场服务种子数据
USE csp_arena;

INSERT INTO arena_template
 (id, code, name, category, image, container_port, difficulty, description, default_flag,
  cpu_limit, memory_limit, ttl_minutes, status)
VALUES
 (9100000000000000001,'basic-web','基础 Web 靶场','WEB','csp/arena-basic-web:1.0',80,'EASY',
  '入门级 Web 靶场：包含一个展示页面与隐藏 flag 接口，适合验证靶场编排链路。','flag{arena_basic_web_2026}',
  '1.0','256m',120,1),
 (9100000000000000002,'basic-web-2','基础 Web 靶场（副本）','WEB','csp/arena-basic-web:1.0',80,'EASY',
  '同一镜像的第二模板，用于并发拉起与端口池验证。','flag{arena_basic_web_2}',
  '1.0','256m',120,1),
 (9100000000000000003,'nginx-demo','Nginx 静态靶场','WEB','nginx:alpine',80,'EASY',
  '使用 nginx:alpine 提供静态页，验证通用镜像可直接作为靶机拉起。',NULL,
  '0.5','128m',60,1),
 (9100000000000000004,'alpine-shell','Alpine 容器靶场','MISC','alpine:latest',80,'EASY',
  '最小化容器，验证非 Web 镜像的编排与端口映射（模拟服务）。',NULL,
  '0.5','64m',60,1)
ON DUPLICATE KEY UPDATE name=VALUES(name), image=VALUES(image), updated_at=CURRENT_TIMESTAMP;
