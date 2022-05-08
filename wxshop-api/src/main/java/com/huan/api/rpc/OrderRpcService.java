package com.huan.api.rpc;

import com.huan.api.entity.DataStatus;
import com.huan.api.entity.OrderInfo;
import com.huan.api.entity.PageResponse;
import com.huan.api.entity.RpcOrderGoods;
import com.huan.api.generate.Order;

public interface OrderRpcService {
    String sayHello(int goodsId, int number);

    Order createOrder(OrderInfo orderInfo, Order order);

    Order selectOrderById(long orderId);

    RpcOrderGoods deleteOrder(long orderId, long userId);

    PageResponse<RpcOrderGoods> obtainPagedOrders(int pageNum, int pageSize, Long userId, DataStatus status);

    RpcOrderGoods updateOrder(long orderId, Order order, Long userId);
}
