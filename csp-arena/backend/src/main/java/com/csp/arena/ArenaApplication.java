package com.csp.arena;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/** 独立靶场服务启动类。 */
@SpringBootApplication
@MapperScan("com.csp.arena.mapper")
@EnableScheduling
public class ArenaApplication {
    public static void main(String[] args) {
        SpringApplication.run(ArenaApplication.class, args);
        System.out.println("""
                ============================================================
                  独立靶场服务 启动成功
                  API  : http://localhost:11002/api
                  Health: http://localhost:11002/api/arena/health
                ============================================================""");
    }
}
