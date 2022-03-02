package com.huan.wxshop.controller;

import com.huan.wxshop.dao.GoodsDao;
import com.huan.wxshop.entity.Response;
import com.huan.wxshop.generate.Goods;
import com.huan.wxshop.service.GoodsService;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/v1")
@SuppressFBWarnings("EI_EXPOSE_REP2")
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
        try {
            response.setStatus(HttpServletResponse.SC_CREATED);
            return Response.of(goodsService.createGoods(goods));
        } catch (GoodsService.NotAuthorizedForShopException e) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return Response.of(e.getMessage(), null);
        }
    }

    private void clean(Goods goods) {
        goods.setId(null);
        goods.setCreatedAt(null);
        goods.setUpdatedAt(null);
    }

    @DeleteMapping("/goods/{id}")
    public Response<Goods> deleteGoods(@PathVariable("id") Long shopId, HttpServletResponse response) {
        try {
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            return Response.of(goodsService.deleteGoodsById(shopId));
        } catch (GoodsService.NotAuthorizedForShopException e) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return Response.of(e.getMessage(), null);
        } catch (GoodsDao.ResourceNotFoundException e) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return Response.of(e.getMessage(), null);
        }
    }


}