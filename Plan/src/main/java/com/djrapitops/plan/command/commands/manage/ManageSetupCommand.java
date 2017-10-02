package main.java.com.djrapitops.plan.command.commands.manage;

import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.ISender;
import com.djrapitops.plugin.command.SubCommand;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Permissions;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.api.exceptions.WebAPIException;
import main.java.com.djrapitops.plan.locale.Locale;
import main.java.com.djrapitops.plan.locale.Msg;
import main.java.com.djrapitops.plan.systems.webserver.webapi.bungee.RequestSetupWebAPI;
import main.java.com.djrapitops.plan.systems.webserver.webapi.universal.PingWebAPI;
import main.java.com.djrapitops.plan.utilities.Check;

/**
 * This manage subcommand is used to swap to a different database and reload the
 * plugin if the connection to the new database can be established.
 *
 * @author Rsl1122
 * @since 2.3.0
 */
public class ManageSetupCommand extends SubCommand {

    private final Plan plugin;

    /**
     * Class Constructor.
     *
     * @param plugin Current instance of Plan
     */
    public ManageSetupCommand(Plan plugin) {
        super("setup",
                CommandType.CONSOLE_WITH_ARGUMENTS,
                Permissions.MANAGE.getPermission(),
                "Set-Up Bungee WebServer connection",
                "<Bungee WebServer address>");

        this.plugin = plugin;

    }

    @Override
    public String[] addHelp() {
        return Locale.get(Msg.CMD_HELP_MANAGE_HOTSWAP).toArray();
    }

    @Override
    public boolean onCommand(ISender sender, String commandLabel, String[] args) {
        if (!Check.isTrue(args.length >= 1, Locale.get(Msg.CMD_FAIL_REQ_ONE_ARG).toString(), sender)) {
            return true;
        }
        String address = args[0].toLowerCase();
        if (address.endsWith("/")) {
            address = address.substring(0, address.length() - 1);
        }
        try {
            plugin.getWebServer().getWebAPI().getAPI(PingWebAPI.class).sendRequest(address);
            plugin.getWebServer().getWebAPI().getAPI(RequestSetupWebAPI.class).sendRequest(address);
            sender.sendMessage("§aConnection successful, Plan may restart in a few seconds.");
        } catch (WebAPIException e) {
            Log.toLog(this.getClass().getName(), e);
            sender.sendMessage("§cConnection to Bungee WebServer failed: More info on console");
        }
        return true;
    }
}
