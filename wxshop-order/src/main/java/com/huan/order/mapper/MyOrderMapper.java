package com.huan.order.mapper;

import com.huan.api.entity.GoodsInfo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MyOrderMapper {
    void batchInsert(List<GoodsInfo> goodsInfos);
}
