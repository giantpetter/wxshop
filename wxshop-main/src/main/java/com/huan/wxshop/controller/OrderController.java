package com.huan.wxshop.controller;

import com.huan.api.entity.DataStatus;
import com.huan.api.entity.OrderInfo;
import com.huan.api.entity.PageResponse;
import com.huan.api.exceptions.HttpException;
import com.huan.api.generate.Order;
import com.huan.wxshop.entity.OrderResponse;
import com.huan.wxshop.entity.Response;
import com.huan.wxshop.service.OrderService;
import com.huan.wxshop.service.UserContext;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@SuppressFBWarnings("EI_EXPOSE_REP2")
public class OrderController {
    OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/test")
    public String dubboTest() {
        return orderService.sayHello(1, 2);

    }

    @PostMapping("/order")
    public Response<OrderResponse> placeOrder(@RequestBody OrderInfo orderInfo) {
        orderService.deductStock(orderInfo);
        return Response.of(orderService.createOrder(orderInfo, UserContext.getCurrentUser().getId()));
    }

    @DeleteMapping("/order/{orderId}")
    public Response<OrderResponse> removeOrder(@PathVariable("orderId") long orderId) {
        return Response.of(orderService.deleteOrder(orderId, UserContext.getCurrentUser().getId()));
    }

    @GetMapping("/order")
    public PageResponse<OrderResponse> obtainAllOrders(@RequestParam("pageNum") int pageNum,
                                                       @RequestParam("pageSize") int pageSize,
                                                       @RequestParam(name = "status", required = false) String status) {
        if (status != null && DataStatus.fromStatus(status) == null) {
            throw HttpException.badRequest("status 非法：" + status);
        }
        return orderService.obtainAllOrders(pageNum, pageSize, UserContext.getCurrentUser().getId(), DataStatus.fromStatus(status));
    }

    @PatchMapping("/order/{orderId}")
    public Response<OrderResponse> updateOrder(@PathVariable("orderId") long orderId,
                                               @RequestBody Order order) {
        return Response.of(orderService.updateOrder(orderId, order, UserContext.getCurrentUser().getId()));
    }


}
