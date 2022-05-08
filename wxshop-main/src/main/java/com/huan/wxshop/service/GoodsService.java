package com.huan.wxshop.service;

import com.huan.wxshop.dao.GoodsDao;
import com.huan.wxshop.dao.ShopDao;
import com.huan.api.entity.DataStatus;
import com.huan.api.entity.PageResponse;
import com.huan.api.exceptions.HttpException;
import com.huan.wxshop.generate.*;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 单元测试：
 */
@Service
@SuppressFBWarnings("EI_EXPOSE_REP2")
public class GoodsService {
    private final GoodsDao goodsDao;
    private final ShopDao shopDao;
    private final GoodsMapper goodsMapper;

    @Autowired
    public GoodsService(GoodsDao goodsDao,
                        ShopDao shopDao,
                        GoodsMapper goodsMapper) {
        this.goodsDao = goodsDao;
        this.shopDao = shopDao;
        this.goodsMapper = goodsMapper;
    }

    public Goods createGoods(Goods goods) {
        Shop shop = shopDao.selectShopById(goods.getShopId());

        if (Objects.equals(shop.getOwnerUserId(), UserContext.getCurrentUser().getId())) {
            return goodsDao.insertGoods(goods);
        }
        throw HttpException.forbidden("无法管理不属于自己的店铺！");
    }

    public Goods deleteGoodsById(Long goodsId) {
        Goods goods = goodsDao.selectGoodsById(goodsId);
        if (goods == null) {
            throw HttpException.notFound("商品未找到！");
        }
        Shop shop = shopDao.selectShopById(goods.getShopId());
        if (Objects.equals(shop.getOwnerUserId(), UserContext.getCurrentUser().getId())) {
            goods.setStatus(DataStatus.DELETE_STATUS.getStatus());
            goodsDao.deleteGoodsById(goods);
            return goods;
        }
        throw HttpException.forbidden("无法管理不属于自己的店铺！");
    }

    public PageResponse<Goods> getAllGoods(Integer pageNum, Integer pageSize, Long shopId) {
        int totalNumber = goodsDao.countGoodsByShopId(shopId);
        int totalPage = totalNumber % pageSize == 0 ? totalNumber / pageSize : totalNumber / pageSize + 1;
        List<Goods> listGoods = goodsDao.selectGoodsByPage(pageNum, pageSize, shopId);
        return PageResponse.pagedData(pageNum, pageSize, totalPage, listGoods);
    }

    public Goods updateGoods(long goodsId, Goods goods) {
        User currentUser = UserContext.getCurrentUser();
        Goods oldGoods = goodsDao.selectGoodsById(goodsId);
        if (oldGoods == null) {
            throw HttpException.notFound("商品未找到！");
        }
        Shop shop = shopDao.selectShopById(goods.getShopId());
        if (Objects.equals(currentUser.getId(), shop.getOwnerUserId())) {
            goods.setUpdatedAt(new Date());
            goodsDao.updateGoods(goods);
            return goods;
        }
        throw HttpException.forbidden("无法管理不属于自己的店铺！");
    }


    public Goods getGoodsById(long goodsId) {
        Goods goods = goodsDao.selectGoodsById(goodsId);
        if (goods == null) {
            throw HttpException.notFound("商品未找到！");
        }
        return goods;
    }

    public Map<Long, Goods> getIdToGoodsMap(List<Long> goodsIds) {
        GoodsExample goodsExample = new GoodsExample();
        goodsExample.createCriteria().andIdIn(goodsIds);
        List<Goods> goods = goodsMapper.selectByExample(goodsExample);
        return goods.stream().collect(Collectors.toMap(Goods::getId, x -> x));
    }
}
