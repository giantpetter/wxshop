package com.huan.wxshop.service;

import com.huan.api.entity.*;
import com.huan.api.exceptions.HttpException;
import com.huan.api.generate.Order;
import com.huan.api.rpc.OrderRpcService;
import com.huan.wxshop.dao.GoodsStockMapper;
import com.huan.wxshop.entity.GoodsWithNumber;
import com.huan.wxshop.entity.OrderResponse;
import com.huan.wxshop.generate.Goods;
import com.huan.wxshop.generate.ShopMapper;
import com.huan.wxshop.generate.UserMapper;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@SuppressFBWarnings({"EI_EXPOSE_REP2", "EI_EXPOSE_REP"})
public class OrderService {
    @DubboReference(version = "${wxshop.orderservice.version}",
            url = "${wxshop.orderservice.url}")
    OrderRpcService orderRpcService;

    private final GoodsService goodsService;
    private final UserMapper userMapper;
    private final ShopMapper shopMapper;
    private final GoodsStockMapper goodsStockMapper;

    @Autowired
    public OrderService(GoodsService goodsService, UserMapper userMapper, ShopMapper shopMapper, GoodsStockMapper goodsStockMapper) {
        this.goodsService = goodsService;
        this.userMapper = userMapper;
        this.shopMapper = shopMapper;
        this.goodsStockMapper = goodsStockMapper;
    }

    public String sayHello(int i, int j) {//for dubbo test
        return orderRpcService.sayHello(i, j);
    }

    public OrderResponse createOrder(OrderInfo orderInfo, Long userId) {
        Map<Long, Goods> goodsIdToGoods = getIdToGoodsMap(orderInfo.getGoods());
        Order createdOrder = createOrderViaRpc(orderInfo, userId, goodsIdToGoods);
        return generateOrderResponse(orderInfo.getGoods(), goodsIdToGoods, createdOrder);

    }

    private OrderResponse generateOrderResponse(List<GoodsInfo> goodsInfo, Map<Long, Goods> goodsIdToGoods, Order createdOrder) {
        OrderResponse orderResponse = new OrderResponse(createdOrder);
        Long shopId = new ArrayList<>(goodsIdToGoods.values()).get(0).getShopId();
        orderResponse.setShop(shopMapper.selectByPrimaryKey(shopId));
        orderResponse.setGoods(
                goodsInfo.stream()
                        .map(goods -> toGoodsWithNumber(goods, goodsIdToGoods))
                        .collect(Collectors.toList())
        );
        return orderResponse;
    }

    private Map<Long, Goods> getIdToGoodsMap(List<GoodsInfo> goodsInfo) {
        List<Long> goodsId = goodsInfo.stream()
                .map(GoodsInfo::getGoodsId)
                .collect(Collectors.toList());
        return goodsService.getIdToGoodsMap(goodsId);
    }

    private Order createOrderViaRpc(OrderInfo orderInfo, Long userId, Map<Long, Goods> goodsIdToGoods) {
        Order order = new Order();
        order.setUserId(userId);
        order.setStatus(DataStatus.PENDING.getStatus());
        order.setAddress(userMapper.selectByPrimaryKey(userId).getAddress());
        order.setTotalPrice(calTotalPrice(orderInfo, goodsIdToGoods));
        return orderRpcService.createOrder(orderInfo, order);
    }

    @Transactional
    public void deductStock(OrderInfo orderInfo) {
        for (GoodsInfo goodsInfo : orderInfo.getGoods()) {
            if (goodsStockMapper.deductStock(goodsInfo) <= 0) {
                throw HttpException.gone("扣减库存失败商品 id" + goodsInfo.getGoodsId() + "，数量：" + goodsInfo.getNumber());
            }
        }
    }

    private GoodsWithNumber toGoodsWithNumber(GoodsInfo goodsInfo, Map<Long, Goods> goodsIdToGoods) {
        GoodsWithNumber ret = new GoodsWithNumber(goodsIdToGoods.get(goodsInfo.getGoodsId()));
        ret.setNumber(goodsInfo.getNumber());
        return ret;
    }

    private BigDecimal calTotalPrice(OrderInfo orderInfo, Map<Long, Goods> goodsIdToGoods) {
        BigDecimal result = BigDecimal.ZERO;
        for (GoodsInfo goodsInfo : orderInfo.getGoods()) {
            Goods goods = goodsIdToGoods.get(goodsInfo.getGoodsId());
            if (goods == null) {
                throw HttpException.badRequest("商品 id 非法:" + goodsInfo.getGoodsId());
            }
            if (goodsInfo.getNumber() <= 0) {
                throw HttpException.badRequest("number 非法：" + goodsInfo.getNumber());
            }
            result = result.add(goods.getPrice().multiply(BigDecimal.valueOf(goodsInfo.getNumber())));
        }
        return result;
    }


    public OrderResponse deleteOrder(long orderId, long userId) {
        RpcOrderGoods rpcOrderGoods = orderRpcService.deleteOrder(orderId, userId);
        Map<Long, Goods> goodsIdToGoods = getIdToGoodsMap(rpcOrderGoods.getGoods());
        return generateOrderResponse(rpcOrderGoods.getGoods(), goodsIdToGoods, rpcOrderGoods.getOrder());
    }

    public PageResponse<OrderResponse> obtainAllOrders(int pageNum, int pageSize, Long userId, DataStatus status) {
        PageResponse<RpcOrderGoods> rpcPageResponse = orderRpcService.obtainPagedOrders(pageNum, pageSize, userId, status);
//        List<GoodsInfo> goodsInfos = rpcPageResponse.getData()
//                .stream()
//                .map(RpcOrderGoods::getGoods)
//                .flatMap(List::stream)
//                .collect(Collectors.toList());

//        Map<Long, Goods> goodsIdToGoods = getIdToGoodsMap(goodsInfos);
//        List<OrderResponse> orders = rpcPageResponse.getData()
//                .stream()
//                .map(order -> generateOrderResponse(goodsInfos, goodsIdToGoods, order.getOrder()))
//                .collect(Collectors.toList());
//        return PageResponse.pagedData(pageNum, pageSize, rpcPageResponse.getTotalPage(), orders);

        List<OrderResponse> data = new ArrayList<>();
        for (RpcOrderGoods rpcOrderGoods : rpcPageResponse.getData()) {
            List<GoodsInfo> goodsInfos = rpcOrderGoods.getGoods();
            Map<Long, Goods> idToGoods = getIdToGoodsMap(goodsInfos);
            data.add(generateOrderResponse(goodsInfos, idToGoods, rpcOrderGoods.getOrder()));
        }
        return PageResponse.pagedData(pageNum, pageSize, rpcPageResponse.getTotalPage(), data);


    }

    public OrderResponse updateOrder(long orderId, Order order, Long userId) {
        RpcOrderGoods rpcOrderGoods = orderRpcService.updateOrder(orderId, order, userId);
        Map<Long, Goods> idToGoodsMap = getIdToGoodsMap(rpcOrderGoods.getGoods());
        return generateOrderResponse(rpcOrderGoods.getGoods(), idToGoodsMap, rpcOrderGoods.getOrder());
    }
}
