package com.huan.wxshop.dao;

import com.huan.wxshop.entity.DataStatus;
import com.huan.wxshop.generate.Shop;
import com.huan.wxshop.generate.ShopExample;
import com.huan.wxshop.generate.ShopMapper;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

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

    public int updateShop(Shop shop) {
        return shopMapper.updateByPrimaryKey(shop);
    }

    public Shop insertShop(Shop shop) {
        shopMapper.insertSelective(shop);
        return selectShopById(shop.getId());
    }

    public Shop deleteShop(Shop shop) {
        shop.setStatus(DataStatus.DELETE_STATUS.getStatus());
        shop.setUpdatedAt(new Date());
        shopMapper.updateByPrimaryKeySelective(shop);
        return selectShopById(shop.getId());
    }

    public int selectCountShopsByUser(Long userId) {
        ShopExample example = new ShopExample();
        example.createCriteria().andStatusEqualTo(DataStatus.OK.getStatus())
                .andOwnerUserIdEqualTo(userId);
        return (int) shopMapper.countByExample(example);
    }

    public List<Shop> selectAllShopsByUser(Long userId, int pageNum, int pageSize) {
        ShopExample example = new ShopExample();
        example.createCriteria().andStatusEqualTo(DataStatus.OK.getStatus())
                .andOwnerUserIdEqualTo(userId);

        example.setLimit(pageNum);
        example.setOffset((pageNum - 1) * pageSize);
        return shopMapper.selectByExample(example);
    }
}
