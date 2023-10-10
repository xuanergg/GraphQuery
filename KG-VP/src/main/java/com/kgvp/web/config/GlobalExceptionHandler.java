package com.kgvp.web.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kgvp.web.base.util.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

/**
 * 异常
 *
 * @author xuan🐽
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(NoHandlerFoundException.class)
    public R handleError(NoHandlerFoundException e) {
        log.error("404没找到请求:{}", e.getMessage());
        return R.error("没找到请求:" + e);
    }

    @ExceptionHandler({MissingServletRequestParameterException.class})
    public R handleError(MissingServletRequestParameterException e) {
        log.warn("缺少请求参数:{}", e.getMessage());
        return R.error("缺少请求参数:" + e);
    }

    @ExceptionHandler(value = RuntimeException.class)
    public R RuntimeException(Exception e) {
        log.error("内部发生错误:", e);
        return R.error("内部发生错误:" + e.getMessage());
    }

    @ExceptionHandler(value = IllegalArgumentException.class)
    public R IllegalArgumentException(Exception e) {
        log.error("运行异常:", e);
        return R.error("运行异常:" + e);
    }


    @ExceptionHandler(value = JsonProcessingException.class)
    public R JsonProcessingException(JsonProcessingException e) {
        log.error("数据校验异常:", e);
        return R.error("数据校验异常：" + e);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R MethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.warn("参数验证失败:{}", e.getMessage());
        return handleError(e.getBindingResult());
    }

    @ExceptionHandler(BindException.class)
    public R BindException(BindException e) {
        log.warn("参数绑定失败:{}", e.getMessage());
        return handleError(e.getBindingResult());
    }

    private R handleError(BindingResult result) {
        FieldError error = result.getFieldError();
        String message = String.format("%s:%s", error.getField(), error.getDefaultMessage());
        return R.error(message);
    }
}
