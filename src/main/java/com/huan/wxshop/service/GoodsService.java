package com.huan.wxshop.service;

import com.huan.wxshop.dao.GoodsDao;
import com.huan.wxshop.dao.ShopDao;
import com.huan.wxshop.generate.Goods;
import com.huan.wxshop.generate.Shop;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@SuppressFBWarnings("EI_EXPOSE_REP2")
public class GoodsService {
    private final GoodsDao goodsDao;
    private final ShopDao shopDao;

    @Autowired
    public GoodsService(GoodsDao goodsDao, ShopDao shopDao) {
        this.goodsDao = goodsDao;
        this.shopDao = shopDao;
    }

    public Goods createGoods(Goods goods) {
        Shop shop = shopDao.selectShopById(goods.getShopId());

        if (Objects.equals(shop.getOwnerUserId(), UserContext.getCurrentUser().getId())) {
            return goodsDao.insertGoods(goods);
        }
        throw new NotAuthorizedForShopException("Unauthorized");
    }

    public Goods deleteGoodsById(Long goodsId) {
        Shop shop = shopDao.selectShopById(goodsId);
        if (Objects.equals(shop.getOwnerUserId(), UserContext.getCurrentUser().getId())) {
            return goodsDao.deleteGoodsById(goodsId);
        }
        throw new NotAuthorizedForShopException("Unauthorized");
    }

    public static class NotAuthorizedForShopException extends RuntimeException {
        public NotAuthorizedForShopException(String message) {
            super(message);
        }
    }
}
