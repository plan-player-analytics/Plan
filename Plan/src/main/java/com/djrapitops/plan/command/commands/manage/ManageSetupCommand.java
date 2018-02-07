package com.djrapitops.plan.command.commands.manage;

import com.djrapitops.plan.api.exceptions.connection.BadRequestException;
import com.djrapitops.plan.api.exceptions.connection.ForbiddenException;
import com.djrapitops.plan.api.exceptions.connection.UnauthorizedServerException;
import com.djrapitops.plan.api.exceptions.connection.WebException;
import com.djrapitops.plan.system.info.InfoSystem;
import com.djrapitops.plan.system.settings.Permissions;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.system.settings.locale.Locale;
import com.djrapitops.plan.system.settings.locale.Msg;
import com.djrapitops.plan.system.webserver.WebServerSystem;
import com.djrapitops.plan.utilities.Condition;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.ISender;
import com.djrapitops.plugin.command.SubCommand;

/**
 * This manage SubCommand is used to request settings from Bungee so that connection can be established.
 *
 * @author Rsl1122
 * @since 2.3.0
 */
public class ManageSetupCommand extends SubCommand {

    public ManageSetupCommand() {
        super("setup",
                CommandType.PLAYER_OR_ARGS,
                Permissions.MANAGE.getPermission(),
                "Set-Up Bungee WebServer connection",
                "<Bungee WebServer address>");
    }

    @Override
    public String[] addHelp() {
        return Locale.get(Msg.CMD_HELP_MANAGE_HOTSWAP).toArray();
    }

    @Override
    public boolean onCommand(ISender sender, String commandLabel, String[] args) {
        if (!Condition.isTrue(args.length >= 1, Locale.get(Msg.CMD_FAIL_REQ_ONE_ARG).toString(), sender)) {
            return true;
        }
        if (!WebServerSystem.isWebServerEnabled()) {
            sender.sendMessage("§cWebServer is not enabled on this server! Make sure it enables on boot!");
            return true;
        }
        String address = args[0].toLowerCase();
        if (!address.startsWith("http")) {
            sender.sendMessage("§cMake sure you're using the full address (Starts with http:// or https://) - Check Bungee enable log for the full address.");
            return true;
        }
        if (address.endsWith("/")) {
            address = address.substring(0, address.length() - 1);
        }
        try {
            Settings.BUNGEE_OVERRIDE_STANDALONE_MODE.set(false);
            Settings.BUNGEE_COPY_CONFIG.set(true);

            InfoSystem.getInstance().requestSetUp(address);

            sender.sendMessage("§aConnection successful, Plan may restart in a few seconds..");
        } catch (ForbiddenException e) {
            sender.sendMessage("§eConnection succeeded, but Bungee has set-up mode disabled - use '/planbungee setup' to enable it.");
        } catch (BadRequestException e) {
            sender.sendMessage("§eConnection succeeded, but Receiving server was a Bukkit server. Use Bungee address instead.");
        } catch (UnauthorizedServerException e) {
            sender.sendMessage("§eConnection succeeded, but Receiving server didn't authorize this server. Contact Discord for support");
        } catch (WebException e) {
            Log.toLog(this.getClass(), e);
            sender.sendMessage("§cConnection to Bungee WebServer failed: More info on console");
        }
        return true;
    }
}
