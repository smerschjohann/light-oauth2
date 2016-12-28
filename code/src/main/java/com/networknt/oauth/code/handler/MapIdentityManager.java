package com.networknt.oauth.code.handler;

import com.hazelcast.core.IMap;
import com.networknt.utility.HashUtil;
import io.undertow.security.idm.Account;
import io.undertow.security.idm.Credential;
import io.undertow.security.idm.IdentityManager;
import io.undertow.security.idm.PasswordCredential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.spec.InvalidKeySpecException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Created by steve on 21/09/16.
 */
public class MapIdentityManager implements IdentityManager {
    Logger logger = LoggerFactory.getLogger(MapIdentityManager.class);

    private final IMap<String, Object> users;

    public MapIdentityManager(final IMap<String, Object> users) {
        this.users = users;
    }
    @Override
    public Account verify(Account account) {
        // An existing account so for testing assume still valid.
        return account;
    }
    @Override
    public Account verify(String id, Credential credential) {
        Account account = getAccount(id);
        if (account != null && verifyCredential(account, credential)) {
            return account;
        }

        return null;
    }
    @Override
    public Account verify(Credential credential) {
        // TODO Auto-generated method stub
        return null;
    }

    private boolean verifyCredential(Account account, Credential credential) {
        boolean match = false;
        if (credential instanceof PasswordCredential) {
            char[] password = ((PasswordCredential) credential).getPassword();
            Map<String, Object> map = (Map<String, Object>)users.get(account.getPrincipal().getName());
            String expectedPassword = ((String)map.get("password"));
            try {
                match = HashUtil.validatePassword(String.valueOf(password), expectedPassword);
            } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                logger.error("Exception:", e);
            }
        }
        if(logger.isDebugEnabled()) logger.debug("verfifyCredential = " + match);
        return match;
    }

    private Account getAccount(final String id) {
        if (users.containsKey(id)) {
            return new Account() {

                private final Principal principal = new Principal() {
                    @Override
                    public String getName() {
                        return id;
                    }
                };
                @Override
                public Principal getPrincipal() {
                    return principal;
                }
                @Override
                public Set<String> getRoles() {
                    return Collections.emptySet();
                }

            };
        }
        return null;
    }
}
