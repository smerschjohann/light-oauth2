package com.networknt.oauth.user;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by steve on 1/28/2016.
 */
public class HashUtilTest {
    @Test
    public void testMd5Hex() {
        String md5 = HashUtil.md5Hex("stevehu@gmail.com");
        Assert.assertEquals(md5, "417bed6d9644f12d8bc709059c225c27");
    }
    @Test
    public void testPasswordHash() throws Exception {
        String password = "123456";
        String hashedPass = HashUtil.generateStorngPasswordHash(password);
        System.out.println("hashedPass = " + hashedPass);
        Assert.assertTrue(HashUtil.validatePassword(password, hashedPass));
    }
}