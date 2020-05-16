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
import com.djrapitops.plan.delivery.domain.auth.User;
import com.djrapitops.plan.delivery.webserver.Addresses;
import com.djrapitops.plan.delivery.webserver.auth.FailReason;
import com.djrapitops.plan.delivery.webserver.auth.RegistrationBin;
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
import com.djrapitops.plan.storage.database.queries.objects.WebUserQueries;
import com.djrapitops.plan.storage.database.transactions.commands.RegisterWebUserTransaction;
import com.djrapitops.plan.utilities.PassEncryptUtil;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import com.djrapitops.plugin.command.CommandNode;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.CommandUtils;
import com.djrapitops.plugin.command.Sender;
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.logging.console.PluginLogger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

/**
 * Command for registering web users.
 * <p>
 * Registers a new WebUser to the database.
 * <p>
 * No permission required for self registration. (Super constructor string is empty).
 * {@code Permissions.MANAGE_WEB} required for registering other users.
 *
 * @author Rsl1122
 */
@Singleton
public class RegisterCommand extends CommandNode {

    private final String notEnoughArgsMsg;
    private final Locale locale;
    private final Processing processing;
    private final DBSystem dbSystem;
    private final UUIDUtility uuidUtility;
    private final Addresses addresses;
    private final PluginLogger logger;
    private final ErrorLogger errorLogger;

    @Inject
    public RegisterCommand(
            Locale locale,
            Processing processing,
            Addresses addresses,
            DBSystem dbSystem,
            UUIDUtility uuidUtility,
            PluginLogger logger,
            ErrorLogger errorLogger
    ) {
        // No Permission Requirement
        super("register", "", CommandType.ALL);

        this.locale = locale;
        this.processing = processing;
        this.addresses = addresses;
        this.uuidUtility = uuidUtility;
        this.logger = logger;
        this.dbSystem = dbSystem;
        this.errorLogger = errorLogger;

        setArguments("<password>", "[name]", "[lvl]");
        setShortHelp(locale.getString(CmdHelpLang.WEB_REGISTER));
        setInDepthHelp(locale.getArray(DeepHelpLang.WEB_REGISTER));

        notEnoughArgsMsg = locale.getString(CommandLang.FAIL_REQ_ARGS, 3, Arrays.toString(getArguments()));
    }

    @Override
    public void onCommand(Sender sender, String commandLabel, String[] args) {
        try {
            Database.State dbState = dbSystem.getDatabase().getState();
            if (dbState != Database.State.OPEN) {
                sender.sendMessage(locale.getString(CommandLang.FAIL_DATABASE_NOT_OPEN, dbState.name()));
                return;
            }

            if (args.length == 0) {
                processing.submitNonCritical(() -> {
                    String url = addresses.getMainAddress().orElseGet(() -> {
                        sender.sendMessage(locale.getString(CommandLang.NO_ADDRESS_NOTIFY));
                        return addresses.getFallbackLocalhostAddress();
                    }) + "/register";
                    String linkPrefix = locale.getString(CommandLang.LINK_PREFIX);
                    sender.sendMessage(linkPrefix);
                    sender.sendLink("   ", locale.getString(CommandLang.LINK_CLICK_ME), url);
                });
                return;
            }

            Arguments arguments = new Arguments(args);
            Optional<String> code = arguments.getAfter("--code");
            if (code.isPresent()) {
                registerUsingCode(sender, code.get());
            } else {
                registerUsingLegacy(sender, arguments);
            }
        } catch (PassEncryptUtil.CannotPerformOperationException e) {
            errorLogger.log(L.WARN, this.getClass(), e);
            sender.sendMessage("§cPassword hash error.");
        } catch (NumberFormatException e) {
            throw new NumberFormatException(args[2]);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            errorLogger.log(L.WARN, this.getClass(), e);
        }
    }

    public void registerUsingCode(Sender sender, String code) {
        UUID linkedToUUID = CommandUtils.isPlayer(sender) ? uuidUtility.getUUIDOf(sender.getName()) : null;
        Optional<User> user = RegistrationBin.register(code, linkedToUUID);
        if (user.isPresent()) {
            registerUser(user.get(), sender, getPermissionLevel(sender));
        } else {
            sender.sendMessage("§c" + locale.getString(FailReason.USER_DOES_NOT_EXIST));
        }
    }

    public void registerUsingLegacy(Sender sender, Arguments arguments) {
        String password = arguments.get(0)
                .orElseThrow(() -> new IllegalArgumentException(locale.getString(CommandLang.FAIL_REQ_ARGS, 1, Arrays.toString(getArguments()))));
        String passwordHash = PassEncryptUtil.createHash(password);
        int permissionLevel = arguments.getInteger(2)
                .filter(arg -> sender.hasPermission(Permissions.MANAGE_WEB.getPerm())) // argument only allowed with plan.webmanage
                .orElseGet(() -> getPermissionLevel(sender));

        if (CommandUtils.isPlayer(sender)) {
            String playerName = sender.getName();
            UUID linkedToUUID = uuidUtility.getUUIDOf(playerName);
            String username = arguments.get(1).orElse(playerName);
            registerUser(new User(username, playerName, linkedToUUID, passwordHash, permissionLevel, Collections.emptyList()), sender, permissionLevel);
        } else {
            String username = arguments.get(1)
                    .orElseThrow(() -> new IllegalArgumentException(notEnoughArgsMsg));
            registerUser(new User(username, "console", null, passwordHash, permissionLevel, Collections.emptyList()), sender, permissionLevel);
        }
    }

    private int getPermissionLevel(Sender sender) {
        final String permAnalyze = Permissions.ANALYZE.getPerm();
        final String permInspectOther = Permissions.INSPECT_OTHER.getPerm();
        final String permInspect = Permissions.INSPECT.getPerm();
        if (sender.hasPermission(permAnalyze)) {
            return 0;
        }
        if (sender.hasPermission(permInspectOther)) {
            return 1;
        }
        if (sender.hasPermission(permInspect)) {
            return 2;
        }
        return 100;
    }

    private void registerUser(User user, Sender sender, int permissionLevel) {
        processing.submitCritical(() -> {
            String username = user.getUsername();
            user.setPermissionLevel(permissionLevel);
            try {
                Database database = dbSystem.getDatabase();
                boolean userExists = database.query(WebUserQueries.fetchUser(username)).isPresent();
                if (userExists) {
                    sender.sendMessage(locale.getString(CommandLang.FAIL_WEB_USER_EXISTS));
                    return;
                }
                database.executeTransaction(new RegisterWebUserTransaction(user))
                        .get(); // Wait for completion

                sender.sendMessage(locale.getString(CommandLang.WEB_USER_REGISTER_SUCCESS, username));
                sendLink(sender);
                logger.info(locale.getString(CommandLang.WEB_USER_REGISTER_NOTIFY, username, permissionLevel));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (DBOpException | ExecutionException e) {
                errorLogger.log(L.WARN, this.getClass(), e);
            }
        });
    }

    private void sendLink(Sender sender) {
        String url = addresses.getMainAddress().orElseGet(() -> {
            sender.sendMessage(locale.getString(CommandLang.NO_ADDRESS_NOTIFY));
            return addresses.getFallbackLocalhostAddress();
        });
        String linkPrefix = locale.getString(CommandLang.LINK_PREFIX);
        // Link
        boolean console = !CommandUtils.isPlayer(sender);
        if (console) {
            sender.sendMessage(linkPrefix + url);
        } else {
            sender.sendMessage(linkPrefix);
            sender.sendLink("   ", locale.getString(CommandLang.LINK_CLICK_ME), url);
        }
    }
}
