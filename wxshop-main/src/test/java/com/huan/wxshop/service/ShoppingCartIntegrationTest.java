package com.huan.wxshop.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.huan.wxshop.WxshopApplication;
import com.huan.wxshop.controller.ShoppingCartController;
import com.huan.api.entity.PageResponse;
import com.huan.wxshop.entity.Response;
import com.huan.wxshop.entity.ShoppingCartData;
import com.huan.wxshop.entity.GoodsWithNumber;
import com.huan.wxshop.generate.Goods;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

@SpringBootTest(classes = WxshopApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {"spring.config.location=classpath:application-test.yml"})
public class ShoppingCartIntegrationTest extends AbstractIntegrationTest {
    @Test
    public void canQueryShoppingCartData() throws URISyntaxException, IOException {
        CloseableHttpResponse response;
        UserLoginResponse userLoginResponse = loginAndGetCookie();
        response = doHttpRequest("shoppingCart?pageNum=2&pageSize=1", null, HttpMethod.GET.name());
        PageResponse<ShoppingCartData> data = objectMapper.readValue(response.getEntity().getContent(), new TypeReference<PageResponse<ShoppingCartData>>() {
        });
        Assertions.assertEquals(2, data.getPageNum());
        Assertions.assertEquals(1, data.getPageSize());
        Assertions.assertEquals(2, data.getTotalPage());
        Assertions.assertEquals(1, data.getData().size());
        Assertions.assertEquals(2, data.getData().get(0).getShop().getId());
        Assertions.assertEquals(Arrays.asList(4L, 5L),
                data.getData().get(0).getGoods().stream()
                        .map(GoodsWithNumber::getId).collect(Collectors.toList()));
        Assertions.assertEquals(Arrays.asList(BigDecimal.valueOf(100L), BigDecimal.valueOf(200L)),
                data.getData().get(0).getGoods().stream()
                        .map(GoodsWithNumber::getPrice).collect(Collectors.toList()));
        Assertions.assertEquals(Arrays.asList(200, 300),
                data.getData().get(0).getGoods().stream()
                        .map(GoodsWithNumber::getNumber).collect(Collectors.toList()));

    }

    @Test
    public void canAddShoppingCart() throws URISyntaxException, IOException {
        CloseableHttpResponse response;
        UserLoginResponse userLoginResponse = loginAndGetCookie();
        ShoppingCartController.AddToShoppingCartRequest requestBody = new ShoppingCartController.AddToShoppingCartRequest();
        ShoppingCartController.AddToShoppingCartItem item = new ShoppingCartController.AddToShoppingCartItem();
        item.setGoodsId(2L);
        item.setNumber(2);
        requestBody.setGoods(Collections.singletonList(item));
        response = doHttpRequest("shoppingCart", requestBody, HttpMethod.POST.name());
        Response<ShoppingCartData> data = objectMapper.readValue(response.getEntity().getContent(),
                new TypeReference<Response<ShoppingCartData>>() {
                });
        Assertions.assertEquals(1, data.getData().getShop().getId());
        Assertions.assertEquals(Arrays.asList(1L, 2L),
                data.getData().getGoods().stream().map(Goods::getId).collect(Collectors.toList()));
        Assertions.assertEquals(Arrays.asList(100, 2),
                data.getData().getGoods().stream().map(GoodsWithNumber::getNumber).collect(Collectors.toList()));
        Assertions.assertTrue(data.getData().getGoods().stream()
                .allMatch(goods -> goods.getShopId() == 1));
    }

    @Test
    public void badRequestWhenGoodsIdIllegal() throws URISyntaxException, IOException {
        CloseableHttpResponse response;
        UserLoginResponse userLoginResponse = loginAndGetCookie();
        ShoppingCartController.AddToShoppingCartRequest requestBody = new ShoppingCartController.AddToShoppingCartRequest();
        ShoppingCartController.AddToShoppingCartItem item1 = new ShoppingCartController.AddToShoppingCartItem();
        ShoppingCartController.AddToShoppingCartItem item2 = new ShoppingCartController.AddToShoppingCartItem();
        item1.setGoodsId(2L);
        item1.setNumber(2);
        item2.setGoodsId(3L);
        item2.setNumber(3);
        requestBody.setGoods(Arrays.asList(item1, item2));

        response = doHttpRequest("shoppingCart", requestBody, HttpMethod.POST.toString());
        Assertions.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.BAD_REQUEST.value());
    }

    @Test
    public void canDeleteShoppingCartByGoodsId() throws URISyntaxException, IOException {
        CloseableHttpResponse response;
        UserLoginResponse userLoginResponse = loginAndGetCookie();
        response = doHttpRequest("shoppingCart/4", null, HttpMethod.DELETE.name());
        Response<ShoppingCartData> data = objectMapper.readValue(response.getEntity().getContent(),
                new TypeReference<Response<ShoppingCartData>>() {
                });
        Assertions.assertEquals(Collections.singletonList(5L),
                data.getData().getGoods().stream().map(GoodsWithNumber::getId).collect(Collectors.toList()));
        Assertions.assertEquals(2L,
                data.getData().getShop().getId());
        Assertions.assertTrue(data.getData().getGoods()
                .stream()
                .map(GoodsWithNumber::getShopId)
                .allMatch(shopId -> shopId == 2L));

    }

    @Test
    public void deletedGoodsNotFound() throws URISyntaxException, IOException {
        CloseableHttpResponse response;
        UserLoginResponse userLoginResponse = loginAndGetCookie();
        response = doHttpRequest("shoppingCart/2", null, HttpMethod.DELETE.name());
        Assertions.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.NOT_FOUND.value());

    }
}
