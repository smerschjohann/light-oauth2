package com.networknt.oauth.token.handler;

import com.hazelcast.core.IMap;
import com.networknt.client.Client;
import com.networknt.config.Config;
import com.networknt.oauth.cache.CacheStartupHookProvider;
import com.networknt.oauth.cache.model.RefreshToken;
import com.networknt.server.Server;
import com.networknt.exception.ClientException;
import com.networknt.exception.ApiException;
import com.networknt.status.Status;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
* Generated by swagger-codegen
*/
public class Oauth2RefreshTokenRefreshTokenDeleteHandlerTest {
    @ClassRule
    public static TestServer server = TestServer.getInstance();

    static final Logger logger = LoggerFactory.getLogger(Oauth2RefreshTokenRefreshTokenDeleteHandlerTest.class);

    @Test
    public void testOauth2RefreshTokenDeleteHandler() throws ClientException, ApiException {
        // manually add a refresh token object into the cache.
        RefreshToken token = new RefreshToken();
        token.setRefreshToken("86c0a39f-0789-4b71-9fed-d99fe6dc9281");
        token.setUserId("admin");
        token.setClientId("6e9d1db3-2feb-4c1f-a5ad-9e93ae8ca59d");
        token.setScope("petstore.r petstore.w");
        IMap<String, RefreshToken> tokens = CacheStartupHookProvider.hz.getMap("tokens");
        tokens.put("86c0a39f-0789-4b71-9fed-d99fe6dc9281", token);

        CloseableHttpClient client = Client.getInstance().getSyncClient();
        HttpDelete httpDelete = new HttpDelete("http://localhost:6887/oauth2/refresh_token/86c0a39f-0789-4b71-9fed-d99fe6dc9281");
        try {
            CloseableHttpResponse response = client.execute(httpDelete);
            int statusCode = response.getStatusLine().getStatusCode();
            Assert.assertEquals(200, statusCode);
            Assert.assertEquals(0, tokens.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testRefreshTokenNotFound() throws ClientException, ApiException {
        CloseableHttpClient client = Client.getInstance().getSyncClient();
        HttpDelete httpDelete = new HttpDelete("http://localhost:6887/oauth2/refresh_token/fake");
        try {
            CloseableHttpResponse response = client.execute(httpDelete);
            int statusCode = response.getStatusLine().getStatusCode();
            String body = IOUtils.toString(response.getEntity().getContent(), "utf8");
            Assert.assertEquals(404, statusCode);
            if(statusCode == 404) {
                Status status = Config.getInstance().getMapper().readValue(body, Status.class);
                Assert.assertNotNull(status);
                Assert.assertEquals("ERR12029", status.getCode());
                Assert.assertEquals("REFRESH_TOKEN_NOT_FOUND", status.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
