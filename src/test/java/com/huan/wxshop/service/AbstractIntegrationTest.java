package com.huan.wxshop.service;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.ClassicConfiguration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;

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

    public String loginAndGetCookie() throws URISyntaxException, IOException {
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
        response.close();
        return jsessionid.get().getValue();
    }


    final ObjectMapper objectMapper = new ObjectMapper();
    final CookieStore cookieStore = new BasicCookieStore();
    CloseableHttpClient client = HttpClients.custom().setDefaultCookieStore(cookieStore).build();
    private final HttpPost post = new HttpPost();
    private final HttpGet get = new HttpGet();

    public CloseableHttpResponse doHttpRequest(String apiName, Object requestBody, boolean isGet) throws URISyntaxException, IOException {
        URI url = new URI(getUrl(apiName));
        if (isGet) {
            get.setURI(url);
            return client.execute(get);
        } else {
            StringEntity entity = new StringEntity(objectMapper.writeValueAsString(requestBody), ContentType.APPLICATION_JSON);
            post.setURI(url);
            post.setEntity(entity);
            return client.execute(post);
        }
    }

    private String getUrl(String apiName) {
        return "http://localhost:" + environment.getProperty("local.server.port") + "/api/v1/" + apiName;
    }
}
