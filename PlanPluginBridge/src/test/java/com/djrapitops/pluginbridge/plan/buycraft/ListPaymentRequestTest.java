package com.djrapitops.pluginbridge.plan.buycraft;

import com.djrapitops.plan.api.exceptions.connection.ForbiddenException;
import org.junit.Test;

import java.io.IOException;

/**
 * Test for ListPaymentRequest.
 *
 * @author Rsl1122
 */
public class ListPaymentRequestTest {

    @Test
    public void testSuccess() throws IOException, ForbiddenException {
        new ListPaymentRequest("166473e780b59e84d6a19f1975c9282bfcc7a2a7").makeRequest();
    }
}