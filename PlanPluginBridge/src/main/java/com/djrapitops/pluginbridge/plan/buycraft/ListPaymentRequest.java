/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.pluginbridge.plan.buycraft;

import com.djrapitops.plan.api.exceptions.connection.ForbiddenException;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Request to Buycraft API for payment listings.
 *
 * @author Rsl1122
 */
public class ListPaymentRequest {

    private final String secret;

    public ListPaymentRequest(String secret) {
        this.secret = secret;
    }

    public List<Payment> makeRequest() throws IOException, ForbiddenException {
        URL url = new URL("https://plugin.buycraft.net/payments");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("GET");
        connection.setRequestProperty("X-BuyCraft-Secret", secret);
        connection.getOutputStream().write(0);

        JsonElement json;
        try {
            InputStreamReader reader = new InputStreamReader(connection.getInputStream());
            json = new JsonParser().parse(reader);
        } finally {
            connection.disconnect();
        }

        if (json == null || json.isJsonNull()) {
            throw new NullPointerException("JSON should not be null");
        }

        List<Payment> payments = new ArrayList<>();
        if (json.isJsonObject()) {
            return readError(json);
        } else if (json.isJsonArray()) {
            readAndAddPayments(json, payments);
        }
        return payments;
    }

    private void readAndAddPayments(JsonElement json, List<Payment> payments) {
        JsonArray jsonArray = json.getAsJsonArray();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

        for (JsonElement element : jsonArray) {
            JsonObject payment = element.getAsJsonObject();
            double amount = payment.get("amount").getAsDouble();
            String dateString = payment.get("date").getAsString();
            long date = dateFormat.parse(dateString, new ParsePosition(0)).getTime();
            String currency = payment.get("currency").getAsJsonObject().get("symbol").getAsString();
            JsonObject player = payment.get("player").getAsJsonObject();
            String playerName = player.get("name").getAsString();
            UUID uuid = UUID.fromString(player.get("uuid").getAsString());

            payments.add(new Payment(amount, currency, uuid, playerName, date));
        }
    }

    private List<Payment> readError(JsonElement json) throws ForbiddenException {
        JsonObject jsonObject = json.getAsJsonObject();
        int errorCode = jsonObject.get("error_code").getAsInt();
        String errorMessage = jsonObject.get("error_message").getAsString();

        if (errorCode == 403) {
            throw new ForbiddenException("Incorrect Server Secret. Check config.");
        } else {
            throw new IllegalStateException(errorCode + ": " + errorMessage);
        }
    }
}