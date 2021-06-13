package com.djrapitops.plan.delivery.webserver.resolver;

import com.djrapitops.plan.delivery.web.resolver.Resolver;
import com.djrapitops.plan.delivery.web.resolver.Response;
import com.djrapitops.plan.delivery.web.resolver.request.Request;
import com.djrapitops.plan.delivery.webserver.ResponseFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;

@Singleton
public class ErrorsPageResolver implements Resolver {

    private final ResponseFactory responseFactory;

    @Inject
    public ErrorsPageResolver(ResponseFactory responseFactory) {this.responseFactory = responseFactory;}

    @Override
    public boolean canAccess(Request request) {
        return request.getUser().map(user -> user.hasPermission("page.server")).orElse(false);
    }

    @Override
    public Optional<Response> resolve(Request request) {
        return Optional.of(responseFactory.errorsPageResponse());
    }

}
