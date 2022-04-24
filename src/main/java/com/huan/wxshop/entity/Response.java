package com.huan.wxshop.entity;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Response<T> {
    private T data;
    private String message;

    public static <T> Response<T> of(String message, T data) {
        return new Response<>(message, data);
    }

    public static <T> Response<T> of(T data) {
        return new Response<>(null, data);
    }


    public Response() {
    }

    private Response(String message, T data) {
        this.data = data;
        this.message = message;
    }

}
