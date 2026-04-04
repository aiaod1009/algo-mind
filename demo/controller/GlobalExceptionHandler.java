package com.example.demo.controller;

import com.example.demo.Result;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.NoSuchElementException;

/**
 * 全局异常处理器，统一返回规范错误响应
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 资源不存在异常（如findById空）
     */
    @ExceptionHandler(NoSuchElementException.class)
    public Result<Void> handleNoSuchElementException(NoSuchElementException e) {
        return Result.fail(40401, "资源不存在：" + e.getMessage());
    }

    /**
     * 参数类型不匹配异常
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Result<Void> handleTypeMismatchException(MethodArgumentTypeMismatchException e) {
        return Result.fail(40001, "参数格式错误：" + e.getName() + "类型不匹配");
    }

    /**
     * 参数校验异常（@Validated）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidException(MethodArgumentNotValidException e) {
        return Result.fail(40001, "参数校验失败：" + e.getBindingResult().getFieldError().getDefaultMessage());
    }

    /**
     * 通用运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleRuntimeException(RuntimeException e) {
        return Result.fail(50001, "服务器内部错误：" + e.getMessage());
    }
}