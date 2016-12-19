package com.networknt.oauth.code.auth;

import com.networknt.exception.ApiException;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.FlexBase64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

import static io.undertow.util.Headers.AUTHORIZATION;
import static io.undertow.util.Headers.BASIC;

/**
 * Created by stevehu on 2016-12-18.
 */
public class BasicAuthentication implements Authentication {
    static Logger logger = LoggerFactory.getLogger(BasicAuthentication.class);

    private static final String BASIC_PREFIX = BASIC + " ";
    private static final String LOWERCASE_BASIC_PREFIX = BASIC_PREFIX.toLowerCase(Locale.ENGLISH);
    private static final int PREFIX_LENGTH = BASIC_PREFIX.length();
    private static final String COLON = ":";

    public boolean authenticate(HttpServerExchange exchange) throws ApiException {
        boolean result = false;
        List<String> authHeaders = exchange.getRequestHeaders().get(AUTHORIZATION);
        if (authHeaders != null) {
            for (String current : authHeaders) {
                if (current.toLowerCase(Locale.ENGLISH).startsWith(LOWERCASE_BASIC_PREFIX)) {

                    String base64Challenge = current.substring(PREFIX_LENGTH);
                    String plainChallenge = null;
                    try {
                        ByteBuffer decode = FlexBase64.decode(base64Challenge);
                        // assume charset is UTF_8
                        Charset charset = StandardCharsets.UTF_8;
                        plainChallenge = new String(decode.array(), decode.arrayOffset(), decode.limit(), charset);
                        logger.debug("Found basic auth header %s (decoded using charset %s) in %s", plainChallenge, charset, exchange);
                    } catch (IOException e) {
                        logger.error("IOException:", e);
                    }
                    int colonPos;
                    if (plainChallenge != null && (colonPos = plainChallenge.indexOf(COLON)) > -1) {
                        String userName = plainChallenge.substring(0, colonPos);
                        char[] password = plainChallenge.substring(colonPos + 1).toCharArray();

                        // match with db/cached user credentials.

                    }
                }
            }
        }
        return result;
    }
}
