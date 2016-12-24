package com.networknt.oauth.code.auth;

import com.networknt.exception.ApiException;
import com.networknt.oauth.code.PathHandlerProvider;
import com.networknt.status.Status;
import com.networknt.utility.HashUtil;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.FlexBase64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static io.undertow.util.Headers.AUTHORIZATION;
import static io.undertow.util.Headers.BASIC;

/**
 * Created by stevehu on 2016-12-18.
 */
public class BasicAuthentication extends AbstractAuthentication {
    static Logger logger = LoggerFactory.getLogger(BasicAuthentication.class);
    static String INCORRECT_PASSWORD = "ERR12016";
    static String USER_NOT_FOUND = "ERR12013";
    static String RUNTIME_EXCEPTION = "ERR10010";

    private static final String BASIC_PREFIX = BASIC + " ";
    private static final String LOWERCASE_BASIC_PREFIX = BASIC_PREFIX.toLowerCase(Locale.ENGLISH);
    private static final int PREFIX_LENGTH = BASIC_PREFIX.length();
    private static final String COLON = ":";

    @Override
    public String authenticate(HttpServerExchange exchange) throws ApiException {
        String result = null;
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
                        int colonPos;
                        if (plainChallenge != null && (colonPos = plainChallenge.indexOf(COLON)) > -1) {
                            String userId = plainChallenge.substring(0, colonPos);
                            String password = plainChallenge.substring(colonPos + 1);
                            // match with db/cached user credentials.
                            Map<String, Object> user = (Map<String, Object>)PathHandlerProvider.users.get(userId);
                            if(user == null) {
                                user = selectUser(userId);
                            }
                            if(user == null) {
                                throw new ApiException(new Status(USER_NOT_FOUND));
                            }
                            if(!HashUtil.validatePassword(password, (String)user.get("password"))) {
                                throw new ApiException(new Status(INCORRECT_PASSWORD));
                            }
                            result = userId;
                        }
                    } catch (NoSuchAlgorithmException | InvalidKeySpecException | IOException e) {
                        logger.error("Exception:", e);
                        throw new ApiException(new Status(RUNTIME_EXCEPTION));
                    }
                }
            }
        }
        return result;
    }


}
