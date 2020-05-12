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

import com.djrapitops.plan.commands.Arguments;
import com.djrapitops.plan.commands.use.CMDSender;
import com.djrapitops.plan.commands.use.MessageBuilder;
import com.djrapitops.plan.delivery.rendering.html.Html;
import com.djrapitops.plan.delivery.webserver.Addresses;
import com.djrapitops.plan.identification.Identifiers;
import com.djrapitops.plan.identification.Server;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.locale.lang.CommandLang;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.queries.objects.ServerQueries;
import com.djrapitops.plan.storage.database.queries.objects.UserIdentifierQueries;
import com.djrapitops.plugin.command.ColorScheme;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.UUID;

@Singleton
public class LinkCommands {

    private final Locale locale;
    private final ColorScheme colors;
    private final Addresses addresses;
    private final Identifiers identifiers;
    private final DBSystem dbSystem;
    private final ServerInfo serverInfo;

    @Inject
    public LinkCommands(
            Locale locale,
            ColorScheme colorScheme,
            Addresses addresses,
            Identifiers identifiers,
            DBSystem dbSystem,
            ServerInfo serverInfo
    ) {
        this.locale = locale;
        this.colors = colorScheme;
        this.addresses = addresses;
        this.identifiers = identifiers;
        this.dbSystem = dbSystem;
        this.serverInfo = serverInfo;
    }

    private String getAddress(CMDSender sender) {
        return addresses.getMainAddress().orElseGet(() -> {
            sender.send(locale.getString(CommandLang.NO_ADDRESS_NOTIFY));
            return addresses.getFallbackLocalhostAddress();
        });
    }

    public void onServerCommand(CMDSender sender, Arguments arguments) {
        Server server;
        String identifier = arguments.concatenate(" ");
        if (arguments.isEmpty()) {
            server = serverInfo.getServer();
        } else {
            server = dbSystem.getDatabase()
                    .query(ServerQueries.fetchServerMatchingIdentifier(identifier))
                    .filter(s -> !s.isProxy())
                    .orElseThrow(() -> new IllegalArgumentException("Server '" + identifier + "' was not found from the database."));
        }

        String address = getAddress(sender);
        String target = "/server/" + Html.encodeToURL(server.getName());
        sender.buildMessage()
                .addPart(colors.getMainColor() + "View server page: ")
                .addPart(colors.getTertiaryColor() + "§l[Link]").link(address + target).hover(address + target)
                .send();
    }

    public void onPlayerCommand(CMDSender sender, Arguments arguments) {
        String identifier = arguments.concatenate(" ");
        UUID playerUUID = identifiers.getPlayerUUID(identifier);
        UUID senderUUID = sender.getUUID().orElse(null);
        if (playerUUID == null) playerUUID = senderUUID;
        if (playerUUID == null) {
            throw new IllegalArgumentException("Player '" + identifier + "' was not found, they have no UUID.");
        }

        String playerName = dbSystem.getDatabase().query(UserIdentifierQueries.fetchPlayerNameOf(playerUUID))
                .orElseThrow(() -> new IllegalArgumentException("Player '" + identifier + "' was not found in the database."));

        if (sender.hasPermission("plan.player.other") || playerUUID.equals(senderUUID)) {
            String address = getAddress(sender);
            String target = "/player/" + Html.encodeToURL(playerName);
            sender.buildMessage()
                    .addPart(colors.getMainColor() + "View player page: ")
                    .addPart(colors.getTertiaryColor() + "§l[Link]").link(address + target).hover(address + target)
                    .send();
        } else {
            throw new IllegalArgumentException("Insufficient permissions: You can not view other player's pages.");
        }
    }

    public void onPlayersCommand(CMDSender sender, Arguments arguments) {
        String address = getAddress(sender);
        String target = "/players";
        sender.buildMessage()
                .addPart(colors.getMainColor() + "View players page: ")
                .addPart(colors.getTertiaryColor() + "§l[Link]").link(address + target).hover(address + target)
                .send();
    }

    public void onNetworkCommand(CMDSender sender, Arguments arguments) {
        String address = getAddress(sender);
        String target = "/network";
        sender.buildMessage()
                .addPart(colors.getMainColor() + "View network page: ")
                .addPart(colors.getTertiaryColor() + "§l[Link]").link(address + target).hover(address + target)
                .send();
        dbSystem.getDatabase().query(ServerQueries.fetchProxyServerInformation())
                .orElseThrow(() -> new IllegalArgumentException("Server is not connected to a network. The link redirects to server page."));
    }

    public void onServersCommand(CMDSender sender, Arguments arguments) {
        String m = colors.getMainColor();
        String t = colors.getTertiaryColor();
        String serversListed = dbSystem.getDatabase()
                .query(ServerQueries.fetchPlanServerInformationCollection())
                .stream().sorted()
                .map(server -> server.getId() + ":" + server.getName() + ":" + server.getUuid() + "\n")
                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                .toString();
        String table = ChatFormatter
        MessageBuilder message = sender.buildMessage().addPart(t + '>' + m + " Servers; id : name : uuid").newLine();
    }
}
