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
import com.djrapitops.plan.commands.use.ColorScheme;
import com.djrapitops.plan.commands.use.MessageBuilder;
import com.djrapitops.plan.delivery.domain.auth.User;
import com.djrapitops.plan.delivery.rendering.html.Html;
import com.djrapitops.plan.delivery.webserver.Addresses;
import com.djrapitops.plan.identification.Identifiers;
import com.djrapitops.plan.identification.Server;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.settings.Permissions;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.locale.lang.CommandLang;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.queries.objects.ServerQueries;
import com.djrapitops.plan.storage.database.queries.objects.WebUserQueries;
import com.djrapitops.plan.utilities.dev.Untrusted;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.UUID;

/**
 * Implementation of commands that send a link to the command sender.
 *
 * @author AuroraLS3
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
        if (sender.supportsChatEvents()) {
            return builder.addPart(colors.getTertiaryColor() + "§l[" + locale.getString(CommandLang.LINK) + "]").link(address).hover(address);
        } else {
            return builder.addPart(colors.getTertiaryColor() + address);
        }
    }

    /**
     * Implementation of server subcommand, used to get link to server page.
     *
     * @param sender    Sender of command.
     * @param arguments Given arguments.
     */
    public void onServerCommand(CMDSender sender, @Untrusted Arguments arguments) {
        Server server;
        @Untrusted String identifier = arguments.concatenate(" ");
        if (arguments.isEmpty()) {
            server = serverInfo.getServer();
        } else {
            server = dbSystem.getDatabase()
                    .query(ServerQueries.fetchServerMatchingIdentifier(identifier))
                    .filter(s -> !s.isProxy())
                    .orElseThrow(() -> new IllegalArgumentException(locale.getString(CommandLang.FAIL_SERVER_NOT_FOUND, identifier)));
        }

        String address = getAddress(sender) + "/server/" + Html.encodeToURL(server.getUuid().toString());
        sender.buildMessage()
                .addPart(colors.getMainColor() + locale.getString(CommandLang.LINK_SERVER))
                .apply(builder -> linkTo(builder, sender, address))
                .send();
    }

    /**
     * Implementation of servers subcommand, used to list servers.
     *
     * @param sender    Sender of command.
     * @param arguments Given arguments.
     */
    public void onServersCommand(CMDSender sender, @Untrusted Arguments arguments) {
        ensureDatabaseIsOpen();
        String m = colors.getMainColor();
        String s = colors.getSecondaryColor();
        String t = colors.getTertiaryColor();
        String serversListed = dbSystem.getDatabase()
                .query(ServerQueries.fetchPlanServerInformationCollection())
                .stream().sorted()
                .map(server -> m + server.getId().orElse(0) + "::" + t + server.getIdentifiableName() + "::" + s + server.getUuid() + "::" + s + server.getPlanVersion() + "\n")
                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                .toString();
        sender.buildMessage()
                .addPart(t + locale.getString(CommandLang.HEADER_SERVERS)).newLine()
                .addPart(sender.getFormatter().table(
                        t + locale.getString(CommandLang.HEADER_SERVER_LIST) + '\n' + serversListed, "::"))
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
    public void onPlayerCommand(CMDSender sender, @Untrusted Arguments arguments) {
        @Untrusted String identifier = arguments.concatenate(" ");
        UUID playerUUID = identifiers.getPlayerUUID(identifier);
        UUID senderUUID = sender.getUUID().orElse(null);
        if (playerUUID == null) playerUUID = senderUUID;
        if (playerUUID == null) {
            throw new IllegalArgumentException(locale.getString(CommandLang.FAIL_PLAYER_NOT_FOUND, identifier));
        }

        if (sender.hasPermission(Permissions.PLAYER_OTHER) || playerUUID.equals(senderUUID)) {
            String address = getAddress(sender) + "/player/" + playerUUID;
            sender.buildMessage()
                    .addPart(colors.getMainColor() + locale.getString(CommandLang.LINK_PLAYER))
                    .apply(builder -> linkTo(builder, sender, address))
                    .send();
        } else {
            throw new IllegalArgumentException(locale.getString(CommandLang.FAIL_NO_PERMISSION) + " (" + Permissions.PLAYER_OTHER.get() + ')');
        }
    }

    /**
     * Implementation of players subcommand, used to get link to players page.
     *
     * @param sender    Sender of command
     * @param arguments Only present to fulfill Subcommand#onCommand requirements.
     */
    public void onPlayersCommand(CMDSender sender, @Untrusted Arguments arguments) {
        String address = getAddress(sender) + "/players";
        sender.buildMessage()
                .addPart(colors.getMainColor() + locale.getString(CommandLang.LINK_PLAYERS))
                .apply(builder -> linkTo(builder, sender, address))
                .send();
    }

    /**
     * Implementation of network subcommand, used to get link to network page.
     *
     * @param sender    Sender of command
     * @param arguments Only present to fulfill Subcommand#onCommand requirements.
     */
    public void onNetworkCommand(CMDSender sender, @Untrusted Arguments arguments) {
        String address = getAddress(sender) + "/network";
        sender.buildMessage()
                .addPart(colors.getMainColor() + locale.getString(CommandLang.LINK_NETWORK))
                .apply(builder -> linkTo(builder, sender, address))
                .send();
        if (dbSystem.getDatabase().query(ServerQueries.fetchProxyServers()).isEmpty()) {
            throw new IllegalArgumentException(locale.getString(CommandLang.NOTIFY_NO_NETWORK));
        }
    }

    /**
     * Implementation of webusers subcommand, used to list webusers.
     *
     * @param sender    Sender of command.
     * @param arguments Given arguments.
     */
    public void onWebUsersCommand(CMDSender sender, @Untrusted Arguments arguments) {
        ensureDatabaseIsOpen();
        String m = colors.getMainColor();
        String s = colors.getSecondaryColor();
        String t = colors.getTertiaryColor();
        List<User> users = dbSystem.getDatabase()
                .query(WebUserQueries.fetchAllUsers());
        if (users.isEmpty()) {
            sender.send(t + locale.getString(CommandLang.HEADER_WEB_USERS, 0));
        } else {
            String usersListed = users.stream().sorted()
                    .map(user -> m + user.getUsername() + "::" + t + user.getLinkedTo() + "::" + s + user.getPermissionGroup() + "\n")
                    .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                    .toString();
            sender.buildMessage()
                    .addPart(t + locale.getString(CommandLang.HEADER_WEB_USERS, users.size())).newLine()
                    .addPart(sender.getFormatter().table(
                            t + locale.getString(CommandLang.HEADER_WEB_USER_LIST) + '\n' + usersListed, "::"))
                    .send();
        }
    }

    public void onJson(CMDSender sender, @Untrusted Arguments arguments) {
        @Untrusted String identifier = arguments.concatenate(" ");
        UUID playerUUID = identifiers.getPlayerUUID(identifier);
        UUID senderUUID = sender.getUUID().orElse(null);
        if (playerUUID == null) playerUUID = senderUUID;
        if (playerUUID == null) {
            throw new IllegalArgumentException(locale.getString(CommandLang.FAIL_PLAYER_NOT_FOUND, identifier));
        }

        if (sender.hasPermission(Permissions.JSON_OTHER) || playerUUID.equals(senderUUID)) {
            String address = getAddress(sender) + "/player/" + playerUUID + "/raw";
            sender.buildMessage()
                    .addPart(colors.getMainColor() + locale.getString(CommandLang.LINK_JSON))
                    .apply(builder -> linkTo(builder, sender, address))
                    .send();
        } else {
            throw new IllegalArgumentException(locale.getString(CommandLang.FAIL_NO_PERMISSION) + " (" + Permissions.JSON_OTHER.get() + ')');
        }
    }
}
