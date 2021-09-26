# limiter-spring-boot-starter
使用springboot和redisson实现的web api放重放锁

### 处理模式
拒绝、排队
### 判断唯一请求标识
header、param
### 可自定义响应数据

### 使用方式

1. 引入stater
```java
<dependency>
    <groupId>cn.fire.limiter</groupId>
    <artifactId>limiter-spring-boot-starter</artifactId>
</dependency>
```
2. 在需要控制的控制器上加入注解
```java
@WebLimiter(expire = 2, timeunit = TimeUnit.SECONDS, handler = DefaultResponseHandler.class, unique = UniqueEnum.HEADER, access = AccessEnum.DENY)
```

