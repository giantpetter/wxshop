package com.huan.wxshop.controller;

import com.huan.api.entity.PageResponse;
import com.huan.wxshop.entity.Response;
import com.huan.wxshop.entity.ShoppingCartData;
import com.huan.wxshop.service.ShoppingCartService;
import com.huan.wxshop.service.UserContext;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@SuppressFBWarnings({"EI_EXPOSE_REP2", "EI_EXPOSE_REP"})
public class ShoppingCartController {

    private final ShoppingCartService shoppingCartService;

    @Autowired
    public ShoppingCartController(ShoppingCartService shoppingCartService) {
        this.shoppingCartService = shoppingCartService;
    }

    @GetMapping("/shoppingCart")
    public PageResponse<ShoppingCartData> getShoppingCart(@RequestParam("pageNum") int pageNum,
                                                          @RequestParam("pageSize") int pageSize) {

        return shoppingCartService.getShoppingCardOfUser(pageNum, pageSize);
    }

    @PostMapping("/shoppingCart")
    public Response<ShoppingCartData> addToShoppingCart(@RequestBody AddToShoppingCartRequest requestBody) {
        long userId = UserContext.getCurrentUser().getId();
        return Response.of(shoppingCartService.addToShoppingCart(requestBody, userId));
    }

    @DeleteMapping("/shoppingCart/{goodsId}")
    public Response<ShoppingCartData> deleteShoppingCartByGoodsId(@PathVariable("goodsId") Long goodsId) {
        return Response.of(shoppingCartService.deleteShoppingCartByGoodsId(goodsId));
    }

    @Setter
    @Getter
    public static class AddToShoppingCartRequest {
        List<AddToShoppingCartItem> goods;

    }

    @Setter
    @Getter
    public static class AddToShoppingCartItem {
        Long goodsId;
        int number;
    }

}
