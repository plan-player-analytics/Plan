/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the LGNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  LGNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
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