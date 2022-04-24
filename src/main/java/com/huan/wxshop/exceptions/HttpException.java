package com.huan.wxshop.exceptions;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
public class HttpException extends RuntimeException {
    private int statusCode;

    public static HttpException forbidden(String message) {
        return new HttpException(message, HttpStatus.FORBIDDEN.value());
    }

    public static HttpException badRequest(String message) {
        return new HttpException(message, HttpStatus.BAD_REQUEST.value());
    }

    public static HttpException notAuthorized(String message) {
        return new HttpException(message, HttpStatus.UNAUTHORIZED.value());
    }

    public static HttpException notShopOwner(String message) {
        return new HttpException(message, HttpStatus.FORBIDDEN.value());
    }

    public static HttpException notFound(String message) {
        return new HttpException(message, HttpStatus.NOT_FOUND.value());
    }

    private HttpException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }
}
