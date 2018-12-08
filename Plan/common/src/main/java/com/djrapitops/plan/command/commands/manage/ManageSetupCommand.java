/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.command.commands.manage;

import com.djrapitops.plan.api.exceptions.connection.*;
import com.djrapitops.plan.system.info.InfoSystem;
import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.locale.lang.CmdHelpLang;
import com.djrapitops.plan.system.locale.lang.CommandLang;
import com.djrapitops.plan.system.locale.lang.DeepHelpLang;
import com.djrapitops.plan.system.processing.Processing;
import com.djrapitops.plan.system.settings.Permissions;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.system.settings.paths.PluginSettings;
import com.djrapitops.plan.system.webserver.WebServer;
import com.djrapitops.plugin.command.CommandNode;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.Sender;
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.logging.error.ErrorHandler;
import com.djrapitops.plugin.utilities.Verify;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;

/**
 * This manage SubCommand is used to request settings from Bungee so that connection can be established.
 *
 * @author Rsl1122
 * @since 2.3.0
 */
@Singleton
public class ManageSetupCommand extends CommandNode {

    private final Locale locale;
    private final PlanConfig config;
    private final Processing processing;
    private final InfoSystem infoSystem;
    private final WebServer webServer;
    private final ErrorHandler errorHandler;

    @Inject
    public ManageSetupCommand(
            Locale locale,
            PlanConfig config,
            Processing processing,
            InfoSystem infoSystem,
            WebServer webServer,
            ErrorHandler errorHandler
    ) {
        super("setup", Permissions.MANAGE.getPermission(), CommandType.PLAYER_OR_ARGS);

        this.locale = locale;
        this.config = config;
        this.processing = processing;
        this.infoSystem = infoSystem;
        this.webServer = webServer;
        this.errorHandler = errorHandler;

        setArguments("<BungeeAddress>");
        setShortHelp(locale.getString(CmdHelpLang.MANAGE_SETUP));
        setInDepthHelp(locale.getArray(DeepHelpLang.MANAGE_SETUP));
    }

    @Override
    public void onCommand(Sender sender, String commandLabel, String[] args) {
        Verify.isTrue(args.length >= 1,
                () -> new IllegalArgumentException(locale.getString(CommandLang.FAIL_REQ_ONE_ARG, Arrays.toString(this.getArguments()))));

        if (!webServer.isEnabled()) {
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

    private void requestSetup(Sender sender, String address) {
        processing.submitNonCritical(() -> {
            try {
                config.set(PluginSettings.BUNGEE_COPY_CONFIG, true);

                infoSystem.requestSetUp(address);

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
                errorHandler.log(L.WARN, this.getClass(), e);
                sender.sendMessage(locale.getString(CommandLang.CONNECT_FAIL, e.toString()));
            }
        });
    }
}
