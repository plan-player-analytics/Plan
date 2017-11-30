/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.java.com.djrapitops.plan;

import main.java.com.djrapitops.plan.settings.Permissions;
import org.junit.Test;
import test.java.utils.TestUtils;

import static org.junit.Assert.assertEquals;

/**
 * @author Rsl1122
 */
public class PermissionsTest {

    @Test
    public void testGetPermission() throws NoSuchFieldException, IllegalAccessException {
        for (Permissions type : Permissions.values()) {
            String exp = TestUtils.getStringFieldValue(type, "permission");

            assertEquals(exp, type.getPermission());
            assertEquals(exp, type.getPerm());
        }
    }
}