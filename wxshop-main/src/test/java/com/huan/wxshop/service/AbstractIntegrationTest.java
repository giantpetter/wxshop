package com.huan.wxshop.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huan.wxshop.entity.LoginResponse;
import com.huan.wxshop.generate.User;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.*;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.ClassicConfiguration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

import static com.huan.wxshop.service.TelVerificationServiceTest.VALID_PARAMETER;
import static com.huan.wxshop.service.TelVerificationServiceTest.VALID_PARAMETER_CODE;
import static java.net.HttpURLConnection.HTTP_OK;

public abstract class AbstractIntegrationTest {
    @Autowired
    Environment environment;
    @Value("${spring.datasource.url}")
    private String dataUrl;
    @Value("${spring.datasource.username}")
    private String dataUser;
    @Value("${spring.datasource.password}")
    private String password;

    @BeforeEach
    public void initDatabase() {
        ClassicConfiguration configuration = new ClassicConfiguration();
        configuration.setDataSource(dataUrl, dataUser, password);
        Flyway flyway = new Flyway(configuration);
        flyway.clean();
        flyway.migrate();
    }

    public UserLoginResponse loginAndGetCookie() throws URISyntaxException, IOException {
        CloseableHttpResponse response;
        //one
        response = doHttpRequest("status", null, HttpMethod.GET.toString());
        LoginResponse loginResponse = objectMapper.readValue(response.getEntity().getContent(), LoginResponse.class);
        Assertions.assertFalse(loginResponse.isLogin());
        response.close();

        //two
        response = doHttpRequest("code", VALID_PARAMETER, HttpMethod.POST.toString());
        int responseCode = response.getStatusLine().getStatusCode();
        Assertions.assertEquals(HTTP_OK, responseCode);
        response.close();

        //three
        response = doHttpRequest("login", VALID_PARAMETER_CODE, HttpMethod.POST.toString());
        List<Cookie> cookies = cookieStore.getCookies();
        Optional<Cookie> jsessionid = cookies.stream()
                .filter(cookie -> cookie.getName().equals("JSESSIONID"))
                .findFirst();
        response.close();
        //four
        response = doHttpRequest("status", VALID_PARAMETER_CODE, HttpMethod.GET.toString());
        LoginResponse userInResponse = objectMapper.readValue(response.getEntity().getContent(), LoginResponse.class);
        response.close();
        return new UserLoginResponse(jsessionid.get().getValue(), userInResponse.getUser());
    }

    static class UserLoginResponse {
        String cookie;
        User user;

        UserLoginResponse(String cookie, User user) {
            this.cookie = cookie;
            this.user = user;
        }
    }

    static final ObjectMapper objectMapper = new ObjectMapper();
    final CookieStore cookieStore = new BasicCookieStore();
    CloseableHttpClient client = HttpClients.custom().setDefaultCookieStore(cookieStore).build();

    public CloseableHttpResponse doHttpRequest(String apiName, Object requestBody, String method) throws URISyntaxException, IOException {
        URI url = new URI(getUrl(apiName));
        StringEntity entity;
        switch (method) {
            case "GET":
            case "get":
                HttpGet get = new HttpGet(url);
                return client.execute(get);
            case "POST":
            case "post":
                HttpPost post = new HttpPost(url);
                entity = new StringEntity(objectMapper.writeValueAsString(requestBody), ContentType.APPLICATION_JSON);
                post.setEntity(entity);
                return client.execute(post);
            case "DELETE":
            case "delete":
                HttpDelete delete = new HttpDelete(url);
                return client.execute(delete);
            case "PATCH":
            case "patch":
            default:
                HttpPatch patch = new HttpPatch(url);
                entity = new StringEntity(objectMapper.writeValueAsString(requestBody), ContentType.APPLICATION_JSON);
                patch.setEntity(entity);
                return client.execute(patch);
        }
    }


    private String getUrl(String apiName) {
        return "http://localhost:" + environment.getProperty("local.server.port") + "/api/v1/" + apiName;
    }
}
