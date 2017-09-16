/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.command.commands;

import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.ISender;
import com.djrapitops.plugin.command.SubCommand;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.api.exceptions.WebAPIException;
import main.java.com.djrapitops.plan.locale.Locale;
import main.java.com.djrapitops.plan.locale.Msg;
import main.java.com.djrapitops.plan.systems.webserver.webapi.WebAPI;
import main.java.com.djrapitops.plan.systems.webserver.webapi.bukkit.InspectWebAPI;
import main.java.com.djrapitops.plan.utilities.Check;

import java.util.Optional;
import java.util.UUID;

/**
 * //TODO Class Javadoc Comment
 *
 * @author Rsl1122
 */
public class DevCommand extends SubCommand {

    private final Plan plugin;

    public DevCommand(Plan plugin) {
        super("dev", CommandType.CONSOLE_WITH_ARGUMENTS, "plan.*", "Test Plugin functions not testable with unit tests.", "<feature to test>");
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(ISender sender, String cmd, String[] args) {
        if (!Check.isTrue(args.length >= 1, Locale.get(Msg.CMD_FAIL_REQ_ONE_ARG).toString(), sender)) {
            return true;
        }
        String feature = args[0];
        switch (feature) {
            case "webapi":
                if (!Check.isTrue(args.length >= 2, Locale.get(Msg.CMD_FAIL_REQ_ONE_ARG).toString(), sender)) {
                    break;
                }
                if (!webapi(args[1] + "webapi")) {
                    sender.sendMessage("[Plan] No such API / Exception occurred.");
                }
            case "web":
                Optional<String> bungeeConnectionAddress = plugin.getServerInfoManager().getBungeeConnectionAddress();
                String accessAddress = plugin.getWebServer().getAccessAddress();
                sender.sendMessage((plugin.getInfoManager().isUsingBungeeWebServer() && bungeeConnectionAddress.isPresent())
                        ? "Bungee: " + bungeeConnectionAddress.get() : "Local: " + accessAddress);
                break;
            default:
                break;
        }
        return true;
    }

    private boolean webapi(String method) {
        WebAPI api = plugin.getWebServer().getWebAPI().getAPI(method);
        if (api == null) {
            return false;
        }
        try {
            if (api instanceof InspectWebAPI) {
                ((InspectWebAPI) api).sendRequest(plugin.getWebServer().getAccessAddress(), UUID.randomUUID());
            } else {
                api.sendRequest(plugin.getWebServer().getAccessAddress());
            }
            return true;
        } catch (WebAPIException e) {
            e.printStackTrace();
        }
        return false;
    }
}