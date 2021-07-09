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
package com.djrapitops.plan.delivery.webserver;

import com.djrapitops.plan.delivery.web.resolver.request.Request;
import com.djrapitops.plan.delivery.web.resolver.request.URIQuery;

public class RequestBodyConverter {
  /**
   * Get the body of a request as an url-encoded form.
   *
   * @return {@link URIQuery}.
   */
  public static URIQuery formBody(Request request) {
    if (
        "POST".equalsIgnoreCase(request.getMethod()) &&
            "application/x-www-form-urlencoded".equalsIgnoreCase(request.getHeader("Content-type").orElse(""))
    ) {
      return new URIQuery(new String(request.getRequestBody()));
    } else {
      return new URIQuery("");
    }
  }
}
