package com.huan.wxshop.dao;

import com.huan.wxshop.entity.DataStatus;
import com.huan.wxshop.generate.Goods;
import com.huan.wxshop.generate.GoodsMapper;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

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

    public Goods deleteGoodsById(Long goodsId) {
        Goods goods = goodsMapper.selectByPrimaryKey(goodsId);
        if (goods == null) {
            throw new ResourceNotFoundException("Not Found");
        }
        goods.setStatus(DataStatus.DELETE_STATUS.getStatus());
        goodsMapper.updateByPrimaryKey(goods);
        return goods;
    }

    public static class ResourceNotFoundException extends RuntimeException {
        public ResourceNotFoundException(String message) {
            super(message);
        }
    }
}
