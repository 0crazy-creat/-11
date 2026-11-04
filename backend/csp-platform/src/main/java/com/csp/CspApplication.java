package com.csp;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 网络安全竞赛平台启动入口。
 * <p>统一底座：CTF / AWD / AWDP / 理论赛 / 场景实操 / CFS 共享用户、权限、题库体系。</p>
 */
@Slf4j
@EnableAsync
@EnableScheduling
@SpringBootApplication(scanBasePackages = "com.csp")
@MapperScan("com.csp.**.mapper")
public class CspApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext ctx = SpringApplication.run(CspApplication.class, args);
        Environment env = ctx.getEnvironment();
        String port = env.getProperty("server.port", "8080");
        String profiles = String.join(",", env.getActiveProfiles());
        log.info("""

                ============================================================
                  网络安全竞赛平台 启动成功
                  API  : http://localhost:{}/api
                  Docs : http://localhost:{}/swagger-ui/index.html
                  Profile: {}
                ============================================================
                """, port, port, profiles.isBlank() ? "default" : profiles);
    }
}
