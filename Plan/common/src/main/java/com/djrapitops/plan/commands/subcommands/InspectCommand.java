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
package com.djrapitops.plan.commands.subcommands;

import com.djrapitops.plan.delivery.rendering.html.Html;
import com.djrapitops.plan.delivery.webserver.Addresses;
import com.djrapitops.plan.delivery.webserver.WebServer;
import com.djrapitops.plan.exceptions.database.DBOpException;
import com.djrapitops.plan.identification.UUIDUtility;
import com.djrapitops.plan.processing.Processing;
import com.djrapitops.plan.settings.Permissions;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.locale.lang.CmdHelpLang;
import com.djrapitops.plan.settings.locale.lang.CommandLang;
import com.djrapitops.plan.settings.locale.lang.DeepHelpLang;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.queries.PlayerFetchQueries;
import com.djrapitops.plan.storage.database.queries.objects.WebUserQueries;
import com.djrapitops.plan.utilities.MiscUtils;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import com.djrapitops.plugin.command.CommandNode;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.CommandUtils;
import com.djrapitops.plugin.command.Sender;
import com.djrapitops.plugin.logging.L;

import javax.inject.Inject;
import java.util.UUID;

/**
 * This command is used to refresh Inspect page and display link.
 *
 * @author Rsl1122
 */
public class InspectCommand extends CommandNode {

    private final Locale locale;
    private final DBSystem dbSystem;
    private final WebServer webServer;
    private final Addresses addresses;
    private final Processing processing;
    private final UUIDUtility uuidUtility;
    private final ErrorLogger errorLogger;

    @Inject
    public InspectCommand(
            Locale locale,
            Addresses addresses,
            Processing processing,
            DBSystem dbSystem,
            WebServer webServer,
            UUIDUtility uuidUtility,
            ErrorLogger errorLogger
    ) {
        super("inspect", Permissions.INSPECT.getPermission(), CommandType.PLAYER_OR_ARGS);
        this.addresses = addresses;
        this.processing = processing;
        setArguments("<player>");

        this.locale = locale;
        this.dbSystem = dbSystem;
        this.webServer = webServer;
        this.uuidUtility = uuidUtility;
        this.errorLogger = errorLogger;

        setShortHelp(locale.getString(CmdHelpLang.INSPECT));
        setInDepthHelp(locale.getArray(DeepHelpLang.INSPECT));
    }

    @Override
    public void onCommand(Sender sender, String commandLabel, String[] args) {
        String playerName = MiscUtils.getPlayerName(args, sender);

        if (playerName == null) {
            sender.sendMessage(locale.getString(CommandLang.FAIL_NO_PERMISSION));
            return;
        }

        Database.State dbState = dbSystem.getDatabase().getState();
        if (dbState != Database.State.OPEN) {
            sender.sendMessage(locale.getString(CommandLang.FAIL_DATABASE_NOT_OPEN, dbState.name()));
            return;
        }

        runInspectTask(playerName, sender);
    }

    private void runInspectTask(String playerName, Sender sender) {
        processing.submitNonCritical(() -> {
            try {
                UUID playerUUID = uuidUtility.getUUIDOf(playerName);
                if (playerUUID == null) {
                    sender.sendMessage(locale.getString(CommandLang.FAIL_USERNAME_NOT_VALID));
                    return;
                }

                if (!dbSystem.getDatabase().query(PlayerFetchQueries.isPlayerRegistered(playerUUID))) {
                    sender.sendMessage(locale.getString(CommandLang.FAIL_USERNAME_NOT_KNOWN));
                    return;
                }

                checkWebUserAndNotify(sender);
                this.sendInspectMsg(sender, playerName);
            } catch (DBOpException e) {
                sender.sendMessage("§eDatabase exception occurred: " + e.getMessage());
                errorLogger.log(L.ERROR, this.getClass(), e);
            }
        });
    }

    private void checkWebUserAndNotify(Sender sender) {
        if (CommandUtils.isPlayer(sender) && webServer.isAuthRequired()) {
            boolean senderHasWebUser = dbSystem.getDatabase().query(WebUserQueries.fetchUserLinkedTo(sender.getName())).isPresent();

            if (!senderHasWebUser) {
                sender.sendMessage("§e" + locale.getString(CommandLang.NO_WEB_USER_NOTIFY));
            }
        }
    }

    private void sendInspectMsg(Sender sender, String playerName) {
        sender.sendMessage(locale.getString(CommandLang.HEADER_INSPECT, playerName));

        String address = addresses.getMainAddress().orElseGet(() -> {
            sender.sendMessage(locale.getString(CommandLang.NO_ADDRESS_NOTIFY));
            return addresses.getFallbackLocalhostAddress();
        });
        String url = address + "/player/" + Html.encodeToURL(playerName);
        String linkPrefix = locale.getString(CommandLang.LINK_PREFIX);

        boolean console = !CommandUtils.isPlayer(sender);
        if (console) {
            sender.sendMessage(linkPrefix + url);
        } else {
            sender.sendMessage(linkPrefix);
            sender.sendLink("   ", locale.getString(CommandLang.LINK_CLICK_ME), url);
        }

        sender.sendMessage(">");
    }
}