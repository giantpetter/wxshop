package com.huan.wxshop.service;

import com.huan.wxshop.dao.ShopDao;
import com.huan.wxshop.generate.Shop;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ShopService {
    private final ShopDao shopDao;

    @Autowired
    public ShopService(ShopDao shopDao) {
        this.shopDao = shopDao;
    }


    public Shop queryShopById(Long shopId) {
        Shop shop = shopDao.selectShopById(shopId);
        return shop;
    }
}
