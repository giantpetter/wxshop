package com.huan.wxshop.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.kevinsawicki.http.HttpRequest;
import com.huan.wxshop.WxshopApplication;
import com.huan.wxshop.controller.AuthController;
import com.huan.wxshop.entity.LoginResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

import static com.huan.wxshop.service.TelVerificationServiceTest.*;
import static java.net.HttpURLConnection.*;

@SpringBootTest(classes = WxshopApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application.yml")
public class AuthIntegrationTest {
    @Autowired
    Environment environment;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final CookieStore cookieStore = new BasicCookieStore();
    private CloseableHttpClient client = HttpClients.custom().setDefaultCookieStore(cookieStore).build();
    private final HttpPost post = new HttpPost();
    private final HttpGet get = new HttpGet();

    private CloseableHttpResponse doHttpRequest(String apiName, AuthController.TelAndCode telAndCode, boolean isGet) throws URISyntaxException, IOException {
        URI url = new URI(getUrl(apiName));
        if (isGet) {
            get.setURI(url);
            return client.execute(get);
        } else {
            StringEntity entity = new StringEntity(objectMapper.writeValueAsString(telAndCode), ContentType.APPLICATION_JSON);
            post.setURI(url);
            post.setEntity(entity);
            return client.execute(post);
        }
    }


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
        //one
        response = doHttpRequest("status", null, true);
        LoginResponse loginResponse = objectMapper.readValue(response.getEntity().getContent(), LoginResponse.class);
        Assertions.assertFalse(loginResponse.isLogin());
        response.close();

        //two
        response = doHttpRequest("code", VALID_PARAMETER, false);
        int responseCode = response.getStatusLine().getStatusCode();
        Assertions.assertEquals(HTTP_OK, responseCode);
        response.close();

        //three
        response = doHttpRequest("login", VALID_PARAMETER_CODE, false);
        List<Cookie> cookies = cookieStore.getCookies();
        Optional<Cookie> jsessionid = cookies.stream()
                .filter(cookie -> cookie.getName().equals("JSESSIONID"))
                .findFirst();
        System.out.println("JSESSIONID : " + jsessionid.get().getValue());
        response.close();

        //four
        response = doHttpRequest("status", null, true);
        loginResponse = objectMapper.readValue(response.getEntity().getContent(), LoginResponse.class);
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
        int responseCode = HttpRequest.post(getUrl("code"))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .send(objectMapper.writeValueAsString(VALID_PARAMETER))
                .code();
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

    private String getUrl(String apiName) {
        return "http://localhost:" + environment.getProperty("local.server.port") + "/api/v1/" + apiName;
    }

}
