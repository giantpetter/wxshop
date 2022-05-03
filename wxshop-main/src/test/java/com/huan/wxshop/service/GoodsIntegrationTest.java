package com.huan.wxshop.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.huan.wxshop.WxshopApplication;
import com.huan.wxshop.entity.DataStatus;
import com.huan.wxshop.entity.PageResponse;
import com.huan.wxshop.entity.Response;
import com.huan.wxshop.generate.Goods;
import com.huan.wxshop.generate.GoodsMapper;
import com.huan.wxshop.generate.Shop;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = WxshopApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {"spring.config.location=classpath:application-test.yml"})
public class GoodsIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    GoodsMapper goodsMapper;

    @Test
    public void createGoods() throws URISyntaxException, IOException {
        UserLoginResponse loginResponse = loginAndGetCookie();
        Shop shop = new Shop();
        shop.setName("我的微信店铺");
        shop.setImgUrl("http://shopUrl");
        shop.setDescription("我的微信店铺开张啦");
        CloseableHttpResponse shopResponse = doHttpRequest("shop", shop, HttpMethod.POST.toString());
        Response<Shop> shopInResponse = objectMapper.readValue(shopResponse.getEntity().getContent(), new TypeReference<Response<Shop>>() {
        });
        Assertions.assertEquals("我的微信店铺", shopInResponse.getData().getName());
        Assertions.assertEquals("我的微信店铺开张啦", shopInResponse.getData().getDescription());
        Assertions.assertEquals("http://shopUrl", shopInResponse.getData().getImgUrl());
        Assertions.assertEquals(DataStatus.OK.getStatus(), shopInResponse.getData().getStatus());
        Assertions.assertEquals(loginResponse.user.getId(), shopInResponse.getData().getOwnerUserId());

        Assertions.assertEquals(HttpServletResponse.SC_CREATED, shopResponse.getStatusLine().getStatusCode());


        Goods goods = new Goods();
        goods.setDescription("纯天然无污染肥皂");
        goods.setName("肥皂");
        goods.setDetails("这是一块好肥皂");
        goods.setImgUrl("https://img.url");
        goods.setPrice(BigDecimal.valueOf(500));
        goods.setStock(10);
        goods.setShopId(shopInResponse.getData().getId());

        CloseableHttpResponse response = doHttpRequest("goods", goods, HttpMethod.POST.toString());
        Response<Goods> goodsInResponse = objectMapper.readValue(response.getEntity().getContent(), new TypeReference<Response<Goods>>() {
        });
        int code = response.getStatusLine().getStatusCode();
        Assertions.assertEquals(code, HttpServletResponse.SC_CREATED);
        Assertions.assertEquals("肥皂", goodsInResponse.getData().getName());
        Assertions.assertEquals("ok", goodsInResponse.getData().getStatus());
        Assertions.assertEquals(shopInResponse.getData().getId(), goodsInResponse.getData().getShopId());

    }

    @Test
    public void return404IfGoodsNotFound() throws URISyntaxException, IOException {
        loginAndGetCookie();
        CloseableHttpResponse response = doHttpRequest("goods/7", null, HttpMethod.DELETE.name());
        Assertions.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.NOT_FOUND.value());

        Goods goods = goodsMapper.selectByPrimaryKey(6L);
        goods.setDescription("更新测试404");
        response = doHttpRequest("goods/7", goods, HttpMethod.PATCH.name());
        Assertions.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.NOT_FOUND.value());
    }


    @Test
    public void canNotManageShopNotOwned() throws URISyntaxException, IOException {
        loginAndGetCookie();
        Goods goods = new Goods();
        goods.setDescription("纯天然无污染肥皂");
        goods.setName("肥皂");
        goods.setDetails("这是一块好肥皂");
        goods.setImgUrl("https://img.url");
        goods.setPrice(BigDecimal.valueOf(500));
        goods.setStock(10);
        goods.setShopId(3L);

        CloseableHttpResponse response = doHttpRequest("goods", goods, HttpMethod.POST.toString());
        Assertions.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.FORBIDDEN.value());
        response.close();

        response = doHttpRequest("goods/6", null, HttpMethod.DELETE.name());
        Assertions.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.FORBIDDEN.value());
        response.close();

        goods = goodsMapper.selectByPrimaryKey(6L);
        goods.setDescription("更新测试401");
        CloseableHttpResponse patchResponse = doHttpRequest("goods/6", goods, HttpMethod.PATCH.name());
        Assertions.assertEquals(patchResponse.getStatusLine().getStatusCode(), HttpStatus.FORBIDDEN.value());
        patchResponse.close();

    }

    @Test
    public void canUpdateGoods() throws URISyntaxException, IOException {
        loginAndGetCookie();
        Goods goods = goodsMapper.selectByPrimaryKey(1L);
        goods.setDescription("更新测试200");
        goods.setStock(200);
        CloseableHttpResponse response = doHttpRequest("goods/1", goods, HttpMethod.PATCH.name());
        Response<Goods> data = objectMapper.readValue(response.getEntity().getContent(), new TypeReference<Response<Goods>>() {
        });
        Assertions.assertEquals(1L, data.getData().getId());
        Assertions.assertEquals("goods1", data.getData().getName());
        Assertions.assertEquals("更新测试200", data.getData().getDescription());
        Assertions.assertEquals("details1", data.getData().getDetails());
        Assertions.assertEquals(1L, data.getData().getShopId());
        Assertions.assertEquals(200, data.getData().getStock());
    }


    @Test
    public void deleteGoods() throws URISyntaxException, IOException {
        loginAndGetCookie();
        CloseableHttpResponse response = doHttpRequest("goods/2", null, HttpMethod.DELETE.name());
        Response<Goods> data = objectMapper.readValue(response.getEntity().getContent(), new TypeReference<Response<Goods>>() {
        });
        Assertions.assertEquals(2L, data.getData().getId());
        Assertions.assertEquals("goods2", data.getData().getName());
        Assertions.assertEquals("desc2", data.getData().getDescription());
        Assertions.assertEquals("details2", data.getData().getDetails());
        Assertions.assertEquals(1L, data.getData().getShopId());
        Assertions.assertEquals("deleted", data.getData().getStatus());


    }

    @Test
    public void testGetGoodsById() throws URISyntaxException, IOException {
        loginAndGetCookie();
        CloseableHttpResponse response = doHttpRequest("goods/2", null, HttpMethod.GET.name());
        Response<Goods> data = objectMapper.readValue(response.getEntity().getContent(), new TypeReference<Response<Goods>>() {
        });
        Assertions.assertEquals(2L, data.getData().getId());
        Assertions.assertEquals("goods2", data.getData().getName());
        Assertions.assertEquals("desc2", data.getData().getDescription());
        Assertions.assertEquals("details2", data.getData().getDetails());
        Assertions.assertEquals(1L, data.getData().getShopId());
    }

    @Test
    public void testGetGoodsNotFound() throws URISyntaxException, IOException {
        loginAndGetCookie();
        CloseableHttpResponse response = doHttpRequest("goods/12", null, HttpMethod.GET.name());
        Assertions.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.NOT_FOUND.value());
    }

    @Test
    public void testGetPagedGoods() throws URISyntaxException, IOException {
        loginAndGetCookie();
        CloseableHttpResponse response = doHttpRequest("goods?pageNum=2&pageSize=3", null, HttpMethod.GET.name());
        PageResponse<Goods> data = objectMapper.readValue(response.getEntity().getContent(), new TypeReference<PageResponse<Goods>>() {
        });
        List<Goods> goods = data.getData();
        Assertions.assertEquals(3, goods.size());
        Assertions.assertEquals(2, data.getPageNum());
        Assertions.assertEquals(3, data.getPageSize());
        Assertions.assertEquals(2, data.getTotalPage());
        Assertions.assertEquals(Arrays.asList(4L, 5L, 6L),
                goods.stream().map(Goods::getId).collect(Collectors.toList()));
        Assertions.assertEquals(Arrays.asList(2L, 2L, 3L),
                goods.stream().map(Goods::getShopId).collect(Collectors.toList()));
        Assertions.assertEquals(Arrays.asList("goods4", "goods5", "goods6"),
                goods.stream().map(Goods::getName).collect(Collectors.toList()));
        response.close();


        response = doHttpRequest("goods?pageNum=2&pageSize=2&shopId=2", null, HttpMethod.GET.name());
        data = objectMapper.readValue(response.getEntity().getContent(), new TypeReference<PageResponse<Goods>>() {
        });
        goods = data.getData();
        Assertions.assertEquals(1, goods.size());
        Assertions.assertEquals(2, data.getPageNum());
        Assertions.assertEquals(2, data.getPageSize());
        Assertions.assertEquals(2, data.getTotalPage());
        Assertions.assertTrue(goods.stream().map(Goods::getShopId).allMatch(x -> x == 2L));
        Assertions.assertEquals(Collections.singletonList(BigDecimal.valueOf(200)),
                goods.stream().map(Goods::getPrice).collect(Collectors.toList()));
        response.close();


    }


}
