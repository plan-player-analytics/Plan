package com.djrapitops.plan.utilities;

import org.junit.Test;

import java.security.NoSuchAlgorithmException;

import static org.junit.Assert.assertEquals;

public class SHA256HashTest {

    @Test
    public void sameStringReturnsSameHash() throws NoSuchAlgorithmException {
        String expected = new SHA256Hash("1.3.4.5").create();
        String result = new SHA256Hash("1.3.4.5").create();
        assertEquals(expected, result);
    }

}