package com.djrapitops.plan.delivery.webserver.resolver.json;

import com.djrapitops.plan.delivery.web.resolver.Resolver;
import com.djrapitops.plan.delivery.web.resolver.Response;
import com.djrapitops.plan.delivery.web.resolver.request.Request;
import com.djrapitops.plan.delivery.web.resolver.request.WebUser;
import com.djrapitops.plan.delivery.webserver.WebServer;
import dagger.Lazy;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Resolves requests for /v1/user
 *
 * @author Kopo942
 */
@Singleton
public class UserJSONResolver implements Resolver {

    private final Lazy<WebServer> webServer;

    @Inject
    public UserJSONResolver(Lazy<WebServer> webServer) {
        this.webServer = webServer;
    }

    @Override
    public boolean canAccess(Request request) {
        return true;
    }

    @Override
    public Optional<Response> resolve(Request request) {
        return Optional.of(getResponse(request));
    }

    private Response getResponse(Request request) {
        if (!webServer.get().isAuthRequired()) {
            return Response.builder()
                    .setStatus(404)
                    .setJSONContent("{}")
                    .build();
        }

        WebUser user = request.getUser().orElse(new WebUser(""));
        Map<String, Object> json = new HashMap<>();

        json.put("username", user.getUsername());
        json.put("linkedTo", user.getName());
        json.put("permissions", user.getPermissions());

        return Response.builder().setJSONContent(json).build();
    }
}
