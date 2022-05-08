package com.huan.order.mapper;

import com.huan.api.entity.GoodsInfo;
import com.huan.api.entity.OrderInfo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MyOrderMapper {
    void batchInsert(OrderInfo orderInfo);

    List<GoodsInfo> queryOrderGoods(long orderId);
}
