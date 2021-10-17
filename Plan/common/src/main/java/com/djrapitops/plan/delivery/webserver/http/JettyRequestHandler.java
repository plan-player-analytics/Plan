package com.djrapitops.plan.delivery.webserver.http;

import com.djrapitops.plan.delivery.web.resolver.Response;
import com.djrapitops.plan.delivery.webserver.Addresses;
import com.djrapitops.plan.delivery.webserver.auth.AuthenticationExtractor;
import com.djrapitops.plan.delivery.webserver.configuration.WebserverConfiguration;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.PluginSettings;
import com.djrapitops.plan.utilities.logging.ErrorContext;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.playeranalytics.plugin.server.PluginLogger;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;

@Singleton
public class JettyRequestHandler extends AbstractHandler {

    private final WebserverConfiguration webserverConfiguration;
    private final AuthenticationExtractor authenticationExtractor;
    private final Addresses addresses;
    private final RequestHandler requestHandler;
    private final PlanConfig config;
    private final PluginLogger logger;
    private final ErrorLogger errorLogger;

    @Inject
    public JettyRequestHandler(WebserverConfiguration webserverConfiguration, AuthenticationExtractor authenticationExtractor, Addresses addresses, RequestHandler requestHandler, PlanConfig config, PluginLogger logger, ErrorLogger errorLogger) {
        this.webserverConfiguration = webserverConfiguration;
        this.authenticationExtractor = authenticationExtractor;
        this.addresses = addresses;
        this.requestHandler = requestHandler;
        this.config = config;
        this.logger = logger;
        this.errorLogger = errorLogger;
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws IOException, ServletException {
        try {
            InternalRequest internalRequest = new JettyInternalRequest(baseRequest, servletRequest, webserverConfiguration, authenticationExtractor);
            Response response = requestHandler.getResponse(internalRequest);
            new JettyResponseSender(response, servletRequest, servletResponse, addresses).send();
            baseRequest.setHandled(true);
        } catch (Exception e) {
            if (config.isTrue(PluginSettings.DEV_MODE)) {
                logger.warn("THIS ERROR IS ONLY LOGGED IN DEV MODE:");
                errorLogger.warn(e, ErrorContext.builder()
                        .whatToDo("THIS ERROR IS ONLY LOGGED IN DEV MODE")
                        .related(baseRequest.getMethod(), baseRequest.getRemoteAddr(), target, baseRequest.getRequestURI())
                        .build());
            }
        }

    }
}
