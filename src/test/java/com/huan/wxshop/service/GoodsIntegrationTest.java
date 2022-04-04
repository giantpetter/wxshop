package com.huan.wxshop.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.huan.wxshop.WxshopApplication;
import com.huan.wxshop.entity.Response;
import com.huan.wxshop.generate.Goods;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = WxshopApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {"spring.config.location=classpath:application-test.yml"})
public class GoodsIntegrationTest extends AbstractIntegrationTest {

    @Test
    public void createGoods() throws URISyntaxException, IOException {
        loginAndGetCookie();
        Goods goods = new Goods();
        goods.setDescription("纯天然无污染肥皂");
        goods.setName("肥皂");
        goods.setDetails("这是一块好肥皂");
        goods.setImgUrl("https://img.url");
        goods.setPrice(BigDecimal.valueOf(500));
        goods.setStock(10);
        goods.setShopId(1L);

        CloseableHttpResponse response = doHttpRequest("goods", goods, false);
        Response<Goods> body = objectMapper.readValue(response.getEntity().getContent(), new TypeReference<Response<Goods>>() {
        });
        int code = response.getStatusLine().getStatusCode();
        Assertions.assertEquals(code, HttpServletResponse.SC_CREATED);
        Assertions.assertEquals("肥皂", body.getData().getName());

    }


    @Test
    public void deleteGoods() throws URISyntaxException, IOException {
        String cookie = loginAndGetCookie();

        Goods goods = new Goods();
        goods.setPrice(BigDecimal.valueOf(1000));
        goods.setStock(10);
        goods.setShopId(1L);
        goods.setImgUrl("http://url");
        goods.setDetails("这是一块好肥皂");
        goods.setName("肥皂");
        goods.setDescription("纯天然无污染肥皂");


    }
}
