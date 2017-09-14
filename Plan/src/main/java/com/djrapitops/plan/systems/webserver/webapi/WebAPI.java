/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.systems.webserver.webapi;

import com.djrapitops.plugin.utilities.Verify;
import main.java.com.djrapitops.plan.api.IPlan;
import main.java.com.djrapitops.plan.api.exceptions.WebAPIConnectionFailException;
import main.java.com.djrapitops.plan.api.exceptions.WebAPIException;
import main.java.com.djrapitops.plan.systems.webserver.response.Response;
import main.java.com.djrapitops.plan.utilities.MiscUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * @author Rsl1122
 */
public abstract class WebAPI {

    private Map<String, String> variables;

    public abstract Response onResponse(IPlan plugin, Map<String, String> variables);

    public String sendRequest(String address, UUID receiverUUID) throws WebAPIException {
        Verify.nullCheck(address, receiverUUID);

        HttpClient httpClient = HttpClients.createDefault();

        HttpPost postRequest = new HttpPost(address + "/api/" + this.getClass().getSimpleName().toLowerCase());

        List<NameValuePair> parameters = new ArrayList<>();
        String serverUUID = MiscUtils.getIPlan().getServerInfoManager().getServerUUID().toString();
        parameters.add(new BasicNameValuePair("sender", serverUUID));
        parameters.add(new BasicNameValuePair("key", receiverUUID.toString()));
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            parameters.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        }

        try {
            postRequest.setEntity(new UrlEncodedFormEntity(parameters, "ISO-8859-1"));
        } catch (UnsupportedEncodingException e) {
            throw new WebAPIException("Unsupported parameter encoding", e);
        }

        try {
            HttpResponse response = httpClient.execute(postRequest);
            HttpEntity responseEntity = response.getEntity();

            if (responseEntity != null) {
                StringBuilder content = new StringBuilder();
                try (InputStream inputStream = responseEntity.getContent()) {
                    Scanner scanner = new Scanner(inputStream);
                    while (scanner.hasNextLine()) {
                        content.append(scanner.nextLine());
                    }
                }
                return content.toString();
            }
        } catch (IOException e) {
            throw new WebAPIConnectionFailException("API connection failed.", e);
        }
        throw new WebAPIException("Response entity was null");
    }

    protected void addVariable(String key, String value) {
        variables.put(key, value);
    }

    public Map<String, String> getVariables() {
        return variables;
    }
}