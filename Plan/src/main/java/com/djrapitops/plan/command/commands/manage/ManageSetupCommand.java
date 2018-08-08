package com.djrapitops.plan.command.commands.manage;

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.api.exceptions.connection.*;
import com.djrapitops.plan.system.info.InfoSystem;
import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.locale.lang.CmdHelpLang;
import com.djrapitops.plan.system.locale.lang.CommandLang;
import com.djrapitops.plan.system.locale.lang.DeepHelpLang;
import com.djrapitops.plan.system.processing.Processing;
import com.djrapitops.plan.system.settings.Permissions;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.system.webserver.WebServerSystem;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plugin.command.CommandNode;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.ISender;
import com.djrapitops.plugin.utilities.Verify;

import java.util.Arrays;

/**
 * This manage SubCommand is used to request settings from Bungee so that connection can be established.
 *
 * @author Rsl1122
 * @since 2.3.0
 */
public class ManageSetupCommand extends CommandNode {

    private final Locale locale;

    public ManageSetupCommand(PlanPlugin plugin) {
        super("setup", Permissions.MANAGE.getPermission(), CommandType.PLAYER_OR_ARGS);

        locale = plugin.getSystem().getLocaleSystem().getLocale();

        setArguments("<BungeeAddress>");
        setShortHelp(locale.getString(CmdHelpLang.MANAGE_SETUP));
        setInDepthHelp(locale.getArray(DeepHelpLang.MANAGE_SETUP));
    }

    @Override
    public void onCommand(ISender sender, String commandLabel, String[] args) {
        Verify.isTrue(args.length >= 1,
                () -> new IllegalArgumentException(locale.getString(CommandLang.FAIL_REQ_ONE_ARG, Arrays.toString(this.getArguments()))));

        if (!WebServerSystem.isWebServerEnabled()) {
            sender.sendMessage(locale.getString(CommandLang.CONNECT_WEBSERVER_NOT_ENABLED));
            return;
        }
        String address = args[0].toLowerCase();
        if (!address.startsWith("http") || address.endsWith("://")) {
            sender.sendMessage(locale.getString(CommandLang.CONNECT_URL_MISTAKE));
            return;
        }
        if (address.endsWith("/")) {
            address = address.substring(0, address.length() - 1);
        }

        requestSetup(sender, address);
    }

    private void requestSetup(ISender sender, String address) {
        Processing.submitNonCritical(() -> {
            try {
                Settings.BUNGEE_OVERRIDE_STANDALONE_MODE.set(false);
                Settings.BUNGEE_COPY_CONFIG.set(true);

                InfoSystem.getInstance().requestSetUp(address);

                sender.sendMessage(locale.getString(CommandLang.CONNECT_SUCCESS));
            } catch (ForbiddenException e) {
                sender.sendMessage(locale.getString(CommandLang.CONNECT_FORBIDDEN));
            } catch (BadRequestException e) {
                sender.sendMessage(locale.getString(CommandLang.CONNECT_BAD_REQUEST));
            } catch (UnauthorizedServerException e) {
                sender.sendMessage(locale.getString(CommandLang.CONNECT_UNAUTHORIZED));
            } catch (ConnectionFailException e) {
                sender.sendMessage(locale.getString(CommandLang.CONNECT_FAIL, e.getMessage()));
            } catch (InternalErrorException e) {
                sender.sendMessage(locale.getString(CommandLang.CONNECT_INTERNAL_ERROR, e.getMessage()));
            } catch (GatewayException e) {
                sender.sendMessage(locale.getString(CommandLang.CONNECT_GATEWAY));
            } catch (WebException e) {
                Log.toLog(this.getClass(), e);
                sender.sendMessage(locale.getString(CommandLang.CONNECT_FAIL, e.toString()));
            }
        });
    }
}
