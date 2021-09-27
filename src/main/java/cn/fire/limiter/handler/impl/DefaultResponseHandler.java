package cn.fire.limiter.handler.impl;

import cn.fire.limiter.handler.IResponseHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: wangzc
 * @Date: 2021/9/24 11:27
 */

public class DefaultResponseHandler implements IResponseHandler {

    @Override
    public HttpStatus status() {
        return HttpStatus.FORBIDDEN;
    }

    @Override
    public byte[] body() {
        return "{\"code\":1,\"msg\":\"访问频次超限\"}".getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public Map<String,String> headers() {
        return new HashMap<>();
    }

    @Override
    public String contentType() {
        return MediaType.APPLICATION_JSON_UTF8_VALUE;
    }

}
