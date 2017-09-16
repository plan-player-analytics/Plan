/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.systems.webserver.webapi;

import com.djrapitops.plugin.utilities.Verify;
import main.java.com.djrapitops.plan.api.IPlan;
import main.java.com.djrapitops.plan.api.exceptions.WebAPIConnectionFailException;
import main.java.com.djrapitops.plan.api.exceptions.WebAPIException;
import main.java.com.djrapitops.plan.api.exceptions.WebAPIForbiddenException;
import main.java.com.djrapitops.plan.api.exceptions.WebAPINotFoundException;
import main.java.com.djrapitops.plan.systems.webserver.response.Response;
import main.java.com.djrapitops.plan.utilities.MiscUtils;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Rsl1122
 */
public abstract class WebAPI {

    private Map<String, String> variables;

    public WebAPI() {
        this.variables = new HashMap<>();
    }

    public abstract Response onResponse(IPlan plugin, Map<String, String> variables);

    public void sendRequest(String address) throws WebAPIException {
        Verify.nullCheck(address);

        try {
            URL url = new URL(address + "/api/" + this.getClass().getSimpleName().toLowerCase());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(10000);
            connection.setDoOutput(true);
            connection.setInstanceFollowRedirects(false);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("charset", "ISO-8859-1");

            StringBuilder parameters = new StringBuilder();
            String serverUUID = MiscUtils.getIPlan().getServerInfoManager().getServerUUID().toString();
            parameters.append("sender=").append(serverUUID).append("&");
            for (Map.Entry<String, String> entry : variables.entrySet()) {
                parameters.append("&").append(entry.getKey()).append(entry.getValue());
            }
            byte[] toSend = parameters.toString().getBytes();
            int length = toSend.length;

            connection.setRequestProperty("Content-Length", Integer.toString(length));

            connection.setUseCaches(false);
            try (DataOutputStream out = new DataOutputStream(connection.getOutputStream())) {
                out.write(toSend);
            }

            int responseCode = connection.getResponseCode();
            switch (responseCode) {
                case 200:
                    return;
                case 400:
                    throw new WebAPIException("Bad Request: " + url.toString() + "|" + parameters);
                case 403:
                    throw new WebAPIForbiddenException(url.toString());
                case 404:
                    throw new WebAPINotFoundException();
                default:
                    throw new WebAPIException(url.toString() + "| Wrong response code " + responseCode);
            }
        } catch (SocketTimeoutException e) {
            throw new WebAPIConnectionFailException("Connection timed out after 10 seconds.", e);
        } catch (IOException e) {
            throw new WebAPIConnectionFailException("API connection failed. address: " + address, e);
        }
    }

    protected void addVariable(String key, String value) {
        variables.put(key, value);
    }

    public Map<String, String> getVariables() {
        return variables;
    }
}