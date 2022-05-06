package com.huan.wxshop.entity;

import com.huan.wxshop.generate.Shop;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@SuppressFBWarnings({"EI_EXPOSE_REP2", "EI_EXPOSE_REP"})
public class ShoppingCartData {
    private Shop shop;
    private List<GoodsWithNumber> goods;

}
