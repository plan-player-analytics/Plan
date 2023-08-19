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
import com.djrapitops.plan.delivery.domain.auth.User;
import com.djrapitops.plan.delivery.domain.auth.WebPermission;
import com.djrapitops.plan.delivery.webserver.auth.ActiveCookieStore;
import com.djrapitops.plan.delivery.webserver.auth.FailReason;
import com.djrapitops.plan.delivery.webserver.auth.RegistrationBin;
import com.djrapitops.plan.exceptions.database.DBOpException;
import com.djrapitops.plan.settings.Permissions;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.locale.lang.CommandLang;
import com.djrapitops.plan.settings.locale.lang.HelpLang;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.queries.objects.WebUserQueries;
import com.djrapitops.plan.storage.database.transactions.commands.RemoveWebUserTransaction;
import com.djrapitops.plan.storage.database.transactions.commands.StoreWebUserTransaction;
import com.djrapitops.plan.utilities.dev.Untrusted;
import com.djrapitops.plan.utilities.logging.ErrorContext;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import net.playeranalytics.plugin.server.PluginLogger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Singleton
public class RegistrationCommands {

    private final Locale locale;
    private final ColorScheme colors;
    private final DBSystem dbSystem;
    private final ActiveCookieStore activeCookieStore;
    private final LinkCommands linkCommands;
    private final Confirmation confirmation;
    private final PluginLogger logger;
    private final ErrorLogger errorLogger;

    @Inject
    public RegistrationCommands(
            Locale locale,
            ColorScheme colors,
            DBSystem dbSystem,
            ActiveCookieStore activeCookieStore,
            LinkCommands linkCommands,
            Confirmation confirmation,
            PluginLogger logger,
            ErrorLogger errorLogger
    ) {
        this.locale = locale;
        this.colors = colors;

        this.dbSystem = dbSystem;
        this.activeCookieStore = activeCookieStore;
        this.linkCommands = linkCommands;
        this.confirmation = confirmation;
        this.logger = logger;
        this.errorLogger = errorLogger;
    }

    private void ensureDatabaseIsOpen() {
        Database.State dbState = dbSystem.getDatabase().getState();
        if (dbState != Database.State.OPEN) {
            throw new IllegalArgumentException(locale.getString(CommandLang.FAIL_DATABASE_NOT_OPEN, dbState.name()));
        }
    }

    public void onRegister(CMDSender sender, @Untrusted Arguments arguments) {
        ensureDatabaseIsOpen();
        if (arguments.isEmpty()) {
            String address = linkCommands.getAddress(sender) + "/register";
            sender.buildMessage()
                    .addPart(colors.getMainColor() + locale.getString(CommandLang.LINK_REGISTER))
                    .apply(builder -> linkCommands.linkTo(builder, sender, address))
                    .send();
        } else {
            @Untrusted Optional<String> code = arguments.getAfter("--code");
            if (code.isPresent()) {
                registerUsingCode(sender, code.get(), arguments);
            } else {
                sender.send(locale.getString(CommandLang.FAIL_REQ_ARGS, "--code", "/plan register --code 81cc5b17"));
            }
        }
    }

    public void registerUsingCode(CMDSender sender, @Untrusted String code, @Untrusted Arguments arguments) {
        UUID linkedToUUID = sender.getUUID().orElse(null);
        User user = RegistrationBin.register(code, linkedToUUID)
                .orElseThrow(() -> new IllegalArgumentException(locale.getString(FailReason.USER_INFORMATION_NOT_FOUND)));
        String permissionGroup = getPermissionGroup(sender, arguments)
                .orElseThrow(() -> new IllegalArgumentException(locale.getString(FailReason.NO_PERMISSION_GROUP)));
        user.setPermissionGroup(permissionGroup);
        registerUser(user, sender);
    }

    private Optional<String> getPermissionGroup(CMDSender sender, @Untrusted Arguments arguments) {
        List<String> groups = dbSystem.getDatabase().query(WebUserQueries.fetchGroupNames());
        if (sender.isPlayer()) {
            for (String group : groups) {
                if (sender.hasPermission("plan.webgroup." + group)) {
                    return Optional.of(group);
                }
            }
        } else if (arguments.contains("superuser")) {
            return dbSystem.getDatabase().query(WebUserQueries.fetchGroupNamesWithPermission(WebPermission.MANAGE_GROUPS.getPermission()))
                    .stream().findFirst();
        }
        return Optional.empty();
    }

    private void registerUser(User user, CMDSender sender) {
        String username = user.getUsername();

        try {
            Database database = dbSystem.getDatabase();
            boolean userExists = database.query(WebUserQueries.fetchUser(username)).isPresent();
            if (userExists) throw new IllegalArgumentException(locale.getString(CommandLang.FAIL_WEB_USER_EXISTS));

            database.executeTransaction(new StoreWebUserTransaction(user))
                    .get(); // Wait for completion

            sender.send(locale.getString(CommandLang.WEB_USER_REGISTER_SUCCESS, username));
            logger.info(locale.getString(CommandLang.WEB_USER_REGISTER_NOTIFY, username, user.getPermissionGroup()));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (DBOpException | ExecutionException e) {
            errorLogger.warn(e, ErrorContext.builder().related(sender, user).build());
        }
    }

    public void onUnregister(CMDSender sender, @Untrusted Arguments arguments) {
        @Untrusted Optional<String> givenUsername = arguments.get(0).filter(arg -> sender.hasPermission(Permissions.UNREGISTER_OTHER));

        Database database = dbSystem.getDatabase();
        UUID playerUUID = sender.getUUID().orElse(null);

        @Untrusted String username;
        if (givenUsername.isEmpty() && playerUUID != null) {
            username = database.query(WebUserQueries.fetchUser(playerUUID))
                    .map(User::getUsername)
                    .orElseThrow(() -> new IllegalArgumentException(locale.getString(CommandLang.USER_NOT_LINKED)));
        } else if (givenUsername.isEmpty()) {
            throw new IllegalArgumentException(locale.getString(CommandLang.FAIL_REQ_ONE_ARG, "<" + locale.getString(HelpLang.ARG_USERNAME) + ">"));
        } else {
            username = givenUsername.get();
        }

        User user = database.query(WebUserQueries.fetchUser(username))
                .orElseThrow(() -> new IllegalArgumentException(locale.getString(FailReason.USER_DOES_NOT_EXIST)));
        boolean ownsTheUser = Objects.equals(playerUUID, user.getLinkedToUUID());
        if (!(ownsTheUser || sender.hasPermission(Permissions.UNREGISTER_OTHER.getPerm()))) {
            throw new IllegalArgumentException(locale.getString(CommandLang.USER_NOT_LINKED));
        }

        String prompt = locale.getString(CommandLang.CONFIRM_UNREGISTER, user.getUsername(), user.getLinkedTo());

        confirmation.confirm(sender, prompt, choice -> {
            if (Boolean.TRUE.equals(choice)) {
                try {
                    sender.send(colors.getMainColor() + locale.getString(CommandLang.UNREGISTER, user.getUsername()));
                    database.executeTransaction(new RemoveWebUserTransaction(username))
                            .get(); // Wait for completion
                    ActiveCookieStore.removeUserCookie(username);
                    sender.send(locale.getString(CommandLang.PROGRESS_SUCCESS));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException e) {
                    errorLogger.warn(e, ErrorContext.builder().related("unregister command", sender, sender.getPlayerName().orElse("console"), arguments).build());
                }
            } else {
                sender.send(colors.getMainColor() + locale.getString(CommandLang.CONFIRM_CANCELLED_UNREGISTER, user.getUsername()));
            }
        });
    }


    public void onLogoutCommand(@Untrusted Arguments arguments) {
        @Untrusted String loggingOut = arguments.get(0)
                .orElseThrow(() -> new IllegalArgumentException(locale.getString(CommandLang.FAIL_REQ_ONE_ARG, locale.getString(HelpLang.ARG_USERNAME) + "/*")));

        if ("*".equals(loggingOut)) {
            activeCookieStore.removeAll();
        } else {
            ActiveCookieStore.removeUserCookie(loggingOut);
        }
    }

    public void onChangePermissionGroup(CMDSender sender, @Untrusted Arguments arguments) {
        String username = arguments.get(0)
                .orElseThrow(() -> new IllegalArgumentException(locale.getString(CommandLang.FAIL_REQ_ARGS, locale.getString(HelpLang.ARG_USERNAME))));
        String group = arguments.get(1)
                .orElseThrow(() -> new IllegalArgumentException(locale.getString(CommandLang.FAIL_REQ_ARGS, locale.getString(HelpLang.ARG_GROUP))));

        Database database = dbSystem.getDatabase();
        User user = database.query(WebUserQueries.fetchUser(username))
                .orElseThrow(() -> new IllegalArgumentException(locale.getString(FailReason.USER_DOES_NOT_EXIST)));

        Optional<Integer> groupId = database.query(WebUserQueries.fetchGroupId(group));
        if (groupId.isEmpty()) {
            throw new IllegalArgumentException(locale.getString(FailReason.GROUP_DOES_NOT_EXIST));
        }

        user.setPermissionGroup(group);

        database.executeTransaction(new StoreWebUserTransaction(user))
                .thenRun(() -> sender.send(locale.getString(CommandLang.PROGRESS_SUCCESS)));
    }

    public void onListWebGroups(CMDSender sender) {
        Database database = dbSystem.getDatabase();
        List<String> groupNames = database.query(WebUserQueries.fetchGroupNames());

        sender.send(String.join(", ", groupNames));
    }
}
