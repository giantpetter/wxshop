package com.huan.wxshop.dao;

import com.huan.wxshop.generate.Shop;
import com.huan.wxshop.generate.ShopMapper;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
@SuppressFBWarnings("EI_EXPOSE_REP2")
public class ShopDao {

    private final ShopMapper shopMapper;

    @Autowired
    public ShopDao(ShopMapper shopMapper) {
        this.shopMapper = shopMapper;
    }


    public Shop selectShopById(Long shopId) {
        return shopMapper.selectByPrimaryKey(shopId);
    }
}
