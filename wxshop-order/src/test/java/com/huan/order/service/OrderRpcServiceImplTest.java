package com.huan.order.service;

import com.huan.api.entity.*;
import com.huan.api.exceptions.HttpException;
import com.huan.api.generate.Order;
import com.huan.order.mapper.MyOrderMapper;
import com.huan.order.mapper.OrderGoodsMapper;
import com.huan.order.mapper.OrderMapper;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.ClassicConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class OrderRpcServiceImplTest {
    private final String dataUrl = "jdbc:mysql://127.0.0.1:3308/order?useSSL=false&allowPublicKeyRetrieval=true";
    private final String dataUser = "root";
    private final String password = "root";
    private OrderRpcServiceImpl orderRpcService;
    private SqlSession sqlSession;

    @BeforeEach
    public void setUp() throws IOException {
        ClassicConfiguration configuration = new ClassicConfiguration();
        configuration.setDataSource(dataUrl, dataUser, password);
        Flyway flyway = new Flyway(configuration);
        flyway.clean();
        flyway.migrate();

        String configUrl = "test-config.xml";
        InputStream config = Resources.getResourceAsStream(configUrl);
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(config);
        sqlSession = sqlSessionFactory.openSession(true);

        orderRpcService = new OrderRpcServiceImpl(sqlSession.getMapper(OrderMapper.class)
                , sqlSession.getMapper(MyOrderMapper.class)
                , sqlSession.getMapper(OrderGoodsMapper.class));

    }

    @AfterEach
    public void cleanUp() {
        sqlSession.close();
    }

    @Test
    public void canCreateOrder() {
        OrderInfo orderInfo = new OrderInfo();

        GoodsInfo goodsInfo1 = new GoodsInfo(1, 3);
        GoodsInfo goodsInfo2 = new GoodsInfo(2, 3);
        orderInfo.setGoods(Arrays.asList(goodsInfo1, goodsInfo2));

        Order order = new Order();
        order.setUserId(1L);
        order.setAddress("火星");
        order.setTotalPrice(BigDecimal.valueOf(130L));

        Order orderWithId = orderRpcService.createOrder(orderInfo, order);

        Assertions.assertNotNull(orderWithId.getId());

        Order orderInDB = orderRpcService.selectOrderById(orderWithId.getId());

        Assertions.assertEquals("火星", orderInDB.getAddress());
        Assertions.assertEquals(1L, orderInDB.getUserId());
        Assertions.assertEquals(BigDecimal.valueOf(130L), orderInDB.getTotalPrice());
        Assertions.assertEquals(DataStatus.PENDING.getStatus(), orderInDB.getStatus());

    }

    @Test
    public void canDeleteOrder() {
        HttpException exception = Assertions.assertThrows(HttpException.class, () -> {
            orderRpcService.deleteOrder(2222L, 1L);
        });
        Assertions.assertEquals(HttpStatus.NOT_FOUND.value(), exception.getStatusCode());
        exception = Assertions.assertThrows(HttpException.class, () -> {
            orderRpcService.deleteOrder(1L, 2L);
        });
        Assertions.assertEquals(HttpStatus.FORBIDDEN.value(), exception.getStatusCode());

        RpcOrderGoods result = orderRpcService.deleteOrder(1L, 1L);
        Order orderDeleted = orderRpcService.selectOrderById(1L);
        Assertions.assertEquals("address1", result.getOrder().getAddress());
        Assertions.assertEquals(orderDeleted, result.getOrder());
        Assertions.assertEquals(DataStatus.DELETE_STATUS.getStatus(), orderDeleted.getStatus());
        Assertions.assertEquals(Arrays.asList(2L, 3L), result.getGoods()
                .stream().map(GoodsInfo::getGoodsId).collect(Collectors.toList()));
        Assertions.assertEquals(Arrays.asList(3, 4), result.getGoods()
                .stream().map(GoodsInfo::getNumber).collect(Collectors.toList()));
    }

    @Test
    public void canObtainAllOrders() {
        PageResponse<RpcOrderGoods> rpcOrderGoods = orderRpcService.obtainPagedOrders(2, 1, 1L, null);
        Order orderInPage = rpcOrderGoods.getData().get(0).getOrder();
        Order orderInDB = orderRpcService.selectOrderById(2L);
        Assertions.assertEquals(2, rpcOrderGoods.getTotalPage());
        Assertions.assertEquals(2, rpcOrderGoods.getPageNum());
        Assertions.assertEquals(1, rpcOrderGoods.getPageSize());
        Assertions.assertEquals(BigDecimal.valueOf(500L), orderInPage.getTotalPrice());
        Assertions.assertEquals(DataStatus.PENDING.getStatus(), orderInPage.getStatus());
        Assertions.assertEquals(1L, orderInPage.getUserId());
        Assertions.assertEquals("address2", orderInPage.getAddress());
        Assertions.assertEquals("company2", orderInPage.getExpressCompany());
        Assertions.assertEquals("2", orderInPage.getExpressId());

        List<GoodsInfo> goodsInfo = rpcOrderGoods.getData().get(0).getGoods();
        Assertions.assertEquals(Arrays.asList(2L, 3L), goodsInfo
                .stream().map(GoodsInfo::getGoodsId).collect(Collectors.toList()));
        Assertions.assertEquals(Arrays.asList(5, 7), goodsInfo
                .stream().map(GoodsInfo::getNumber).collect(Collectors.toList()));


    }

    @Test
    public void canUpdateOrder() {
        Order orderToBeUpdated = new Order();
        HttpException exception = Assertions.assertThrows(HttpException.class, () -> {
            orderRpcService.updateOrder(2222L, orderToBeUpdated, 1L);
        });
        Assertions.assertEquals(HttpStatus.NOT_FOUND.value(), exception.getStatusCode());
        exception = Assertions.assertThrows(HttpException.class, () -> {
            orderRpcService.updateOrder(1L, orderToBeUpdated, 2L);
        });
        Assertions.assertEquals(HttpStatus.FORBIDDEN.value(), exception.getStatusCode());

        orderToBeUpdated.setId(1L);
        orderToBeUpdated.setStatus(DataStatus.RECEIVED.getStatus());
        orderToBeUpdated.setExpressCompany("Shun");
        orderToBeUpdated.setExpressId("S");

        RpcOrderGoods result = orderRpcService.updateOrder(1L, orderToBeUpdated, 1L);
        Order order = orderRpcService.selectOrderById(1L);

        Assertions.assertEquals(order, result.getOrder());
        Assertions.assertEquals("Shun", result.getOrder().getExpressCompany());
        Assertions.assertEquals("S", result.getOrder().getExpressId());
        Assertions.assertEquals(DataStatus.RECEIVED.getStatus(), result.getOrder().getStatus());
        Assertions.assertEquals(Arrays.asList(2L, 3L), result.getGoods()
                .stream().map(GoodsInfo::getGoodsId).collect(Collectors.toList()));
        Assertions.assertEquals(Arrays.asList(3, 4), result.getGoods()
                .stream().map(GoodsInfo::getNumber).collect(Collectors.toList()));
    }


}
