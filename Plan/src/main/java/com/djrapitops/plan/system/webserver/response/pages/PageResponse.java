package com.djrapitops.plan.system.webserver.response.pages;

import com.djrapitops.plan.system.webserver.response.Response;
import com.djrapitops.plan.system.webserver.response.ResponseType;
import com.googlecode.htmlcompressor.compressor.HtmlCompressor;

/**
 * Response for all HTML Page responses.
 *
 * @author Rsl1122
 */
public class PageResponse extends Response {

    public PageResponse(ResponseType type) {
        super(type);
    }

    public PageResponse() {
    }

    @Override
    public void setContent(String content) {
        HtmlCompressor compressor = new HtmlCompressor();
        compressor.setRemoveIntertagSpaces(true);
        super.setContent(compressor.compress(content));
    }
}