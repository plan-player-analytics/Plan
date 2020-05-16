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

import com.djrapitops.plan.commands.use.Arguments;
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
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.queries.objects.ServerQueries;
import com.djrapitops.plan.storage.database.queries.objects.UserIdentifierQueries;
import com.djrapitops.plugin.command.ColorScheme;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.UUID;

/**
 * Implementation of commands that send a link to the command sender.
 *
 * @author Rsl1122
 */
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

    String getAddress(CMDSender sender) {
        return addresses.getMainAddress().orElseGet(() -> {
            sender.send(locale.getString(CommandLang.NO_ADDRESS_NOTIFY));
            return addresses.getFallbackLocalhostAddress();
        });
    }

    MessageBuilder linkTo(MessageBuilder builder, CMDSender sender, String address) {
        if (sender.isPlayer()) {
            builder.addPart(colors.getTertiaryColor() + "§l[Link]").link(address).hover(address);
        } else {
            builder.addPart(colors.getTertiaryColor() + address);
        }
        return builder;
    }

    /**
     * Implementation of server subcommand, used to get link to server page.
     *
     * @param sender    Sender of command.
     * @param arguments Given arguments.
     */
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

        String address = getAddress(sender) + "/server/" + Html.encodeToURL(server.getName());
        sender.buildMessage()
                .addPart(colors.getMainColor() + "Server page: ")
                .apply(builder -> linkTo(builder, sender, address))
                .send();
    }

    /**
     * Implementation of servers subcommand, used to list servers.
     *
     * @param sender    Sender of command.
     * @param arguments Given arguments.
     */
    public void onServersCommand(CMDSender sender, Arguments arguments) {
        ensureDatabaseIsOpen();
        String m = colors.getMainColor();
        String s = colors.getSecondaryColor();
        String t = colors.getTertiaryColor();
        String serversListed = dbSystem.getDatabase()
                .query(ServerQueries.fetchPlanServerInformationCollection())
                .stream().sorted()
                .map(server -> m + server.getId() + ":" + t + server.getName() + ":" + s + server.getUuid() + "\n")
                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                .toString();
        sender.buildMessage()
                .addPart(t + '>' + m + " Servers").newLine()
                .addPart(sender.getFormatter().table(
                        t + "id:name:uuid\n" + serversListed, ":"))
                .send();
    }

    private void ensureDatabaseIsOpen() {
        Database.State dbState = dbSystem.getDatabase().getState();
        if (dbState != Database.State.OPEN) {
            throw new IllegalArgumentException(locale.getString(CommandLang.FAIL_DATABASE_NOT_OPEN, dbState.name()));
        }
    }

    /**
     * Implementation of player command, used to get link to player page.
     *
     * @param sender    Sender of command.
     * @param arguments Given arguments.
     */
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
            String address = getAddress(sender) + "/player/" + Html.encodeToURL(playerName);
            sender.buildMessage()
                    .addPart(colors.getMainColor() + "Player page: ")
                    .apply(builder -> linkTo(builder, sender, address))
                    .send();
        } else {
            throw new IllegalArgumentException("Insufficient permissions: You can not view other player's pages.");
        }
    }

    /**
     * Implementation of players subcommand, used to get link to players page.
     *
     * @param sender    Sender of command
     * @param arguments Only present to fulfill Subcommand#onCommand requirements.
     */
    public void onPlayersCommand(CMDSender sender, Arguments arguments) {
        String address = getAddress(sender) + "/players";
        sender.buildMessage()
                .addPart(colors.getMainColor() + "Players page: ")
                .apply(builder -> linkTo(builder, sender, address))
                .send();
    }

    /**
     * Implementation of network subcommand, used to get link to network page.
     *
     * @param sender    Sender of command
     * @param arguments Only present to fulfill Subcommand#onCommand requirements.
     */
    public void onNetworkCommand(CMDSender sender, Arguments arguments) {
        String address = getAddress(sender) + "/network";
        sender.buildMessage()
                .addPart(colors.getMainColor() + "Network page: ")
                .apply(builder -> linkTo(builder, sender, address))
                .send();
        dbSystem.getDatabase().query(ServerQueries.fetchProxyServerInformation())
                .orElseThrow(() -> new IllegalArgumentException("Server is not connected to a network. The link redirects to server page."));
    }

}
