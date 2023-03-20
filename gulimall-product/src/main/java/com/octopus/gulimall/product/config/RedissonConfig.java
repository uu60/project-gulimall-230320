package com.octopus.gulimall.product.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redisson() {
        // 1. 创建配置
        Config config = new Config();
        config.useSingleServer().setAddress("redis://172.16.212.10:6379").setConnectionMinimumIdleSize(1);

        // 2. 根据Config创建出RedissonClient实例
        return Redisson.create(config);
    }
}
