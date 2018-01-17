package com.djrapitops.plan.system.info.connection;

import com.djrapitops.plan.api.exceptions.connection.WebException;
import com.djrapitops.plan.system.info.request.InfoRequest;
import com.djrapitops.plan.system.webserver.response.Response;

import java.util.Map;

public class ConnectionIn {
    
    private final Map<String, String> variables;
    private final InfoRequest infoRequest;

    public ConnectionIn(Map<String, String> variables, InfoRequest infoRequest) {
        this.variables = variables;
        this.infoRequest = infoRequest;
    }

    public Response handleRequest() throws WebException {
        return infoRequest.handleRequest(variables);
    }
}
