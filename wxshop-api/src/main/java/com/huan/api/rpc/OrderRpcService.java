package com.huan.api.rpc;

import com.huan.api.entity.OrderInfo;
import com.huan.api.generate.Order;

public interface OrderRpcService {
    String sayHello(int goodsId, int number);

    Order createOrder(OrderInfo orderInfo, Order order);
}
