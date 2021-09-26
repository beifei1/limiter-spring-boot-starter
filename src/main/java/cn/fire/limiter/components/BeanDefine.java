package cn.fire.limiter.components;

import cn.fire.limiter.properties.RedisProperties;
import io.netty.channel.nio.NioEventLoopGroup;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;

/**
 * @Author: wangzc
 * @Date: 2021/9/23 14:58
 */

@Slf4j
@Configuration
@ConditionalOnBean(RedisProperties.class)
@AutoConfigureAfter(RedisProperties.class)
public class BeanDefine {

    @Autowired
    private RedisProperties redisProperties;

    /**
     * default
     * @return
     */
    @Bean("limiterRedissonClient")
    @ConditionalOnMissingBean(RedissonClient.class)
    public RedissonClient redissonClient() {
        Config config = new Config();
        String url = "redis://" + this.redisProperties.getHost() + ":" + this.redisProperties.getPort();

        (config.useSingleServer().setAddress(url).setPassword(this.redisProperties.getPassword())).setDatabase(this.redisProperties.getDatabase());
        //适用于未明确设置leaseTimeout参数情况
        config.setLockWatchdogTimeout(5000L);
        //cpu核心数 * 2
        config.setNettyThreads(1 * 2);
        //netty多路复用
        config.setEventLoopGroup(new NioEventLoopGroup());
        //用于控制topic,remoteservice，和executorService线程池
        config.setExecutor(Executors.newFixedThreadPool(2));

        try {
            return Redisson.create(config);
        } catch (Exception e) {
            log.error("RedissonClient init redis url:[{}], Exception:", url, e);
            return null;
        }
    }

}
