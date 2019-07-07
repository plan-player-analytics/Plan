package com.djrapitops.plan.system.webserver.pages.json;

import com.djrapitops.plan.api.exceptions.WebUserAuthException;
import com.djrapitops.plan.api.exceptions.connection.WebException;
import com.djrapitops.plan.system.Identifiers;
import com.djrapitops.plan.system.json.TabJSONParser;
import com.djrapitops.plan.system.webserver.Request;
import com.djrapitops.plan.system.webserver.RequestTarget;
import com.djrapitops.plan.system.webserver.auth.Authentication;
import com.djrapitops.plan.system.webserver.pages.PageHandler;
import com.djrapitops.plan.system.webserver.response.Response;
import com.djrapitops.plan.system.webserver.response.data.JSONResponse;

import java.util.UUID;
import java.util.function.Function;

/**
 * Generic Tab JSON handler for any tab's data.
 *
 * @author Rsl1122
 */
public class ServerTabJSONHandler<T> implements PageHandler {

    private final Identifiers identifiers;
    private final Function<UUID, T> jsonParser;

    public ServerTabJSONHandler(Identifiers identifiers, TabJSONParser<T> jsonParser) {
        this.identifiers = identifiers;
        this.jsonParser = jsonParser;
    }

    @Override
    public Response getResponse(Request request, RequestTarget target) throws WebException {
        UUID serverUUID = identifiers.getServerUUID(target); // Can throw BadRequestException
        return new JSONResponse<>(jsonParser.apply(serverUUID));
    }

    @Override
    public boolean isAuthorized(Authentication auth, RequestTarget target) throws WebUserAuthException {
        return auth.getWebUser().getPermLevel() <= 0;
    }
}