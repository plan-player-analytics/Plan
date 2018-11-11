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
package com.djrapitops.plan.system.webserver.pages;

import com.djrapitops.plan.api.exceptions.WebUserAuthException;
import com.djrapitops.plan.api.exceptions.connection.WebException;
import com.djrapitops.plan.system.webserver.Request;
import com.djrapitops.plan.system.webserver.auth.Authentication;
import com.djrapitops.plan.system.webserver.response.Response;
import com.djrapitops.plan.system.webserver.response.ResponseFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstract PageHandler that allows Tree-like target deduction.
 *
 * @author Rsl1122
 */
public abstract class TreePageHandler implements PageHandler {

    protected final ResponseFactory responseFactory;

    private Map<String, PageHandler> pages;

    public TreePageHandler(ResponseFactory responseFactory) {
        this.responseFactory = responseFactory;
        pages = new HashMap<>();
    }

    public void registerPage(String targetPage, PageHandler handler) {
        pages.put(targetPage, handler);
    }

    public void registerPage(String targetPage, Response response, int requiredPerm) {
        pages.put(targetPage, new PageHandler() {
            @Override
            public Response getResponse(Request request, List<String> target) {
                return response;
            }

            @Override
            public boolean isAuthorized(Authentication auth, List<String> target) throws WebUserAuthException {
                return auth.getWebUser().getPermLevel() <= requiredPerm;
            }
        });
    }

    @Override
    public Response getResponse(Request request, List<String> target) throws WebException {
        PageHandler pageHandler = getPageHandler(target);
        return pageHandler != null
                ? pageHandler.getResponse(request, target)
                : responseFactory.pageNotFound404();
    }

    public PageHandler getPageHandler(List<String> target) {
        if (target.isEmpty()) {
            return pages.get("");
        }
        String targetPage = target.get(0);
        target.remove(0);
        return pages.get(targetPage);
    }

    public PageHandler getPageHandler(String targetPage) {
        return pages.get(targetPage);
    }
}
