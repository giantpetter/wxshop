package com.huan.wxshop.dao;

import com.huan.wxshop.entity.DataStatus;
import com.huan.wxshop.generate.Goods;
import com.huan.wxshop.generate.GoodsExample;
import com.huan.wxshop.generate.GoodsMapper;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@SuppressFBWarnings("EI_EXPOSE_REP2")
public class GoodsDao {
    private final GoodsMapper goodsMapper;

    @Autowired
    public GoodsDao(GoodsMapper goodsMapper) {
        this.goodsMapper = goodsMapper;
    }


    public Goods insertGoods(Goods goods) {
        goodsMapper.insertSelective(goods);
        return selectGoodsById(goods.getId());
    }

    public Goods selectGoodsById(Long id) {
        return goodsMapper.selectByPrimaryKey(id);
    }

    public int deleteGoodsById(Goods goods) {

        return goodsMapper.updateByPrimaryKey(goods);
    }

    public Integer countGoodsByShopId(Long shopId) {
        GoodsExample goodsExample = new GoodsExample();
        if (shopId == null) {
            goodsExample.createCriteria().andStatusEqualTo(DataStatus.OK.getStatus());
        } else {
            goodsExample.createCriteria().andStatusEqualTo(DataStatus.OK.getStatus())
                    .andShopIdEqualTo(shopId);
        }
        return (int) goodsMapper.countByExample(goodsExample);
    }

    public List<Goods> selectGoodsByPage(Integer pageNum, Integer pageSize, Long shopId) {
        GoodsExample goodsExample = new GoodsExample();
        if (shopId == null) {
            goodsExample.createCriteria().andStatusEqualTo(DataStatus.OK.getStatus());
        } else {
            goodsExample.createCriteria().andStatusEqualTo(DataStatus.OK.getStatus()).andShopIdEqualTo(shopId);
        }
        goodsExample.setLimit(pageSize);
        goodsExample.setOffset((pageNum - 1) * pageSize);
        return goodsMapper.selectByExample(goodsExample);
    }


    public int updateGoods(Goods goods) {
        return goodsMapper.updateByPrimaryKey(goods);
    }
}
