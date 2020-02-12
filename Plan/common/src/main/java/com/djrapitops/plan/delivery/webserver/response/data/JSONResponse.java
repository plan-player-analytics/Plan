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
package com.djrapitops.plan.delivery.webserver.response.data;

import com.djrapitops.plan.delivery.webserver.response.ResponseType;
import com.djrapitops.plan.delivery.webserver.response.Response_old;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

/**
 * Generic JSON response implemented using Gson.
 * <p>
 * Returns a JSON version of the given object.
 *
 * @author Rsl1122
 */
public class JSONResponse extends Response_old {

    public JSONResponse(Object object) {
        this(new Gson().toJson(object));
    }

    public JSONResponse(JsonElement json) {
        this(json.getAsString());
    }

    public JSONResponse(String jsonString) {
        super(ResponseType.JSON);
        super.setHeader("HTTP/1.1 200 OK");
        super.setContent(jsonString);
    }
}