package com.huan.order.mapper;

import com.huan.api.generate.OrderGoods;
import com.huan.api.generate.OrderGoodsExample;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface OrderGoodsMapper {
    long countByExample(OrderGoodsExample example);

    int deleteByExample(OrderGoodsExample example);

    int deleteByPrimaryKey(Long id);

    int insert(OrderGoods record);

    int insertSelective(OrderGoods record);

    List<OrderGoods> selectByExample(OrderGoodsExample example);

    OrderGoods selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") OrderGoods record, @Param("example") OrderGoodsExample example);

    int updateByExample(@Param("record") OrderGoods record, @Param("example") OrderGoodsExample example);

    int updateByPrimaryKeySelective(OrderGoods record);

    int updateByPrimaryKey(OrderGoods record);
}