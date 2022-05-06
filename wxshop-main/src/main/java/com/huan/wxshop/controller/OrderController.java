package com.huan.wxshop.controller;

import com.huan.api.entity.OrderInfo;
import com.huan.wxshop.entity.OrderResponse;
import com.huan.wxshop.entity.Response;
import com.huan.wxshop.service.OrderService;
import com.huan.wxshop.service.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class OrderController {
    OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/test")
    public String dubboTest() {
        orderService.sayHello(1, 2);
        return "";
    }

    @PostMapping("/order")
    public Response<OrderResponse> placeOrder(@RequestBody OrderInfo orderInfo) {
        orderService.deductStock(orderInfo);
        return Response.of(orderService.createOrder(orderInfo, UserContext.getCurrentUser().getId()));
    }


}
