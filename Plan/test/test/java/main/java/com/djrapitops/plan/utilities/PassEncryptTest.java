package test.java.main.java.com.djrapitops.plan.utilities;

import main.java.com.djrapitops.plan.utilities.PassEncryptUtil;
import org.junit.Before;
import org.junit.Test;
import test.java.utils.RandomData;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Fuzzlemann
 */
public class PassEncryptTest {

    private final Map<String, String> PASSWORD_MAP = new HashMap<>();

    @Before
    public void setUp() throws Exception {
        for (int i = 0; i < 20; i++) {
            String password = RandomData.randomString(RandomData.randomInt(1, 50));
            PASSWORD_MAP.put(password, PassEncryptUtil.createHash(password));
        };
    }

    @Test
    public void testVerification() throws Exception {
        for (Map.Entry<String, String> entry : PASSWORD_MAP.entrySet()) {
            String password = entry.getKey();
            String hash = entry.getValue();

            assertTrue(PassEncryptUtil.verifyPassword(password, hash));
            assertFalse(PassEncryptUtil.verifyPassword("WrongPassword", hash));
        }
    }
}
