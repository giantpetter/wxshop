package com.huan.wxshop.service;

import com.huan.wxshop.WxshopApplication;
import com.huan.wxshop.entity.LoginResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;
import java.net.URISyntaxException;

import static com.huan.wxshop.service.TelVerificationServiceTest.EMPTY_TEL;
import static com.huan.wxshop.service.TelVerificationServiceTest.VALID_PARAMETER;
import static java.net.HttpURLConnection.*;

@SpringBootTest(classes = WxshopApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {"spring.config.location=classpath:application-test.yml"})
public class AuthIntegrationTest extends AbstractIntegrationTest {


    /**
     * 1.第一次登录 /api/status, 处于为登出状态
     * 2.发送验证码 /api/code
     * 3.带着验证码进行登录，/api/login,得到 Cookie
     * 4.带着 Cookie 访问，/api/status,处于登录状态
     * 5./api/logout，登出
     * 6. 再次带着 Cookie 访问 /api/status ，处于登出状态
     *
     * @throws IOException
     */
    @Test
    public void loginLogoutTest() throws IOException, URISyntaxException {
        CloseableHttpResponse response;
        String sessionId = loginAndGetCookie();

        //four
        response = doHttpRequest("status", null, true);
        LoginResponse loginResponse = objectMapper.readValue(response.getEntity().getContent(), LoginResponse.class);
        Assertions.assertTrue(loginResponse.isLogin());
        Assertions.assertEquals(loginResponse.getUser().getTel(), VALID_PARAMETER.getTel());
        response.close();

        //five + six
        doHttpRequest("logout", null, true);
        response = doHttpRequest("status", null, true);
        loginResponse = objectMapper.readValue(response.getEntity().getContent(), LoginResponse.class);
        Assertions.assertFalse(loginResponse.isLogin());

        response.close();
        cookieStore.clear();
        client.close();
    }


    @Test
    public void returnHttpOkWhenParameterIsCorrect() throws IOException, URISyntaxException {
        int responseCode = doHttpRequest("code", VALID_PARAMETER, false)
                .getStatusLine().getStatusCode();
        Assertions.assertEquals(HTTP_OK, responseCode);

    }

    @Test
    public void returnHttpBadRequestWhenParameterIsNotCorrect() throws IOException, URISyntaxException {
        CloseableHttpResponse response = doHttpRequest("code", EMPTY_TEL, false);
        int responseCode = response.getStatusLine().getStatusCode();
        Assertions.assertEquals(HTTP_BAD_REQUEST, responseCode);
        client.close();
    }

    @Test
    public void returnUnauthorisedWhenNotLogin() throws URISyntaxException, IOException {
        CloseableHttpResponse response = doHttpRequest("any", null, true);
        int responseCode = response.getStatusLine().getStatusCode();
        Assertions.assertEquals(HTTP_UNAUTHORIZED, responseCode);
    }


}
