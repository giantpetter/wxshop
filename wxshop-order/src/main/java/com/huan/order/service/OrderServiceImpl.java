package com.huan.order.service;

import com.huan.api.rpc.OrderService;
import org.apache.dubbo.config.annotation.DubboService;

@DubboService(version = "${wxshop.orderservice.version}")
public class OrderServiceImpl implements OrderService {
    @Override
    public String sayHello(int goodsId, int number) {
        System.out.println("goodsId: " + goodsId + ", number:" + number);
        return "hello!";
    }
}
