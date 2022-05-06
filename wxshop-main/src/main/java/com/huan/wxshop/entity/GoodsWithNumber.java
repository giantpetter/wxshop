package com.huan.wxshop.entity;

import com.huan.wxshop.generate.Goods;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GoodsWithNumber extends Goods {
    int number;

    public GoodsWithNumber() {
    }

    public GoodsWithNumber(Goods goods) {
        this.setId(goods.getId());
        this.setShopId(goods.getShopId());
        this.setName(goods.getName());
        this.setDescription(goods.getDescription());
        this.setImgUrl(goods.getImgUrl());
        this.setPrice(goods.getPrice());
        this.setStock(goods.getStock());
        this.setCreatedAt(goods.getCreatedAt());
        this.setUpdatedAt(goods.getUpdatedAt());
        this.setStatus(goods.getStatus());

    }
}
