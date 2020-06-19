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

import com.djrapitops.plan.delivery.webserver.Addresses;
import com.djrapitops.plan.processing.Processing;
import com.djrapitops.plan.settings.Permissions;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.locale.lang.CmdHelpLang;
import com.djrapitops.plan.settings.locale.lang.CommandLang;
import com.djrapitops.plan.settings.locale.lang.DeepHelpLang;
import com.djrapitops.plugin.command.CommandNode;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.CommandUtils;
import com.djrapitops.plugin.command.Sender;

import javax.inject.Inject;

/**
 * Command used to display url to the player list page.
 *
 * @author Rsl1122
 */
public class ListPlayersCommand extends CommandNode {

    private final Locale locale;
    private final Addresses addresses;
    private final Processing processing;

    @Inject
    public ListPlayersCommand(
            Locale locale,
            Addresses addresses,
            Processing processing
    ) {
        super("players|pl|playerlist|list", Permissions.INSPECT_OTHER.getPermission(), CommandType.CONSOLE);

        this.locale = locale;
        this.addresses = addresses;
        this.processing = processing;

        setShortHelp(locale.getString(CmdHelpLang.PLAYERS));
        setInDepthHelp(locale.getArray(DeepHelpLang.PLAYERS));
    }

    @Override
    public void onCommand(Sender sender, String commandLabel, String[] args) {
        processing.submitNonCritical(() -> sendListMsg(sender));
    }

    private void sendListMsg(Sender sender) {
        sender.sendMessage(locale.getString(CommandLang.HEADER_PLAYERS));

        // Link
        String address = addresses.getMainAddress().orElseGet(() -> {
            sender.sendMessage(locale.getString(CommandLang.NO_ADDRESS_NOTIFY));
            return addresses.getFallbackLocalhostAddress();
        });
        String url = address + "/players";
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