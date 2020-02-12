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
import com.djrapitops.plan.delivery.webserver.response.Response_old;
import com.djrapitops.plan.exceptions.WebUserAuthException;
import com.djrapitops.plan.exceptions.connection.WebException;

/**
 * Used for Response resolution and authorization.
 *
 * @author Rsl1122
 * @see CompositePageResolver for larger depth resolution than 1.
 */
@Deprecated
public interface PageResolver {

    /**
     * Resolve the request to a response.
     *
     * @param request Request in case it is useful for choosing page.
     * @param target  Rest of the target coordinates after this page has been solved.
     * @return Appropriate response.
     */
    Response_old resolve(Request request, RequestTarget target) throws WebException;

    default boolean isAuthorized(Authentication auth, RequestTarget target) throws WebUserAuthException {
        return true;
    }

}
