package com.huan.wxshop.dao;

import com.huan.wxshop.entity.GoodsWithNumber;
import com.huan.wxshop.generate.Shop;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ShoppingCartQueryMapper {

    int countHowManyShopsInUserShoppingCart(@Param("userId") long userId);


    List<Long> selectShopIdsByPageAndUser(
            @Param("userId") long userId,
            @Param("limit") int limit,
            @Param("offset") int offset);

    Shop selectShopById(@Param("shopId") long shopId);

    List<GoodsWithNumber> selectGoodsByShopIdFromCart(@Param("shopId") long shopId,
                                                      @Param("userId") long userId);
}
