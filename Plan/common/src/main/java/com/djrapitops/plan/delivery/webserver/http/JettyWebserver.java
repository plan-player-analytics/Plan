package com.djrapitops.plan.delivery.webserver.http;

import com.djrapitops.plan.delivery.webserver.ResponseResolver;
import com.djrapitops.plan.delivery.webserver.configuration.WebserverConfiguration;
import com.djrapitops.plan.delivery.webserver.configuration.WebserverLogMessages;
import com.djrapitops.plan.exceptions.EnableException;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.WebserverSettings;
import org.eclipse.jetty.http2.server.HTTP2CServerConnectionFactory;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class JettyWebserver implements WebServer {

    private final PlanConfig config;
    private final WebserverConfiguration webserverConfiguration;
    private final Server webserver;
    private final JettyRequestHandler jettyRequestHandler;
    private final ResponseResolver responseResolver;
    private final WebserverLogMessages webserverLogMessages;

    private int port;

    @Inject
    public JettyWebserver(PlanConfig config, WebserverConfiguration webserverConfiguration, JettyRequestHandler jettyRequestHandler, ResponseResolver responseResolver) {
        this.config = config;
        this.webserverConfiguration = webserverConfiguration;
        webserverLogMessages = webserverConfiguration.getWebserverLogMessages();
        this.jettyRequestHandler = jettyRequestHandler;
        this.responseResolver = responseResolver;

        webserver = new Server();
    }

    @Override
    public void enable() {
        if (isEnabled()) return;

        if (webserverConfiguration.isWebserverDisabled()) {
            webserverLogMessages.warnWebserverDisabledByConfig();
        }

        this.port = webserverConfiguration.getPort();

        HttpConfiguration configuration = new HttpConfiguration();

        HttpConnectionFactory httpConnector = new HttpConnectionFactory(configuration);
        HTTP2CServerConnectionFactory http2Connector = new HTTP2CServerConnectionFactory(configuration);
        http2Connector.setConnectProtocolEnabled(true);

        ServerConnector connector = new ServerConnector(webserver, httpConnector, http2Connector);
        connector.setPort(port);
        webserver.addConnector(connector);

        webserver.setHandler(jettyRequestHandler);

        try {
            webserver.start();
        } catch (Exception e) {
            throw new EnableException("Failed to start Jetty webserver: " + e.toString());
        }

        webserverLogMessages.infoWebserverEnabled(getPort());

        responseResolver.registerPages();
    }

    @Override
    public boolean isEnabled() {
        return webserver.isStarting() || webserver.isStarted();
    }

    @Override
    public void disable() {
        try {
            webserver.stop();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public String getProtocol() {
        return isUsingHTTPS() ? "https//" : "http://";
    }

    @Override
    public boolean isUsingHTTPS() {
        return false;
    }

    @Override
    public boolean isAuthRequired() {
        return isUsingHTTPS() && config.isFalse(WebserverSettings.DISABLED_AUTHENTICATION);
    }

    @Override
    public int getPort() {
        return port;
    }
}
