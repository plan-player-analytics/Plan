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
import com.djrapitops.plan.delivery.domain.auth.User;
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
import com.djrapitops.plan.storage.database.transactions.commands.RegisterWebUserTransaction;
import com.djrapitops.plan.storage.database.transactions.commands.RemoveWebUserTransaction;
import com.djrapitops.plan.utilities.PassEncryptUtil;
import com.djrapitops.plan.utilities.logging.ErrorContext;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import com.djrapitops.plugin.command.ColorScheme;
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.logging.console.PluginLogger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Singleton
public class RegistrationCommands {

    private final Locale locale;
    private final ColorScheme colors;
    private final DBSystem dbSystem;
    private final LinkCommands linkCommands;
    private final Confirmation confirmation;
    private final PluginLogger logger;
    private final ErrorLogger errorLogger;

    @Inject
    public RegistrationCommands(
            Locale locale,
            ColorScheme colors,
            DBSystem dbSystem,
            LinkCommands linkCommands,
            Confirmation confirmation,
            PluginLogger logger,
            ErrorLogger errorLogger
    ) {
        this.locale = locale;
        this.colors = colors;

        this.dbSystem = dbSystem;
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

    public void onRegister(CMDSender sender, Arguments arguments) {
        ensureDatabaseIsOpen();
        if (arguments.isEmpty()) {
            String address = linkCommands.getAddress(sender) + "/register";
            sender.buildMessage()
                    .addPart(colors.getMainColor() + locale.getString(CommandLang.LINK_REGISTER))
                    .apply(builder -> linkCommands.linkTo(builder, sender, address))
                    .send();
        } else {
            Optional<String> code = arguments.getAfter("--code");
            if (code.isPresent()) {
                registerUsingCode(sender, code.get());
            } else {
                registerUsingLegacy(sender, arguments);
            }
        }
    }

    public void registerUsingCode(CMDSender sender, String code) {
        UUID linkedToUUID = sender.getUUID().orElse(null);
        Optional<User> user = RegistrationBin.register(code, linkedToUUID);
        if (user.isPresent()) {
            registerUser(user.get(), sender, getPermissionLevel(sender));
        } else {
            throw new IllegalArgumentException(locale.getString(FailReason.USER_INFORMATION_NOT_FOUND));
        }
    }

    public void registerUsingLegacy(CMDSender sender, Arguments arguments) {
        String password = arguments.get(0)
                .orElseThrow(() -> new IllegalArgumentException(locale.getString(CommandLang.FAIL_REQ_ARGS, 1, "<password>")));
        String passwordHash = PassEncryptUtil.createHash(password);
        int permissionLevel = arguments.getInteger(2)
                .filter(arg -> sender.hasPermission(Permissions.REGISTER_OTHER)) // argument only allowed with register other permission
                .orElseGet(() -> getPermissionLevel(sender));

        if (sender.getUUID().isPresent() && sender.getPlayerName().isPresent()) {
            String playerName = sender.getPlayerName().get();
            UUID linkedToUUID = sender.getUUID().get();
            String username = arguments.get(1).orElse(playerName);
            registerUser(new User(username, playerName, linkedToUUID, passwordHash, permissionLevel, Collections.emptyList()), sender, permissionLevel);
        } else {
            String username = arguments.get(1)
                    .orElseThrow(() -> new IllegalArgumentException(locale.getString(CommandLang.FAIL_REQ_ARGS, 3, "<password> <name> <level>")));
            registerUser(new User(username, "console", null, passwordHash, permissionLevel, Collections.emptyList()), sender, permissionLevel);
        }
    }

    private int getPermissionLevel(CMDSender sender) {
        if (sender.hasPermission(Permissions.SERVER)) {
            return 0;
        }
        if (sender.hasPermission(Permissions.PLAYER_OTHER)) {
            return 1;
        }
        if (sender.hasPermission(Permissions.PLAYER_SELF)) {
            return 2;
        }
        return 100;
    }

    private void registerUser(User user, CMDSender sender, int permissionLevel) {
        String username = user.getUsername();
        user.setPermissionLevel(permissionLevel);
        try {
            Database database = dbSystem.getDatabase();
            boolean userExists = database.query(WebUserQueries.fetchUser(username)).isPresent();
            if (userExists) throw new IllegalArgumentException(locale.getString(CommandLang.FAIL_WEB_USER_EXISTS));

            database.executeTransaction(new RegisterWebUserTransaction(user))
                    .get(); // Wait for completion

            sender.send(locale.getString(CommandLang.WEB_USER_REGISTER_SUCCESS, username));
            logger.info(locale.getString(CommandLang.WEB_USER_REGISTER_NOTIFY, username, permissionLevel));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (DBOpException | ExecutionException e) {
            errorLogger.log(L.WARN, e, ErrorContext.builder().related(sender, user, permissionLevel).build());
        }
    }

    public void onUnregister(String mainCommand, CMDSender sender, Arguments arguments) {
        Optional<String> givenUsername = arguments.get(0).filter(arg -> sender.hasPermission(Permissions.UNREGISTER_OTHER));

        Database database = dbSystem.getDatabase();
        UUID playerUUID = sender.getUUID().orElse(null);

        String username;
        if (!givenUsername.isPresent() && playerUUID != null) {
            Optional<User> found = database.query(WebUserQueries.fetchUser(playerUUID));
            if (!found.isPresent()) {
                throw new IllegalArgumentException(locale.getString(CommandLang.USER_NOT_LINKED));
            }
            username = found.get().getUsername();
        } else if (!givenUsername.isPresent()) {
            throw new IllegalArgumentException(locale.getString(CommandLang.FAIL_REQ_ONE_ARG, "<" + locale.getString(HelpLang.ARG_USERNAME) + ">"));
        } else {
            username = givenUsername.get();
        }

        Optional<User> found = database.query(WebUserQueries.fetchUser(username));
        if (!found.isPresent()) {
            throw new IllegalArgumentException(locale.getString(FailReason.USER_DOES_NOT_EXIST));
        }
        User presentUser = found.get();
        boolean ownsTheUser = Objects.equals(playerUUID, presentUser.getLinkedToUUID());
        if (!(ownsTheUser || sender.hasPermission(Permissions.UNREGISTER_OTHER.getPerm()))) {
            throw new IllegalArgumentException(locale.getString(CommandLang.USER_NOT_LINKED));
        }

        if (sender.supportsChatEvents()) {
            sender.buildMessage()
                    .addPart(colors.getMainColor() + locale.getString(CommandLang.CONFIRM_UNREGISTER, presentUser.getUsername(), presentUser.getLinkedTo())).newLine()
                    .addPart(colors.getTertiaryColor() + locale.getString(CommandLang.CONFIRM))
                    .addPart("§2§l[\u2714]").command("/" + mainCommand + " accept").hover(locale.getString(CommandLang.CONFIRM_ACCEPT))
                    .addPart(" ")
                    .addPart("§4§l[\u2718]").command("/" + mainCommand + " cancel").hover(locale.getString(CommandLang.CONFIRM_DENY))
                    .send();
        } else {
            sender.buildMessage()
                    .addPart(colors.getMainColor() + locale.getString(CommandLang.CONFIRM_UNREGISTER, presentUser.getUsername(), presentUser.getLinkedTo())).newLine()
                    .addPart(colors.getTertiaryColor() + locale.getString(CommandLang.CONFIRM)).addPart("§a/" + mainCommand + " accept")
                    .addPart(" ")
                    .addPart("§c/" + mainCommand + " cancel")
                    .send();
        }

        confirmation.confirm(sender, choice -> {
            if (Boolean.TRUE.equals(choice)) {
                try {
                    sender.send(colors.getMainColor() + locale.getString(CommandLang.UNREGISTER, presentUser.getUsername()));
                    database.executeTransaction(new RemoveWebUserTransaction(username))
                            .get(); // Wait for completion
                    sender.send(locale.getString(CommandLang.PROGRESS_SUCCESS));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException e) {
                    errorLogger.log(L.WARN, e, ErrorContext.builder().related("unregister command", sender, sender.getPlayerName().orElse("console"), arguments).build());
                }
            } else {
                sender.send(colors.getMainColor() + locale.getString(CommandLang.CONFIRM_CANCELLED_UNREGISTER, presentUser.getUsername()));
            }
        });
    }
}
