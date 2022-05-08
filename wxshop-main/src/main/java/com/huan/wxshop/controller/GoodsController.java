package com.huan.wxshop.controller;

import com.huan.api.entity.PageResponse;
import com.huan.wxshop.entity.Response;
import com.huan.wxshop.generate.Goods;
import com.huan.wxshop.service.GoodsService;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/v1")
@SuppressFBWarnings({"EI_EXPOSE_REP2", "EI_EXPOSE_REP"})
public class GoodsController {
    private GoodsService goodsService;

    @Autowired
    public GoodsController(GoodsService goodsService) {
        this.goodsService = goodsService;
    }

    /**
     * @param goods    goods to be created
     * @param response the HTTP response
     * @return the newly created goods
     */
    // @formatter:on
    @PostMapping("/goods")
    public Response<Goods> createGoods(@RequestBody Goods goods, HttpServletResponse response) {
        clean(goods);
        response.setStatus(HttpServletResponse.SC_CREATED);
        return Response.of(goodsService.createGoods(goods));
    }

    @GetMapping("/goods")
    public PageResponse<Goods> getPagedGoods(@RequestParam("pageNum") Integer pageNum,
                                             @RequestParam("pageSize") Integer pageSize,
                                             @RequestParam(name = "shopId", required = false) Long shopId) {
        return goodsService.getAllGoods(pageNum, pageSize, shopId);
    }

    @GetMapping("/goods/{goodsId}")
    public Response<Goods> getGoodsById(@PathVariable("goodsId") Long goodsId) {
        return Response.of(goodsService.getGoodsById(goodsId));
    }


    private void clean(Goods goods) {
        goods.setId(null);
        goods.setCreatedAt(null);
        goods.setUpdatedAt(null);
    }

    @DeleteMapping("/goods/{id}")
    public Response<Goods> deleteGoods(@PathVariable("id") Long shopId) {
        return Response.of(goodsService.deleteGoodsById(shopId));
    }

    @PatchMapping("/goods/{id}")
    public Response<Goods> updateGoodsByID(@PathVariable("id") Long shopId,
                                           @RequestBody Goods goods) {
        return Response.of(goodsService.updateGoods(shopId, goods));
    }


}
