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
package com.djrapitops.plan.system.webserver.response.data;

import com.djrapitops.plan.system.webserver.response.Response;
import com.djrapitops.plan.system.webserver.response.ResponseType;

/**
 * Generic JSON response implemented using Gson.
 * <p>
 * Returns a JSON version of the given object.
 *
 * @author Rsl1122
 */
public class JSONResponse<T> extends Response {

    public JSONResponse(T object) {
        super(ResponseType.JSON);
        super.setHeader("HTTP/1.1 200 OK");

        try {
            Class<?> gsonClass = Class.forName("com.google.gson.Gson");
            Object gson = gsonClass.getConstructor().newInstance();
            Object json = gsonClass.getMethod("toJson", Object.class).invoke(gson, object);

            super.setContent(json.toString());
        } catch (ReflectiveOperationException e) {
            super.setContent("{\"error\":\"Gson for json responses not available on this server: " + e.toString() + "\"}");
        }
    }

    public JSONResponse(String jsonString) {
        super(ResponseType.JSON);
        super.setHeader("HTTP/1.1 200 OK");
        super.setContent(jsonString);
    }
}