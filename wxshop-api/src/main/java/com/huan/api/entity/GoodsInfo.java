package com.huan.api.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GoodsInfo {
    private long goodsId;
    private int number;

    public GoodsInfo(long goodsId, int number) {
        this.goodsId = goodsId;
        this.number = number;
    }

    public GoodsInfo() {
    }
}
