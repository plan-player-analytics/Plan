/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.system.webserver.webapi;

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.api.exceptions.connection.*;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.system.webserver.response.Response;
import com.djrapitops.plan.system.webserver.response.api.BadRequestResponse;
import com.djrapitops.plan.system.webserver.response.api.SuccessResponse;
import com.djrapitops.plan.system.webserver.response.cache.PageId;
import com.djrapitops.plan.system.webserver.response.cache.ResponseCache;
import com.djrapitops.plan.system.webserver.response.errors.NotFoundResponse;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plugin.utilities.Verify;

import javax.net.ssl.*;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author Rsl1122
 */
public abstract class WebAPI {

    private static TrustManager[] trustAllCerts = new TrustManager[]{
            new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                    //No need to implement.
                }

                public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                    //No need to implement.
                }
            }
    };
    private Map<String, String> variables;


    public WebAPI() {
        this.variables = new HashMap<>();
    }

    public static Map<String, String> readVariables(String requestBody) {
        String[] variables = requestBody.split(";&variable;");

        return Arrays.stream(variables)
                .map(variable -> variable.split("=", 2))
                .filter(splitVariables -> splitVariables.length == 2)
                .collect(Collectors.toMap(splitVariables -> splitVariables[0], splitVariables -> splitVariables[1], (a, b) -> b));
    }

    public Response processRequest(PlanPlugin plugin, Map<String, String> variables) {
        String sender = variables.get("sender");
        if (sender == null) {
            Log.debug(getClass().getSimpleName() + ": Sender not Found");
            return badRequest("Sender not present");
        } else {
            try {
                UUID.fromString(sender);
            } catch (Exception e) {
                Log.debug(getClass().getSimpleName() + ": Invalid Sender UUID");
                return badRequest("Faulty Sender value");
            }
        }
        return onRequest(plugin, variables);
    }

    public abstract Response onRequest(PlanPlugin plugin, Map<String, String> variables);

    public void sendRequest(String address) throws WebException {
        Verify.nullCheck(address);

        try {
            URL url = new URL(address + "/api/" + this.getClass().getSimpleName().toLowerCase());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            if (address.startsWith("https")) {
                HttpsURLConnection httpsConn = (HttpsURLConnection) connection;

                // Disables unsigned certificate & hostname check, because we're trusting the user given certificate.

                // This allows https connections internally to local ports.
                httpsConn.setHostnameVerifier((hostname, session) -> true);

                // This allows connecting to connections with invalid certificate
                // Drawback: MitM attack possible between connections to servers that are not local.
                // Scope: WebAPI transmissions
                // Risk: Attacker sets up a server between Bungee and Bukkit WebServers
                //       - Negotiates SSL Handshake with both servers
                //       - Receives the SSL encrypted data, but decrypts it in the MitM server.
                //       -> Access to valid ServerUUID for POST requests
                //       -> Access to sending Html to the (Bungee) WebServer
                // Mitigating factors:
                // - If Server owner has access to all routing done on the domain (IP/Address)
                // - If Direct IPs are used to transfer between servers
                // Alternative solution: WebAPI run only on HTTP, HTTP can be read during transmission,
                // would require running two WebServers when HTTPS is used.
                httpsConn.setSSLSocketFactory(getRelaxedSocketFactory());
            }
            connection.setConnectTimeout(10000);
            connection.setDoOutput(true);
            connection.setInstanceFollowRedirects(false);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            connection.setRequestProperty("charset", "UTF-8");

            String parameters = parseVariables();

            connection.setRequestProperty("Content-Length", Integer.toString(parameters.length()));

            byte[] toSend = parameters.getBytes();

            connection.setUseCaches(false);
            Log.debug("Sending WebAPI Request: " + this.getClass().getSimpleName() + " to " + address);
            try (DataOutputStream out = new DataOutputStream(connection.getOutputStream())) {
                out.write(toSend);
            }

            int responseCode = connection.getResponseCode();
            Log.debug("Response: " + responseCode);
            switch (responseCode) {
                case 200:
                    return;
                case 400:
                    throw new WebFailException("Bad Request: " + url.toString() + "|" + parameters);
                case 403:
                    throw new ForbiddenException(url.toString());
                case 404:
                    throw new NotFoundException();
                case 500:
                    throw new InternalErrorException();
                default:
                    throw new WebException(url.toString() + "| Wrong response code " + responseCode);
            }
        } catch (SocketTimeoutException e) {
            throw new ConnectionFailException("Connection timed out after 10 seconds.", e);
        } catch (NoSuchAlgorithmException | KeyManagementException | IOException e) {
            if (Settings.DEV_MODE.isTrue()) {
                Log.toLog(this.getClass().getName(), e);
            }
            throw new ConnectionFailException("API connection failed. address: " + address, e);
        }
    }

    protected void addVariable(String key, String value) {
        variables.put(key, value);
    }

    public Map<String, String> getVariables() {
        return variables;
    }

    private SSLSocketFactory getRelaxedSocketFactory() throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        return sc.getSocketFactory();
    }

    protected Response success() {
        return ResponseCache.loadResponse(PageId.TRUE.id(), SuccessResponse::new);
    }

    protected Response fail(String reason) {
        return ResponseCache.loadResponse(PageId.FALSE.id(), () -> {
            NotFoundResponse notFoundResponse = new NotFoundResponse("");
            notFoundResponse.setContent(reason);
            return notFoundResponse;
        });
    }

    protected Response badRequest(String error) {
        return ResponseCache.loadResponse(PageId.ERROR.of(error), () -> new BadRequestResponse(error));
    }

    private String parseVariables() {
        StringBuilder parameters = new StringBuilder();
        String serverUUID = PlanPlugin.getInstance().getServerUuid().toString();
        parameters.append("sender=").append(serverUUID);
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            parameters.append(";&variable;").append(entry.getKey()).append("=").append(entry.getValue());
        }
        return parameters.toString();
    }
}