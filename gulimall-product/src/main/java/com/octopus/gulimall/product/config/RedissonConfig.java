package com.octopus.gulimall.product.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author octopus
 * @date 2023/3/21 19:32
 */
//@Configuration
public class RedissonConfig {

    @Value("${spring.redis.host}")
    private String redisIP;
    @Value("${spring.redis.port}")
    private Integer redisPort;

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://" + redisIP + ":" + redisPort).setConnectionMinimumIdleSize(1);
        return Redisson.create(config);
    }
}
