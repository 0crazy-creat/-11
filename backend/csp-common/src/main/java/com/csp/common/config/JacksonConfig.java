package com.csp.common.config;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 全局 Jackson 配置：Long / long 序列化为字符串。
 *
 * <p>背景：雪花 ID（如 2117274592296144898）超出 JavaScript Number 安全整数范围
 * （2^53-1 = 9007199254740991），前端会精度丢失（...898 → ...896），
 * 导致按 ID 查询/跳转拿不到数据。序列化为字符串可彻底规避。</p>
 */
@Configuration
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer longToStringCustomizer() {
        return builder -> {
            SimpleModule module = new SimpleModule();
            module.addSerializer(Long.class, ToStringSerializer.instance);
            module.addSerializer(Long.TYPE, ToStringSerializer.instance);
            builder.modulesToInstall(module);
        };
    }
}
