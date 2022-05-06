package com.huan.order.service;

import com.huan.api.entity.DataStatus;
import com.huan.api.entity.OrderInfo;
import com.huan.api.generate.Order;
import com.huan.api.rpc.OrderRpcService;
import com.huan.order.mapper.MyOrderMapper;
import com.huan.order.mapper.OrderMapper;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.function.BooleanSupplier;

@DubboService(version = "${wxshop.orderservice.version}")
public class OrderRpcServiceImpl implements OrderRpcService {
    private final OrderMapper orderMapper;
    private final MyOrderMapper myOrderMapper;

    @Autowired
    public OrderRpcServiceImpl(OrderMapper orderMapper, MyOrderMapper myOrderMapper) {
        this.orderMapper = orderMapper;
        this.myOrderMapper = myOrderMapper;
    }

    @Override
    public String sayHello(int goodsId, int number) {
        System.out.println("goodsId: " + goodsId + ", number:" + number);
        return "hello!";
    }

    @Override
    public Order createOrder(OrderInfo orderInfo, Order order) {
        insertOrder(order);
        myOrderMapper.batchInsert(orderInfo.getGoods());
        return orderMapper.selectByPrimaryKey(order.getId());
    }

    private void insertOrder(Order order) {
        order.setStatus(DataStatus.PENDING.getStatus());
        order.setExpressId(null);
        order.setExpressCompany(null);
        verify(() -> order.getUserId() == null, "userId 不能为空");
        verify(() -> order.getTotalPrice() == null || order.getTotalPrice().doubleValue() < 0, "total 非法");
        verify(() -> order.getAddress() == null, "Address 不能为空");
        orderMapper.insertSelective(order);
    }

    private void verify(BooleanSupplier supplier, String msg) {
        if (supplier.getAsBoolean()) {
            throw new IllegalArgumentException(msg);
        }
    }


}
