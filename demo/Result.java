package com.example.demo;

import lombok.Data;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class Result<T> {
    private int code;
    private String message;
    private T data;
    private String requestId;
    private String timestamp;

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(0);
        result.setMessage("success");
        result.setData(data);
        result.setRequestId(UUID.randomUUID().toString());
        result.setTimestamp(OffsetDateTime.now().toString());
        return result;
    }

    public static <T> Result<T> fail(int code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        result.setData(null);
        result.setRequestId(UUID.randomUUID().toString());
        result.setTimestamp(OffsetDateTime.now().toString());
        return result;
    }
}