package com.djrapitops.plan.system.info.connection;

import com.djrapitops.plan.api.exceptions.connection.*;
import com.djrapitops.plan.api.exceptions.database.DBException;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.info.request.InfoRequest;
import com.djrapitops.plan.system.info.request.RequestSetupRequest;
import com.djrapitops.plan.system.webserver.Request;
import com.djrapitops.plan.system.webserver.response.Response;
import com.djrapitops.plan.utilities.NullCheck;
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

    public ConnectionIn(Request httpRequest, InfoRequest infoRequest) throws WebException {
        Verify.nullCheck(httpRequest, infoRequest);

        Map<String, String> variables = readVariables(httpRequest);
        checkAuthentication(variables);

        this.variables = variables;
        this.infoRequest = infoRequest;
    }

    private void checkAuthentication(Map<String, String> variables) throws WebException {
        String sender = variables.get("sender");
        NullCheck.check(sender, new BadRequestException("Sender ('sender') variable not supplied in the request."));
        UUID serverUUID = UUID.fromString(sender);

        try {
            if (!Database.getActive().check().isServerInDatabase(serverUUID)) {
                if (infoRequest instanceof RequestSetupRequest) {
                    return;
                }
                throw new UnauthorizedServerException(sender + " (Sender) was not found from database");
            }
        } catch (DBException e) {
            throw new TransferDatabaseException(e);
        }
    }

    private Map<String, String> readVariables(Request request) throws WebException {
        String requestBody = readRequestBody(request.getRequestBody());
        String[] variables = requestBody.split(";&variable;");

        return Arrays.stream(variables)
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
