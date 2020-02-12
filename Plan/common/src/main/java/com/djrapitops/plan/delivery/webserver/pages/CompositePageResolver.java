/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.delivery.webserver.pages;

import com.djrapitops.plan.delivery.webserver.Request;
import com.djrapitops.plan.delivery.webserver.RequestTarget;
import com.djrapitops.plan.delivery.webserver.auth.Authentication;
import com.djrapitops.plan.delivery.webserver.response.ResponseFactory;
import com.djrapitops.plan.delivery.webserver.response.Response_old;
import com.djrapitops.plan.exceptions.WebUserAuthException;
import com.djrapitops.plan.exceptions.connection.WebException;

import java.util.HashMap;
import java.util.Map;

/**
 * Extended {@link PageResolver} to implement layered resolution of addresses.
 * <p>
 * Allows resolving /example/foo and /example/bar with different PageResolvers as if the address being resolved
 * was at the root of the address (/example/foo - FooPageResolver sees /foo).
 * <p>
 * Tree-like pattern for URL resolution.
 *
 * @author Rsl1122
 */
@Deprecated
public abstract class CompositePageResolver implements PageResolver {

    protected final ResponseFactory responseFactory;

    private final Map<String, PageResolver> pages;

    public CompositePageResolver(ResponseFactory responseFactory) {
        this.responseFactory = responseFactory;
        pages = new HashMap<>();
    }

    @Deprecated
    public void registerPage(String targetPage, PageResolver resolver) {
        pages.put(targetPage, resolver);
    }

    @Deprecated
    public void registerPage(String targetPage, PageResolver resolver, int requiredPerm) {
        pages.put(targetPage, new PageResolver() {
            @Override
            public Response_old resolve(Request request, RequestTarget target) throws WebException {
                return resolver.resolve(request, target);
            }

            @Override
            public boolean isAuthorized(Authentication auth, RequestTarget target) throws WebUserAuthException {
                return auth.getWebUser().getPermLevel() <= requiredPerm;
            }
        });
    }

    @Deprecated
    public void registerPage(String targetPage, Response_old response, int requiredPerm) {
        pages.put(targetPage, new PageResolver() {
            @Override
            public Response_old resolve(Request request, RequestTarget target) {
                return response;
            }

            @Override
            public boolean isAuthorized(Authentication auth, RequestTarget target) throws WebUserAuthException {
                return auth.getWebUser().getPermLevel() <= requiredPerm;
            }
        });
    }

    @Override
    @Deprecated
    public Response_old resolve(Request request, RequestTarget target) throws WebException {
        PageResolver pageResolver = getPageResolver(target);
        return pageResolver != null
                ? pageResolver.resolve(request, target)
                : responseFactory.pageNotFound404();
    }

    @Deprecated
    public PageResolver getPageResolver(RequestTarget target) {
        if (target.isEmpty()) {
            return pages.get("");
        }
        String targetPage = target.get(0);
        target.removeFirst();
        return pages.get(targetPage);
    }

    @Deprecated
    public PageResolver getPageResolver(String targetPage) {
        return pages.get(targetPage);
    }
}
