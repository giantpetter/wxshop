package com.huan.wxshop.service;

import com.huan.wxshop.dao.GoodsDao;
import com.huan.wxshop.dao.ShopDao;
import com.huan.wxshop.entity.DataStatus;
import com.huan.wxshop.entity.PageResponse;
import com.huan.wxshop.exceptions.NotAuthorizedForShopException;
import com.huan.wxshop.exceptions.ResourceNotFoundException;
import com.huan.wxshop.generate.Goods;
import com.huan.wxshop.generate.Shop;
import com.huan.wxshop.generate.User;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * 单元测试：
 */
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
            Goods goods = goodsDao.selectGoodsById(goodsId);
            if (goods == null) {
                throw new ResourceNotFoundException("Not Found");
            }
            goods.setStatus(DataStatus.DELETE_STATUS.getStatus());
            goodsDao.deleteGoodsById(goods);
            return goods;
        }
        throw new NotAuthorizedForShopException("Unauthorized");
    }

    public PageResponse<Goods> getGoods(Integer pageNum, Integer pageSize, Integer shopId) {
        int totalNumber = goodsDao.countGoodsByShopId(shopId);
        int totalPage = totalNumber % pageSize == 0 ? totalNumber / pageSize : totalNumber / pageSize + 1;
        List<Goods> listGoods = goodsDao.selectGoodsByPage(pageNum, pageSize);
        return PageResponse.pagedData(pageNum, pageSize, totalPage, listGoods);
    }

    public Goods updateGoods(Long shopId, Goods goods) {
        User currentUser = UserContext.getCurrentUser();
        Shop shop = shopDao.selectShopById(shopId);
        if (Objects.equals(currentUser.getId(), shop.getOwnerUserId())) {
            goods.setUpdatedAt(new Date());
            int affectedRow = goodsDao.updateGoods(goods);
            if (affectedRow == 0) {
                throw new ResourceNotFoundException("Not Found");
            }
            return goods;
        }
        throw new NotAuthorizedForShopException("Unauthorized");
    }


}
