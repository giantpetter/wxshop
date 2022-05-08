package com.huan.order.service;

import com.huan.api.entity.*;
import com.huan.api.exceptions.HttpException;
import com.huan.api.generate.Order;
import com.huan.api.generate.OrderExample;
import com.huan.api.generate.OrderGoods;
import com.huan.api.generate.OrderGoodsExample;
import com.huan.api.rpc.OrderRpcService;
import com.huan.order.mapper.MyOrderMapper;
import com.huan.order.mapper.OrderGoodsMapper;
import com.huan.order.mapper.OrderMapper;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;

@DubboService(version = "${wxshop.orderservice.version}")
public class OrderRpcServiceImpl implements OrderRpcService {
    private final OrderMapper orderMapper;
    private final MyOrderMapper myOrderMapper;
    private final OrderGoodsMapper orderGoodsMapper;


    @Autowired
    public OrderRpcServiceImpl(OrderMapper orderMapper, MyOrderMapper myOrderMapper, OrderGoodsMapper orderGoodsMapper) {
        this.orderMapper = orderMapper;
        this.myOrderMapper = myOrderMapper;
        this.orderGoodsMapper = orderGoodsMapper;
    }

    @Override
    public String sayHello(int goodsId, int number) {
        System.out.println("goodsId: " + goodsId + ", number:" + number);
        return "hello!";
    }

    @Override
    public Order createOrder(OrderInfo orderInfo, Order order) {
        insertOrder(order);
        orderInfo.setOrderId(order.getId());
        myOrderMapper.batchInsert(orderInfo);
        return orderMapper.selectByPrimaryKey(order.getId());
    }

    @Override
    public Order selectOrderById(long orderId) {
        return orderMapper.selectByPrimaryKey(orderId);
    }

    @Override
    public RpcOrderGoods deleteOrder(long orderId, long userId) {
        Order orderToBeDeleted = selectOrderById(orderId);
        if (Objects.isNull(orderToBeDeleted)) {
            throw HttpException.notFound("订单不存在！");
        }
        if (!Objects.equals(orderToBeDeleted.getUserId(), userId)) {
            throw HttpException.forbidden("不能删除非自己的订单！");
        }

        Order copy = new Order();
        copy.setStatus(DataStatus.DELETE_STATUS.getStatus());
        copy.setUpdatedAt(new Date());
        copy.setId(orderId);
        orderMapper.updateByPrimaryKeySelective(copy);

        RpcOrderGoods result = new RpcOrderGoods(orderMapper.selectByPrimaryKey(orderId));
        result.setGoods(myOrderMapper.queryOrderGoods(orderId));
        return result;
    }

    @Override
    public PageResponse<RpcOrderGoods> obtainPagedOrders(int pageNum, int pageSize, Long userId, DataStatus status) {
        OrderExample count = new OrderExample();
        setStatus(count, userId, status);
        long totalNum = orderMapper.countByExample(count);
        int totalPage = (int) (totalNum % pageSize == 0 ? totalNum / pageSize : (totalNum / pageSize + 1));
        OrderExample pageOrder = new OrderExample();
        setStatus(pageOrder, userId, status);
        pageOrder.setLimit(pageSize);
        pageOrder.setOffset((pageNum - 1) * pageSize);

        List<Order> orders = orderMapper.selectByExample(pageOrder);

        List<Long> orderIds = orders.stream().map(Order::getId).collect(Collectors.toList());
        OrderGoodsExample orderGoodsExample = new OrderGoodsExample();
        orderGoodsExample.createCriteria().andOrderIdIn(orderIds);
        List<OrderGoods> orderGoods = orderGoodsMapper.selectByExample(orderGoodsExample);

        Map<Long, List<OrderGoods>> orderIdToGoodsMap = orderGoods.stream()
                .collect(Collectors.groupingBy(OrderGoods::getOrderId));

        List<RpcOrderGoods> data = orders.stream()
                .map(order -> ToRpcOrderGoods(order, orderIdToGoodsMap))
                .collect(Collectors.toList());

        return PageResponse.pagedData(pageNum, pageSize, totalPage, data);
    }

    @Override
    public RpcOrderGoods updateOrder(long orderId, Order order, Long userId) {
        Order orderInDB = orderMapper.selectByPrimaryKey(orderId);
        if (Objects.isNull(orderInDB)) {
            throw HttpException.notFound("订单未找到：" + orderId);
        }
        if (!orderInDB.getUserId().equals(userId)) {
            throw HttpException.forbidden("不是自己的订单");
        }
        Order copy = new Order();
        refreshCopiedOrder(copy, order);
        orderMapper.updateByPrimaryKeySelective(copy);

        RpcOrderGoods rpcOrderGoods = new RpcOrderGoods(orderMapper.selectByPrimaryKey(orderId));
        rpcOrderGoods.setGoods(myOrderMapper.queryOrderGoods(orderId));
        return rpcOrderGoods;
    }

    private void refreshCopiedOrder(Order copy, Order order) {
        if (order.getExpressCompany() != null) {
            copy.setExpressCompany(order.getExpressCompany());
        }
        if (order.getExpressId() != null) {
            copy.setExpressId(order.getExpressId());
        }
        if (order.getStatus() != null) {
            copy.setStatus(order.getStatus());
        }
        copy.setId(order.getId());
        copy.setUpdatedAt(new Date());
    }

    private RpcOrderGoods ToRpcOrderGoods(Order order, Map<Long, List<OrderGoods>> orderIdToGoodsMap) {
        RpcOrderGoods result = new RpcOrderGoods(order);
        List<OrderGoods> orderGoods = orderIdToGoodsMap.get(order.getId());
        result.setGoods(orderGoods.stream()
                .map(this::toGoodsInfo)
                .collect(Collectors.toList()));
        return result;
    }

    private GoodsInfo toGoodsInfo(OrderGoods orderGoods) {
        GoodsInfo goodsInfo = new GoodsInfo();
        goodsInfo.setGoodsId(orderGoods.getGoodsId());
        goodsInfo.setNumber(orderGoods.getNumber().intValue());
        return goodsInfo;
    }

    private void setStatus(OrderExample orderExample, long userId, DataStatus status) {
        if (Objects.nonNull(status)) {
            orderExample.createCriteria().andUserIdEqualTo(userId).andStatusEqualTo(status.getStatus());
        } else {
            orderExample.createCriteria().andUserIdEqualTo(userId).andStatusNotEqualTo(DataStatus.DELETE_STATUS.getStatus());
        }
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
