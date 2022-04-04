package com.huan.wxshop.generate;

import java.io.Serializable;
import java.math.BigDecimal;

public class OrderGoods implements Serializable {
    private Long id;

    private Long goodsId;

    private BigDecimal number;

    private static final long serialVersionUID = 1L;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(Long goodsId) {
        this.goodsId = goodsId;
    }

    public BigDecimal getNumber() {
        return number;
    }

    public void setNumber(BigDecimal number) {
        this.number = number;
    }
}