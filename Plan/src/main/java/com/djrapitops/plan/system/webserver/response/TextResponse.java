package com.djrapitops.plan.system.webserver.response;

/**
 * Response for raw text.
 *
 * @author Rsl1122
 */
public class TextResponse extends Response {

    public TextResponse(String content) {
        setHeader("HTTP/1.1 200 OK");
        setContent(content);
    }
}