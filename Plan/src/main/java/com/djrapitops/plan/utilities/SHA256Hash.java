package com.djrapitops.plan.utilities;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class SHA256Hash {

    private final String original;

    public SHA256Hash(String original) {
        this.original = original;
    }

    public String create() throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        digest.update(original.getBytes("UTF-8"));
        return Base64.getEncoder().encodeToString(digest.digest());
    }
}
