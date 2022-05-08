package com.huan.wxshop.entity;

import com.huan.api.generate.Order;
import com.huan.wxshop.generate.Shop;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@SuppressFBWarnings({"EI_EXPOSE_REP2", "EI_EXPOSE_REP"})
public class OrderResponse extends Order {
    private Shop shop;
    private List<GoodsWithNumber> goods;

    public OrderResponse(Order order) {
        this.setUserId(order.getUserId());
        this.setTotalPrice(order.getTotalPrice());
        this.setUpdatedAt(order.getUpdatedAt());
        this.setExpressId(order.getExpressId());
        this.setExpressCompany(order.getExpressCompany());
        this.setCreatedAt(order.getCreatedAt());
        this.setId(order.getId());
        this.setAddress(order.getAddress());
        this.setStatus(order.getStatus());
    }

    public OrderResponse() {
    }
}
