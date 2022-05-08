package com.huan.wxshop.mock;

import com.huan.api.entity.DataStatus;
import com.huan.api.entity.OrderInfo;
import com.huan.api.entity.PageResponse;
import com.huan.api.entity.RpcOrderGoods;
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

    @Override
    public Order selectOrderById(long orderId) {
        return rpcService.selectOrderById(orderId);
    }

    @Override
    public RpcOrderGoods deleteOrder(long orderId, long userId) {
        return rpcService.deleteOrder(orderId, userId);
    }

    @Override
    public PageResponse<RpcOrderGoods> obtainPagedOrders(int pageNum, int pageSize, Long userId, DataStatus status) {
        return rpcService.obtainPagedOrders(pageNum, pageSize, userId, status);
    }

    @Override
    public RpcOrderGoods updateOrder(long orderId, Order order, Long userId) {
        return rpcService.updateOrder(orderId, order, userId);
    }
}
