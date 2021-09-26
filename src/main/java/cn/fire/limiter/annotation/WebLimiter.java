package cn.fire.limiter.annotation;

import cn.fire.limiter.enums.AccessEnum;
import cn.fire.limiter.enums.UniqueEnum;
import cn.fire.limiter.handler.IResponseHandler;
import cn.fire.limiter.handler.impl.DefaultResponseHandler;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * @Author: wangzc
 * @Date: 2021/9/23 11:39
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface WebLimiter {

    /**
     * 后到达的请求默认处理策略
     * @return
     */
    AccessEnum access() default AccessEnum.DENY;

    /**
     * 用于判断相同请求的标识
     * @return
     */
    UniqueEnum unique() default UniqueEnum.METHOD_PARAM;

    /**
     * response处理器
     * @return
     */
    Class<? extends IResponseHandler> handler() default DefaultResponseHandler.class;

    /**
     * 限制时长
     * @return
     */
    int expire() default 1;

    /**
     * 单位
     * @return
     */
    TimeUnit timeunit() default TimeUnit.SECONDS;

}
