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
package com.djrapitops.plan.commands.subcommands.manage;

import com.djrapitops.plan.delivery.rendering.html.Html;
import com.djrapitops.plan.delivery.webserver.Addresses;
import com.djrapitops.plan.processing.Processing;
import com.djrapitops.plan.settings.Permissions;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.locale.lang.CmdHelpLang;
import com.djrapitops.plan.settings.locale.lang.CommandLang;
import com.djrapitops.plan.settings.locale.lang.DeepHelpLang;
import com.djrapitops.plan.utilities.MiscUtils;
import com.djrapitops.plugin.command.CommandNode;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.CommandUtils;
import com.djrapitops.plugin.command.Sender;
import com.djrapitops.plugin.utilities.Verify;

import javax.inject.Inject;
import java.util.Arrays;

/**
 * This manage subcommand is used to remove a single player's data from the
 * database.
 *
 * @author Rsl1122
 */
public class ManageRawDataCommand extends CommandNode {

    private final Locale locale;
    private final Addresses addresses;
    private final Processing processing;

    @Inject
    public ManageRawDataCommand(
            Locale locale,
            Addresses addresses,
            Processing processing
    ) {
        super("raw", Permissions.MANAGE.getPermission(), CommandType.PLAYER_OR_ARGS);

        this.locale = locale;
        this.addresses = addresses;
        this.processing = processing;

        setArguments("<player>");
        setShortHelp(locale.getString(CmdHelpLang.MANAGE_RAW_DATA));
        setInDepthHelp(locale.getArray(DeepHelpLang.MANAGE_RAW_DATA));
    }

    @Override
    public void onCommand(Sender sender, String commandLabel, String[] args) {
        Verify.isTrue(args.length >= 1,
                () -> new IllegalArgumentException(locale.getString(CommandLang.FAIL_REQ_ONE_ARG, Arrays.toString(this.getArguments()))));

        processing.submitNonCritical(() -> {
            String playerName = MiscUtils.getPlayerName(args, sender, Permissions.MANAGE);

            sender.sendMessage(locale.getString(CommandLang.HEADER_INSPECT, playerName));
            // Link
            String address = addresses.getMainAddress().orElseGet(() -> {
                sender.sendMessage(locale.getString(CommandLang.NO_ADDRESS_NOTIFY));
                return addresses.getFallbackLocalhostAddress();
            });
            String url = address + "/player/" + Html.encodeToURL(playerName) + "/raw";
            String linkPrefix = locale.getString(CommandLang.LINK_PREFIX);
            boolean console = !CommandUtils.isPlayer(sender);
            if (console) {
                sender.sendMessage(linkPrefix + url);
            } else {
                sender.sendMessage(linkPrefix);
                sender.sendLink("   ", locale.getString(CommandLang.LINK_CLICK_ME), url);
            }

            sender.sendMessage(">");
        });
    }
}
