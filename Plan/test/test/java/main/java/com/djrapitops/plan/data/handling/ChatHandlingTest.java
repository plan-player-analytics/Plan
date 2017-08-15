/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.java.main.java.com.djrapitops.plan.data.handling;

import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.data.handling.ChatHandling;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import test.java.utils.MockUtils;
import test.java.utils.TestInit;

import static org.junit.Assert.assertTrue;

/**
 * @author Rsl1122
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(JavaPlugin.class)
public class ChatHandlingTest {

    /**
     *
     */
    public ChatHandlingTest() {
    }

    /**
     *
     */
    @Before
    public void setUp() throws Exception {
        TestInit.init();
    }

    /**
     *
     */
    @Test
    public void testProcessChatInfoAddedNickname() {
        UserData data = MockUtils.mockUser2();
        String expected = "TestNicknameChatHandling";
        ChatHandling.processChatInfo(data, expected);
        assertTrue("Didn't add nickname", data.getNicknames().contains(expected));
    }
}
