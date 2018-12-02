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
package com.djrapitops.plan.system.info.connection;

import com.djrapitops.plan.api.exceptions.connection.*;
import com.djrapitops.plan.api.exceptions.database.DBOpException;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.info.request.InfoRequest;
import com.djrapitops.plan.system.info.request.SetupRequest;
import com.djrapitops.plan.system.webserver.Request;
import com.djrapitops.plan.system.webserver.response.Response;
import com.djrapitops.plugin.utilities.Verify;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class ConnectionIn {

    private final Map<String, String> variables;
    private final InfoRequest infoRequest;
    private final Database database;
    private final ConnectionSystem connectionSystem;

    public ConnectionIn(
            Request httpRequest, InfoRequest infoRequest,
            Database database,
            ConnectionSystem connectionSystem
    ) throws WebException {
        this.database = database;
        this.connectionSystem = connectionSystem;
        Verify.nullCheck(httpRequest, infoRequest);

        this.variables = readVariables(httpRequest);
        this.infoRequest = infoRequest;

        checkAuthentication();
    }

    private void checkAuthentication() throws WebException {
        UUID serverUUID = getServerUUID();

        try {
            if (database.check().isServerInDatabase(serverUUID)) {
                return;
            }
        } catch (DBOpException e) {
            throw new TransferDatabaseException(e);
        }

        if (infoRequest instanceof SetupRequest) {
            if (!connectionSystem.isSetupAllowed()) {
                throw new ForbiddenException("Setup not enabled on this server, use commands to enable.");
            }
        } else {
            throw new UnauthorizedServerException(serverUUID + " (Sender) was not found from database");
        }
    }

    private UUID getServerUUID() throws BadRequestException {
        String sender = variables.get("sender");
        Verify.nullCheck(sender, () -> new BadRequestException("Sender ('sender') variable not supplied in the request."));

        try {
            return UUID.fromString(sender);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Sender ('sender') was not a valid UUID: " + e.getMessage());
        }
    }

    private Map<String, String> readVariables(Request request) throws WebException {
        String requestBody = readRequestBody(request.getRequestBody());
        String[] bodyVariables = requestBody.split(";&variable;");

        return Arrays.stream(bodyVariables)
                .map(variable -> variable.split("=", 2))
                .filter(splitVariables -> splitVariables.length == 2)
                .collect(Collectors.toMap(splitVariables -> splitVariables[0], splitVariables -> splitVariables[1], (a, b) -> b));
    }

    public Response handleRequest() throws WebException {
        return infoRequest.handleRequest(variables);
    }

    private String readRequestBody(InputStream in) throws WebFailException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] bytes;

            byte[] buf = new byte[4096];
            for (int n = in.read(buf); n > 0; n = in.read(buf)) {
                out.write(buf, 0, n);
            }

            bytes = out.toByteArray();

            return new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new WebFailException("Exception while reading Request.", e);
        }
    }
}
