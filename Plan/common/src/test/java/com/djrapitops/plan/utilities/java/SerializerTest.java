package com.djrapitops.plan.utilities.java;

import com.djrapitops.plan.data.store.Type;
import com.djrapitops.plan.utilities.Base64Util;
import com.djrapitops.plugin.utilities.Format;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.Serializable;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;

/**
 * Test for {@link Serializer}.
 *
 * @author Rsl1122
 */
public class SerializerTest {

    private String store;

    @Before
    public void setUp() throws Exception {
        Function<String, String> function = (Function<String, String> & Serializable)
                string -> new Format(string).removeSymbols().toString();

        Serializer<Function<String, String>> serializer = new Serializer<>(Type.of(function));
        byte[] output = serializer.serialize(function);
        store = Base64Util.encodeBytes(output);
    }

    @Test
    public void test() throws IOException, ClassNotFoundException {
        Function<String, String> function = new Serializer<>(new Type<Function<String, String>>() {})
                .deserialize(Base64Util.decodeBytes(store));
        String result = function.apply("no-,.-.,-.,-.,-");
        assertEquals("no", result);
    }
}