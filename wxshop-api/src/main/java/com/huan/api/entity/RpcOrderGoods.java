package com.huan.api.entity;

import com.huan.api.generate.Order;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Setter
@Getter
public class RpcOrderGoods implements Serializable {
    private Order order;
    private List<GoodsInfo> goods;

    public RpcOrderGoods(Order order) {
        this.order = order;
    }

    public RpcOrderGoods() {
    }
}
