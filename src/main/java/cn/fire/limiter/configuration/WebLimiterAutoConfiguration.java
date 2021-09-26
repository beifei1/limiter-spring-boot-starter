package cn.fire.limiter.configuration;

import cn.fire.limiter.components.BeanDefine;
import cn.fire.limiter.components.LockAspect;
import cn.fire.limiter.properties.RedisProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @Author: wangzc
 * @Date: 2021/9/23 11:38
 */
@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@Import({RedisProperties.class, BeanDefine.class, LockAspect.class})
public class WebLimiterAutoConfiguration {
}
