package cn.fire.limiter.handler;


import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * @Author: wangzc
 * @Date: 2021/9/24 11:25
 */

public interface IResponseHandler {

    /**
     * 返回的状态码
     * @return
     */
    HttpStatus status();

    /**
     * 响应的body
     * @return
     */
    byte[] body();

    /**
     * 需要响应的header
     * @return
     */
    Map<String,String> headers();

    /**
     * 响应的数据类型
     * @return
     */
    String contentType();

}
