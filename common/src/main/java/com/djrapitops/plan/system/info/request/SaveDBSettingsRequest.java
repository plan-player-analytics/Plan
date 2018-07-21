/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.info.request;

import com.djrapitops.plan.PlanHelper;
import com.djrapitops.plan.api.exceptions.connection.BadRequestException;
import com.djrapitops.plan.api.exceptions.connection.WebException;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.system.webserver.response.DefaultResponses;
import com.djrapitops.plan.system.webserver.response.Response;
import com.djrapitops.plan.system.webserver.response.api.BadRequestResponse;
import com.djrapitops.plugin.api.Check;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plugin.utilities.Verify;

import java.util.Map;

/**
 * InfoRequest for sending Database config settings to Bukkit servers.
 *
 * @author Rsl1122
 */
public class SaveDBSettingsRequest extends InfoRequestWithVariables implements SetupRequest {

    public SaveDBSettingsRequest() {
        variables.put("DB_TYPE", "mysql"); // Settings.DB_TYPE
        variables.put("DB_HOST", Settings.DB_HOST.toString());
        variables.put("DB_USER", Settings.DB_USER.toString());
        variables.put("DB_PASS", Settings.DB_PASS.toString());
        variables.put("DB_DATABASE", Settings.DB_DATABASE.toString());
        variables.put("DB_PORT", Settings.DB_PORT.toString());
    }

    /**
     * Private constructor for creating a handler.
     */
    private SaveDBSettingsRequest(boolean b) {
    }

    public static SaveDBSettingsRequest createHandler() {
        return new SaveDBSettingsRequest(true);
    }

    @Override
    public void runLocally() {
        /* Won't be run */
    }

    @Override
    public Response handleRequest(Map<String, String> variables) throws WebException {
        if (Check.isBungeeAvailable()) {
            return new BadRequestResponse("Not supposed to be called on a Bungee server");
        }
        if (Settings.BUNGEE_COPY_CONFIG.isFalse() || Settings.BUNGEE_OVERRIDE_STANDALONE_MODE.isTrue()) {
            return new BadRequestResponse("Bungee config settings overridden on this server.");
        }

        try {
            setSettings(variables);
            Log.info("----------------------------------");
            Log.info("The Received Bungee Database Settings, restarting Plan..");
            Log.info("----------------------------------");
            return DefaultResponses.SUCCESS.get();
        } finally {
            PlanHelper.getInstance().reloadPlugin(true);
        }
    }

    private void setSettings(Map<String, String> variables) throws BadRequestException {
        String type = variables.get("DB_TYPE");
        String host = variables.get("DB_HOST");
        String user = variables.get("DB_USER");
        String pass = variables.get("DB_PASS");
        String database = variables.get("DB_DATABASE");
        String portS = variables.get("DB_PORT");

        Verify.nullCheck(type, () -> new BadRequestException("DB_TYPE not specified in the request."));
        Verify.nullCheck(host, () -> new BadRequestException("DB_HOST not specified in the request."));
        Verify.nullCheck(user, () -> new BadRequestException("DB_USER not specified in the request."));
        Verify.nullCheck(pass, () -> new BadRequestException("DB_PASS not specified in the request."));
        Verify.nullCheck(database, () -> new BadRequestException("DB_DATABASE not specified in the request."));
        Verify.nullCheck(portS, () -> new BadRequestException("DB_PORT not specified in the request."));

        try {
            Settings.DB_PORT.set(Integer.valueOf(portS));
        } catch (NumberFormatException e) {
            throw new BadRequestException("DB_PORT was not a number.");
        }
        Settings.DB_TYPE.set(type);
        Settings.DB_HOST.set(host);
        Settings.DB_USER.set(user);
        Settings.DB_PASS.set(pass);
        Settings.DB_DATABASE.set(database);
        Settings.save();
    }
}
