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
}