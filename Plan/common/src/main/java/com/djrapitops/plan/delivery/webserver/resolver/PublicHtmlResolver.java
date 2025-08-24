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
package com.djrapitops.plan.delivery.webserver.resolver;

import com.djrapitops.plan.delivery.web.resolver.MimeType;
import com.djrapitops.plan.delivery.web.resolver.NoAuthResolver;
import com.djrapitops.plan.delivery.web.resolver.Response;
import com.djrapitops.plan.delivery.web.resolver.request.Request;
import com.djrapitops.plan.delivery.webserver.ResponseFactory;
import com.djrapitops.plan.identification.Identifiers;
import com.djrapitops.plan.utilities.dev.Untrusted;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;

/**
 * Resolves any files in public_html folder.
 *
 * @author AuroraLS3
 */
@Singleton
public class PublicHtmlResolver implements NoAuthResolver {

    private final ResponseFactory responseFactory;

    @Inject
    public PublicHtmlResolver(ResponseFactory responseFactory) {
        this.responseFactory = responseFactory;
    }

    @Override
    public Optional<Response> resolve(Request request) {
        return Optional.ofNullable(getResponse(request));
    }

    @SuppressWarnings("OptionalIsPresent") // More readable
    private Response getResponse(Request request) {
        @Untrusted String resource = request.getPath().asString().substring(1);
        @Untrusted Optional<Long> etag = Identifiers.getEtag(request);

        Optional<String> mimeType = getMimeType(resource);
        if (mimeType.isEmpty()) return null;

        return etag.map(tag -> responseFactory.publicHtmlResourceResponse(tag, resource, mimeType.get()))
                .orElseGet(() -> responseFactory.publicHtmlResourceResponse(resource, mimeType.get()));
    }

    private Optional<String> getMimeType(@Untrusted String resource) {
        return Optional.ofNullable(getNullableMimeType(resource));
    }

    // Checkstyle.OFF: CyclomaticComplexity
    // https://developer.mozilla.org/en-US/docs/Web/HTTP/Basics_of_HTTP/MIME_types/Common_types
    private String getNullableMimeType(@Untrusted String resource) {
        if (resource.endsWith(".avif")) return "image/avif";
        if (resource.endsWith(".bin")) return "application/octet-stream";
        if (resource.endsWith(".bmp")) return "image/bmp";
        if (resource.endsWith(".css")) return MimeType.CSS;
        if (resource.endsWith(".csv")) return "text/csv";
        if (resource.endsWith(".eot")) return MimeType.FONT_BYTESTREAM;
        if (resource.endsWith(".gif")) return MimeType.IMAGE;
        if (resource.endsWith(".html")) return MimeType.HTML;
        if (resource.endsWith(".htm")) return MimeType.HTML;
        if (resource.endsWith(".ico")) return "image/vnd.microsoft.icon";
        if (resource.endsWith(".ics")) return "text/calendar";
        if (resource.endsWith(".js")) return MimeType.JS;
        if (resource.endsWith(".jpeg")) return MimeType.IMAGE;
        if (resource.endsWith(".jpg")) return MimeType.IMAGE;
        if (resource.endsWith(".json")) return MimeType.JSON;
        if (resource.endsWith(".jsonld")) return "application/ld+json";
        if (resource.endsWith(".mjs")) return MimeType.JS;
        if (resource.endsWith(".otf")) return MimeType.FONT_BYTESTREAM;
        if (resource.endsWith(".pdf")) return "application/pdf";
        if (resource.endsWith(".php")) return "application/x-httpd-php";
        if (resource.endsWith(".png")) return MimeType.IMAGE;
        if (resource.endsWith(".rtf")) return "application/rtf";
        if (resource.endsWith(".svg")) return "image/svg+xml";
        if (resource.endsWith(".tif")) return "image/tiff";
        if (resource.endsWith(".tiff")) return "image/tiff";
        if (resource.endsWith(".ttf")) return "text/plain";
        if (resource.endsWith(".txt")) return "text/plain";
        if (resource.endsWith(".woff")) return MimeType.FONT_BYTESTREAM;
        if (resource.endsWith(".woff2")) return MimeType.FONT_BYTESTREAM;
        if (resource.endsWith(".xml")) return "application/xml";

        return null;
    }
    // Checkstyle.ON: CyclomaticComplexity

}