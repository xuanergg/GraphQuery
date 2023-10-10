package com.kgvp.web.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author xuan🐽
 */
@Slf4j
@Component
public class AuthHandlerInterceptor implements HandlerInterceptor {
    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object object) throws Exception {
//        log.info("=======进入拦截器========");
//        if (!(object instanceof HandlerMethod)) {
            return true;
//        }
//        String token = httpServletRequest.getHeader("access-token");
//        if (null == token || "".equals(token.trim())) {
//            throw new RuntimeException("token失效");
//        }
//        log.info("==============token:" + token);
//
//        Boolean aBoolean = redisTemplate.hasKey(token);
//
//        if (Boolean.FALSE.equals(aBoolean)) {
//            throw new RuntimeException("token失效");
//        }
//        return aBoolean;
    }

}
