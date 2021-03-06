package cn.fire.limiter.components;

import cn.fire.limiter.annotation.WebLimiter;
import cn.fire.limiter.enums.AccessEnum;
import cn.fire.limiter.enums.UniqueEnum;
import cn.fire.limiter.handler.IResponseHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.aop.aspectj.MethodInvocationProceedingJoinPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @Author: wangzc
 * @Date: 2021/9/23 13:43
 */
@Aspect
@Slf4j
@Component
@ConditionalOnBean(RedissonClient.class)
public class LockAspect {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    private RedissonClient redissonClient;

    @Pointcut("@annotation(cn.fire.limiter.annotation.WebLimiter)")
    private void methodAnnotation() {}

    @Pointcut("@within(cn.fire.limiter.annotation.WebLimiter)")
    private void typeAnnotation() {}

    @Around("methodAnnotation() || typeAnnotation()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {

        //????????????
        StopWatch sw = new StopWatch();
        sw.start();

        MethodInvocationProceedingJoinPoint mjp = (MethodInvocationProceedingJoinPoint) joinPoint;
        MethodSignature signature = (MethodSignature) mjp.getSignature();
        Method method = signature.getMethod();

        //??????????????????????????????
        WebLimiter webLimiter = (method.isAnnotationPresent(WebLimiter.class) == true ? method.getAnnotation(WebLimiter.class) :
                AnnotationUtils.findAnnotation(method.getDeclaringClass(), WebLimiter.class));

        if (Objects.isNull(webLimiter)) {
            sw.stop();
            return joinPoint.proceed();
        }

        IResponseHandler handler = webLimiter.handler().newInstance();

        if (Objects.isNull(handler)) {
            log.error("?????????????????????????????????????????????: {}", handler);
            return null;
        }

        Integer expire = webLimiter.expire();
        TimeUnit timeUnit = webLimiter.timeunit();
        AccessEnum accessStrategy = webLimiter.access();
        UniqueEnum uniqueStrategy = webLimiter.unique();

        //????????????????????????????????????servlet???web???????????????????????????threadlocal????????????????????????????????????response??????
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

        if (StringUtils.equalsIgnoreCase(request.getMethod(), "options")) {
            sw.stop();
            return joinPoint.proceed();
        }

        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();


        StringBuilder key = new StringBuilder(StringUtils.EMPTY);
        if (uniqueStrategy == UniqueEnum.METHOD_PARAM) {
            Object[] params = joinPoint.getArgs();

            if (Objects.isNull(params) || params.length <= 0) {
                processResponse(response, handler);
                return null;
            }
            for (Object param : params) {
                key.append(OBJECT_MAPPER.writeValueAsString(param));
            }
        }
        if (uniqueStrategy == UniqueEnum.HEADER) {
            key.append(getHeaderValue(request));
        }

        if (StringUtils.isBlank(key.toString())) {
            processResponse(response, handler);
            return null;
        }

        //??????header ??? method param?????????uri ??????????????????key
        String digest = DigestUtils.md5DigestAsHex((key + request.getRequestURI()).getBytes(StandardCharsets.UTF_8));

        RLock lock = redissonClient.getLock("access:limiter:" + digest);

        if (accessStrategy == AccessEnum.DENY) {
            if (lock.tryLock(0, expire, timeUnit)) {
                sw.stop();
                log.info("?????????????????????: {} ms", sw.getTime());
                return joinPoint.proceed();
            } else {
                processResponse(response, handler);
                sw.stop();
                log.info("?????????????????????: {} ms", sw.getTime());
                return null;
            }
        }
        if (accessStrategy == AccessEnum.QUEUE) {
            /**
             * 1???spring mvc?????????????????????????????????????????????????????????????????????????????????redisson??????threadId????????????key??????????????????????????????????????????????????????????????????
             * 2?????????expire??????????????????????????????????????????????????????
             */
            lock.lock(expire, timeUnit);
            sw.stop();
            log.info("?????????????????????: {} ms", sw.getTime());
            return joinPoint.proceed();
        }
        return null;
    }


    /**
     * ??????Response??????
     * @param response
     * @param handler
     * @throws Exception
     */
    private void processResponse(HttpServletResponse response, IResponseHandler handler) throws Exception {
        if (Objects.nonNull(handler.status())) {
            response.setStatus(handler.status().value());
        }
        if (Objects.nonNull(handler.headers())) {
            Map<String, String> headers = handler.headers();
            headers.forEach((k, v) -> {
                response.addHeader(k, v);
            });
        }
        if (Objects.nonNull(handler.body()) && handler.body().length > 0) {
            IOUtils.write(handler.body(), response.getOutputStream());
        }

        if (StringUtils.isNotBlank(handler.contentType())) {
            response.setContentType(handler.contentType());
        }
    }

    /**
     * ??????header???
     *
     * @param request
     * @return
     */
    private String getHeaderValue(HttpServletRequest request) {
        Enumeration<String> headerNames = request.getHeaderNames();
        StringBuilder stringBuilder = new StringBuilder();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            //?????????????????????????????????
            String value = request.getHeader(name);
            stringBuilder.append(value);
        }
        return stringBuilder.toString();
    }

}
