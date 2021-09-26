package cn.fire.limiter.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @Author: wangzc
 * @Date: 2021/9/23 14:55
 */
@Data
@Component
@ConfigurationProperties("spring.redis")
public class RedisProperties {

    private String host;

    private String port;

    private String password;

    private int database;

}
