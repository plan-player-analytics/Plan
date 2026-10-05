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
package com.djrapitops.plan.delivery.web.resolver;

/**
 * Commonly used MIME types for web responses.
 *
 * @author AuroraLS3
 */
public final class MimeType {

    /**
     * MIME type text/html.
     */
    public static final String HTML = "text/html";
    /**
     * MIME type text/css.
     */
    public static final String CSS = "text/css";
    /**
     * MIME type application/json.
     */
    public static final String JSON = "application/json";
    /**
     * MIME type text/javascript.
     */
    public static final String JS = "text/javascript";
    /**
     * MIME type image/gif.
     */
    public static final String IMAGE = "image/gif";
    /**
     * MIME type image/x-icon.
     */
    public static final String FAVICON = "image/x-icon";
    /**
     * MIME type application/x-font-ttf.
     */
    public static final String FONT_TTF = "application/x-font-ttf";
    /**
     * MIME type application/font-woff.
     */
    public static final String FONT_WOFF = "application/font-woff";
    /**
     * MIME type application/font-woff2.
     */
    public static final String FONT_WOFF2 = "application/font-woff2";
    /**
     * MIME type application/vnd.ms-fontobject.
     */
    public static final String FONT_EOT = "application/vnd.ms-fontobject";
    /**
     * MIME type application/octet-stream.
     */
    public static final String FONT_BYTESTREAM = "application/octet-stream";

    private MimeType() {
        // Static variable class
    }
}
