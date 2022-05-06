package com.huan.wxshop.mock;

import com.huan.api.entity.OrderInfo;
import com.huan.api.generate.Order;
import com.huan.api.rpc.OrderRpcService;
import org.apache.dubbo.config.annotation.DubboService;
import org.mockito.Mock;

@DubboService(version = "${wxshop.orderservice.version}")
public class MockRpcOrderService implements OrderRpcService {
    @Mock
    public OrderRpcService rpcService;

    @Override
    public String sayHello(int goodsId, int number) {
        return "I'm mock!";
    }

    @Override
    public Order createOrder(OrderInfo orderInfo, Order order) {
        return rpcService.createOrder(orderInfo, order);
    }
}
