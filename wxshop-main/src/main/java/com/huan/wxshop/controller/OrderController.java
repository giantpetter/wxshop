package com.huan.wxshop.controller;

import com.huan.api.rpc.OrderService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class OrderController {
    @DubboReference(version = "${wxshop.orderservice.version}")
//    @DubboReference(version = "1.0.1")
    OrderService orderService;


    @GetMapping("/test")
    public String dubboTest() {
        orderService.sayHello(1, 2);
        return "";
    }

}
