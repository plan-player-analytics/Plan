/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.info.connection;

import com.djrapitops.plan.api.exceptions.connection.*;
import com.djrapitops.plan.system.info.request.InfoRequest;
import com.djrapitops.plan.system.info.request.InfoRequestWithVariables;
import com.djrapitops.plan.system.info.server.Server;
import com.djrapitops.plan.system.settings.Settings;
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
import java.util.Map;
import java.util.UUID;

/**
 * Represents an outbound action request to another Plan server.
 *
 * @author Rsl1122
 */
public class ConnectionOut {

    private static final TrustManager[] trustAllCerts = new TrustManager[]{
            new X509TrustManager() {
                @Override
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                @Override
                public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                    //No need to implement.
                }

                @Override
                public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                    //No need to implement.
                }
            }
    };

    private final Server toServer;
    private final UUID serverUUID;
    private final InfoRequest infoRequest;

    /**
     * Constructor.
     *
     * @param toServer    Full address to another Plan webserver. (http://something:port)
     * @param serverUUID  UUID of server this outbound connection.
     * @param infoRequest Type of the action this connection wants to be performed.
     */
    public ConnectionOut(Server toServer, UUID serverUUID, InfoRequest infoRequest) {
        Verify.nullCheck(toServer, serverUUID, infoRequest);
        this.toServer = toServer;
        this.serverUUID = serverUUID;
        this.infoRequest = infoRequest;
    }

    public void sendRequest() throws WebException {
        String address = toServer.getWebAddress();

        HttpURLConnection connection = null;
        try {
            URL url = new URL(address + "/info/" + infoRequest.getClass().getSimpleName().toLowerCase());
            connection = (HttpURLConnection) url.openConnection();
            if (address.startsWith("https")) {
                HttpsURLConnection httpsConn = (HttpsURLConnection) connection;

                // Disables unsigned certificate & hostname check, because we're trusting the user given certificate.

                // This allows https connections internally to local ports.
                httpsConn.setHostnameVerifier((hostname, session) -> true);

                // This allows connecting to connections with invalid certificate
                // Drawback: MitM attack possible between connections to servers that are not local.
                // Scope: InfoRequest transmissions
                // Risk: Attacker sets up a server between Bungee and Bukkit WebServers
                //       - Negotiates SSL Handshake with both servers
                //       - Receives the SSL encrypted data, but decrypts it in the MitM server.
                //       -> Access to valid ServerUUID for POST requests
                //       -> Access to sending Html to the (Bungee) WebServer
                // Mitigating factors:
                // - If Server owner has access to all routing done on the domain (IP/Address)
                // - If Direct IPs are used to transfer between servers
                // Alternative solution: InfoRequests run only on HTTP, HTTP can be read during transmission,
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
            try (DataOutputStream out = new DataOutputStream(connection.getOutputStream())) {
                out.write(toSend);
            }

            int responseCode = connection.getResponseCode();

            ConnectionLog.logConnectionTo(toServer, infoRequest, responseCode);
            switch (responseCode) {
                case 200:
                    return;
                case 400:
                    throw new BadRequestException("Bad Request: " + url.toString() + " | " + parameters);
                case 403:
                    throw new ForbiddenException(url.toString() + " returned 403 | " + parameters);
                case 404:
                    throw new NotFoundException(url.toString() + " returned a 404, ensure that your server is connected to an up to date Plan server.");
                case 412:
                    throw new UnauthorizedServerException(url.toString() + " reported that it does not recognize this server. Make sure '/plan m setup' was successful.");
                case 500:
                    throw new InternalErrorException();
                case 504:
                    throw new GatewayException(url.toString() + " reported that it failed to connect to this server.");
                default:
                    throw new WebException(url.toString() + "| Wrong response code " + responseCode);
            }
        } catch (SocketTimeoutException e) {
            ConnectionLog.logConnectionTo(toServer, infoRequest, 0);
            throw new ConnectionFailException("Connection timed out after 10 seconds.", e);
        } catch (NoSuchAlgorithmException | KeyManagementException | IOException e) {
            if (Settings.DEV_MODE.isTrue()) {
                Log.warn("THIS ERROR IS ONLY LOGGED IN DEV MODE:");
                Log.toLog(this.getClass(), e);
            }
            ConnectionLog.logConnectionTo(toServer, infoRequest, -1);
            throw new ConnectionFailException("Connection failed to address: " + address + " - Make sure the server is online.", e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private String parseVariables() {
        StringBuilder parameters = new StringBuilder("sender=" + serverUUID + ";&variable;" +
                "type=" + infoRequest.getClass().getSimpleName());

        if (infoRequest instanceof InfoRequestWithVariables) {
            Map<String, String> variables = ((InfoRequestWithVariables) infoRequest).getVariables();
            for (Map.Entry<String, String> entry : variables.entrySet()) {
                parameters.append(";&variable;").append(entry.getKey()).append("=").append(entry.getValue());
            }
        }

        return parameters.toString();
    }

    private SSLSocketFactory getRelaxedSocketFactory() throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext sc = SSLContext.getInstance("TLSv1.2");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        return sc.getSocketFactory();
    }
}
