package com.huan.wxshop.controller;

import com.huan.wxshop.entity.PageResponse;
import com.huan.wxshop.entity.Response;
import com.huan.wxshop.generate.Shop;
import com.huan.wxshop.service.ShopService;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/v1")
@SuppressFBWarnings("EI_EXPOSE_REP2")
public class ShopController {
    ShopService shopService;

    @Autowired
    public ShopController(ShopService shopService) {
        this.shopService = shopService;
    }

    @PatchMapping("/shop/{id}")
    public Response<Shop> modifyShop(@PathVariable("id") Long shopId,
                                     @RequestBody Shop shop) {
        shop.setId(shopId);
        Shop data = shopService.updateShopById(shop);
        return Response.of(data);
    }

    @PostMapping("/shop")
    public Response<Shop> createShop(@RequestBody Shop shop, HttpServletResponse response) {
        Shop result = shopService.createShop(shop);
        response.setStatus(HttpServletResponse.SC_CREATED);
        return Response.of(result);
    }

    @DeleteMapping("/shop/{id}")
    public Response<Shop> deleteShop(@PathVariable("id") Long shopId) {
        Shop data = shopService.deleteShopById(shopId);
        return Response.of(data);
    }

    @GetMapping("/shop")
    public PageResponse<Shop> getPagedShopsByUserId(@RequestParam("pageNum") int pageNum,
                                                    @RequestParam("pageSize") int pageSize) {
        return shopService.getShopsByPage(pageNum, pageSize);
    }

    @GetMapping("/shop/{id}")
    public Response<Shop> getShopByShopId(@PathVariable("id") Long shopId) {
        Shop data = shopService.getShopByShopId(shopId);
        return Response.of(data);
    }

}
