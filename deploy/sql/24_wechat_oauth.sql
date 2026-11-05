-- 微信扫码登录：持久化 openid / unionid，避免仅依赖 Redis 绑定导致过期后重复建号
ALTER TABLE sys_user
  ADD COLUMN wechat_openid  VARCHAR(64) DEFAULT NULL COMMENT '微信 openid',
  ADD COLUMN wechat_unionid VARCHAR(64) DEFAULT NULL COMMENT '微信 unionid',
  ADD UNIQUE KEY uk_user_wechat_openid (wechat_openid);
