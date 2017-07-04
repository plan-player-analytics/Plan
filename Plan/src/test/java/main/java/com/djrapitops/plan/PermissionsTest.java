/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.java.main.java.com.djrapitops.plan;

import main.java.com.djrapitops.plan.Permissions;
import static org.junit.Assert.*;
import org.junit.Test;
import test.java.utils.MockUtils;

/**
 *
 * @author Rsl1122
 */
public class PermissionsTest {

    /**
     *
     */
    public PermissionsTest() {
    }

    /**
     *
     */
    @Test
    public void testGetPermission() {
        assertEquals("plan.inspect.other", Permissions.INSPECT_OTHER.getPermission());
    }

}
