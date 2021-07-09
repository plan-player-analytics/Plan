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
