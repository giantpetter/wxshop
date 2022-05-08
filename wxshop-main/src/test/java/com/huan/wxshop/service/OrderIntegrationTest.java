package com.huan.wxshop.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.huan.api.entity.*;
import com.huan.api.exceptions.HttpException;
import com.huan.api.generate.Order;
import com.huan.wxshop.WxshopApplication;
import com.huan.wxshop.entity.GoodsWithNumber;
import com.huan.wxshop.entity.OrderResponse;
import com.huan.wxshop.entity.Response;
import com.huan.wxshop.generate.Goods;
import com.huan.wxshop.generate.Shop;
import com.huan.wxshop.mock.MockRpcOrderService;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.TestPropertySource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = WxshopApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {"spring.config.location=classpath:application-test.yml"})
public class OrderIntegrationTest extends AbstractIntegrationTest {
    @Autowired
    MockRpcOrderService mockRpcOrderService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(mockRpcOrderService);
    }

    @Test
    public void dubboTest() throws URISyntaxException, IOException {
        CloseableHttpResponse response = doHttpRequest("test", null, HttpMethod.GET.name());
        String result = new BufferedReader(new InputStreamReader(response.getEntity().getContent())).readLine();
//        String result = objectMapper.readValue(response.getEntity().getContent(), String.class);
        Assertions.assertEquals("I'm mock!", result);
        response.close();
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

        when(mockRpcOrderService.rpcService.createOrder(any(), any())).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                Order order = invocationOnMock.getArgument(1);
                order.setId(1234L);
                return order;
            }
        });

        CloseableHttpResponse response;
        response = doHttpRequest("order", orderInfo, HttpMethod.POST.name());
        Response<OrderResponse> data = objectMapper.readValue(response.getEntity().getContent(), new TypeReference<Response<OrderResponse>>() {
        });
        response.close();

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
        response = doHttpRequest("order", orderInfo, HttpMethod.POST.name());
        Assertions.assertEquals(HttpStatus.SC_GONE, response.getStatusLine().getStatusCode());
        Response<OrderResponse> data = objectMapper.readValue(response.getEntity().getContent(), new TypeReference<Response<OrderResponse>>() {
        });
        response.close();

        cookieStore.clear();
        canCreateOrder();
    }

    @Test
    public void canDeleteOrder() throws URISyntaxException, IOException {
        when(mockRpcOrderService.rpcService.obtainPagedOrders(anyInt(), anyInt(), anyLong(), any())).thenAnswer((Answer<PageResponse<RpcOrderGoods>>) invocation -> {
            int pageNum = invocation.getArgument(0);
            int pageSize = invocation.getArgument(1);
            long userId = invocation.getArgument(2);
            DataStatus status = invocation.getArgument(3, DataStatus.class);
            return mockResponse(pageNum, pageSize, userId, status);
        });
        CloseableHttpResponse response;
        loginAndGetCookie();
        response = doHttpRequest("order?pageNum=3&pageSize=2&status=ok", null, HttpMethod.GET.name());
        PageResponse<OrderResponse> data = objectMapper.readValue(response.getEntity().getContent(),
                new TypeReference<PageResponse<OrderResponse>>() {
                });
        Assertions.assertEquals(3, data.getPageNum());
        Assertions.assertEquals(2, data.getPageSize());
        Assertions.assertEquals(10, data.getTotalPage());
        Assertions.assertEquals(100, data.getData().get(0).getId());
        Assertions.assertEquals(101, data.getData().get(1).getId());
        Assertions.assertEquals(Arrays.asList("shop2", "shop2"), data.getData()
                .stream().map(OrderResponse::getShop)
                .map(Shop::getName)
                .collect(Collectors.toList()));
        Assertions.assertEquals(Arrays.asList("goods3", "goods4"), data.getData()
                .stream()
                .map(OrderResponse::getGoods)
                .flatMap(List::stream)
                .map(Goods::getName)
                .collect(Collectors.toList()));
        Assertions.assertEquals(Arrays.asList(5, 2), data.getData()
                .stream()
                .map(OrderResponse::getGoods)
                .flatMap(List::stream)
                .map(GoodsWithNumber::getNumber).collect(Collectors.toList()));
        response.close();
        //删除订单
        when(mockRpcOrderService.deleteOrder(anyLong(), anyLong())).thenAnswer((Answer<RpcOrderGoods>) invocation -> {
            long orderId = invocation.getArgument(0);
            long userId = invocation.getArgument(1);
            return mockRpcOrderGoods(orderId, userId, 3L, 5, DataStatus.DELETE_STATUS);
        });
        response = doHttpRequest("order/100", null, HttpMethod.DELETE.name());
        Response<OrderResponse> deletedOrder = objectMapper.readValue(response.getEntity().getContent(), new TypeReference<Response<OrderResponse>>() {
        });
        Assertions.assertEquals(DataStatus.DELETE_STATUS.getStatus(), deletedOrder.getData().getStatus());
        Assertions.assertEquals(100L, deletedOrder.getData().getId());
        Assertions.assertEquals(1L, deletedOrder.getData().getUserId());
        Assertions.assertEquals("shop2", deletedOrder.getData().getShop().getName());
        Assertions.assertEquals(3L, deletedOrder.getData().getGoods().get(0).getId());
        Assertions.assertEquals(5, deletedOrder.getData().getGoods().get(0).getNumber());
    }

    @Test
    public void return400WhenGetOrder() throws URISyntaxException, IOException {
        CloseableHttpResponse response;
        loginAndGetCookie();
        response = doHttpRequest("order?pageNum=3&pageSize=2&status=haha", null, HttpMethod.GET.name());
        Assertions.assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatusLine().getStatusCode());
        response.close();
    }


    @Test
    public void allEventsWhenUpdateOrder() throws URISyntaxException, IOException {
        when(mockRpcOrderService.rpcService.updateOrder(anyLong(), any(), anyLong()))
                .thenAnswer((Answer<RpcOrderGoods>) invocation -> {
                    long orderId = invocation.getArgument(0);
                    Order order = invocation.getArgument(1);
                    long userId = invocation.getArgument(2);
                    if (order.getUserId() != userId) {
                        throw HttpException.forbidden("not allowed");
                    }
                    if (orderId != 1) {
                        throw HttpException.notFound("not found!");
                    }
                    RpcOrderGoods orderGoods = mockRpcOrderGoods(orderId, userId, 2L, 3, DataStatus.fromStatus(order.getStatus()));
                    orderGoods.setOrder(order);
                    return orderGoods;
                });
        CloseableHttpResponse response;
        loginAndGetCookie();
        Order orderToBeUpdated = new Order();
        orderToBeUpdated.setUserId(1L);
        response = doHttpRequest("order/2", orderToBeUpdated, HttpMethod.PATCH.name());
        Assertions.assertEquals(HttpStatus.SC_NOT_FOUND, response.getStatusLine().getStatusCode());
        response.close();

        orderToBeUpdated.setUserId(2L);
        response = doHttpRequest("order/1", orderToBeUpdated, HttpMethod.PATCH.name());
        Assertions.assertEquals(HttpStatus.SC_FORBIDDEN, response.getStatusLine().getStatusCode());
        response.close();

        orderToBeUpdated.setId(1L);
        orderToBeUpdated.setUserId(1L);
        orderToBeUpdated.setStatus(DataStatus.RECEIVED.getStatus());
        orderToBeUpdated.setExpressId("X");
        orderToBeUpdated.setExpressCompany("Shun");
        response = doHttpRequest("order/1", orderToBeUpdated, HttpMethod.PATCH.name());
        Response<OrderResponse> updatedOrder = objectMapper.readValue(response.getEntity().getContent(), new TypeReference<Response<OrderResponse>>() {
        });
        Assertions.assertEquals(DataStatus.RECEIVED.getStatus(), updatedOrder.getData().getStatus());
        Assertions.assertEquals("Shun", updatedOrder.getData().getExpressCompany());
        Assertions.assertEquals("X", updatedOrder.getData().getExpressId());
        Assertions.assertEquals(2L, updatedOrder.getData().getGoods().get(0).getId());
        Assertions.assertEquals(3, updatedOrder.getData().getGoods().get(0).getNumber());
        response.close();
    }

    private PageResponse<RpcOrderGoods> mockResponse(int pageNum, int pageSize, long userId, DataStatus status) {
        RpcOrderGoods order1 = mockRpcOrderGoods(100, userId, 3, 5, status);
        RpcOrderGoods order2 = mockRpcOrderGoods(101, userId, 4, 2, status);
        return PageResponse.pagedData(pageNum, pageSize, 10, Arrays.asList(order1, order2));
    }

    private RpcOrderGoods mockRpcOrderGoods(long orderId, long userId, long goodsId, int number, DataStatus status) {
        RpcOrderGoods rpcOrderGoods = new RpcOrderGoods();
        Order order = new Order();
        GoodsInfo goodsInfo = new GoodsInfo();

        goodsInfo.setGoodsId(goodsId);
        goodsInfo.setNumber(number);

        order.setId(orderId);
        order.setUserId(userId);
        order.setStatus(status.getStatus());

        rpcOrderGoods.setOrder(order);
        rpcOrderGoods.setGoods(Collections.singletonList(goodsInfo));
        return rpcOrderGoods;
    }
}
