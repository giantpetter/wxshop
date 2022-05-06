package com.huan.wxshop.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.huan.api.entity.DataStatus;
import com.huan.api.entity.GoodsInfo;
import com.huan.api.entity.OrderInfo;
import com.huan.api.generate.Order;
import com.huan.wxshop.WxshopApplication;
import com.huan.wxshop.entity.GoodsWithNumber;
import com.huan.wxshop.entity.OrderResponse;
import com.huan.wxshop.entity.Response;
import com.huan.wxshop.mock.MockRpcOrderService;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.stream.Collectors;

@SpringBootTest(classes = WxshopApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {"spring.config.location=classpath:application-test.yml"})
public class OrderIntegrationTest extends AbstractIntegrationTest {
    @Autowired
    MockRpcOrderService mockRpcOrderService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(mockRpcOrderService);
        Mockito.when(mockRpcOrderService.rpcService.createOrder(Mockito.any(), Mockito.any())).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                Order order = invocationOnMock.getArgument(1);
                order.setId(1234L);
                return order;
            }
        });
    }

    @Test
    public void canCreateOrder() throws URISyntaxException, IOException {
        loginAndGetCookie();
        OrderInfo orderInfo = new OrderInfo();
        GoodsInfo goodsInfo1 = new GoodsInfo();
        GoodsInfo goodsInfo2 = new GoodsInfo();
        goodsInfo1.setGoodsId(3L);
        goodsInfo1.setNumber(2);
        goodsInfo2.setGoodsId(5L);
        goodsInfo2.setNumber(5);
        orderInfo.setGoods(Arrays.asList(goodsInfo1, goodsInfo2));

        CloseableHttpResponse response;
        response = doHttpRequest("order", orderInfo, "post");
        Response<OrderResponse> data = objectMapper.readValue(response.getEntity().getContent(), new TypeReference<Response<OrderResponse>>() {
        });

        Assertions.assertEquals(1234L, data.getData().getId());
        Assertions.assertEquals(2L, data.getData().getShop().getId());
        Assertions.assertEquals("shop2", data.getData().getShop().getName());
        Assertions.assertEquals(DataStatus.PENDING.getStatus(), data.getData().getStatus());
        Assertions.assertEquals("火星", data.getData().getAddress());
        Assertions.assertEquals(Arrays.asList(3L, 5L), data.getData().getGoods()
                .stream().map(GoodsWithNumber::getId).collect(Collectors.toList()));
        Assertions.assertEquals(Arrays.asList(2, 5), data.getData().getGoods()
                .stream().map(GoodsWithNumber::getNumber).collect(Collectors.toList()));

    }

    @Test
    public void canRollBackIfDeductStockFailed() throws URISyntaxException, IOException {
        loginAndGetCookie();
        OrderInfo orderInfo = new OrderInfo();
        GoodsInfo goodsInfo1 = new GoodsInfo();
        GoodsInfo goodsInfo2 = new GoodsInfo();
        goodsInfo1.setGoodsId(3L);
        goodsInfo1.setNumber(2);
        goodsInfo2.setGoodsId(5L);
        goodsInfo2.setNumber(6);
        orderInfo.setGoods(Arrays.asList(goodsInfo1, goodsInfo2));

        CloseableHttpResponse response;
        response = doHttpRequest("order", orderInfo, "post");
        Assertions.assertEquals(HttpStatus.SC_GONE, response.getStatusLine().getStatusCode());
        Response<OrderResponse> data = objectMapper.readValue(response.getEntity().getContent(), new TypeReference<Response<OrderResponse>>() {
        });
        canCreateOrder();
    }
}
