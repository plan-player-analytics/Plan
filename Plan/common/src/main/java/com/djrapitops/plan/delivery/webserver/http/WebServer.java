package com.djrapitops.plan.delivery.webserver.http;

import com.djrapitops.plan.SubSystem;

public interface WebServer extends SubSystem {
    @Override
    void enable();

    boolean isEnabled();

    @Override
    void disable();

    String getProtocol();

    boolean isUsingHTTPS();

    boolean isAuthRequired();

    int getPort();
}
