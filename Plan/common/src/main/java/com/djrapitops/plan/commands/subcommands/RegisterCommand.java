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

import com.djrapitops.plan.PlanSystem;
import com.djrapitops.plan.delivery.domain.WebUser_old;
import com.djrapitops.plan.delivery.webserver.WebServer;
import com.djrapitops.plan.exceptions.database.DBOpException;
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
import com.djrapitops.plugin.command.CommandNode;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.CommandUtils;
import com.djrapitops.plugin.command.Sender;
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.logging.console.PluginLogger;
import com.djrapitops.plugin.logging.error.ErrorHandler;
import com.djrapitops.plugin.utilities.Verify;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;
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
    private final WebServer webServer;
    private final PluginLogger logger;
    private final ErrorHandler errorHandler;

    @Inject
    public RegisterCommand(
            Locale locale,
            Processing processing,
            DBSystem dbSystem,
            WebServer webServer,
            PluginLogger logger,
            ErrorHandler errorHandler
    ) {
        // No Permission Requirement
        super("register", "", CommandType.PLAYER_OR_ARGS);

        this.locale = locale;
        this.processing = processing;
        this.webServer = webServer;
        this.logger = logger;
        this.dbSystem = dbSystem;
        this.errorHandler = errorHandler;

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

            if (args.length < 1) {
                throw new IllegalArgumentException(locale.getString(CommandLang.FAIL_REQ_ARGS, 1, Arrays.toString(getArguments())));
            }

            if (CommandUtils.isPlayer(sender)) {
                playerRegister(args, sender);
            } else {
                consoleRegister(args, sender, notEnoughArgsMsg);
            }
        } catch (PassEncryptUtil.CannotPerformOperationException e) {
            errorHandler.log(L.WARN, this.getClass(), e);
            sender.sendMessage("§cPassword hash error.");
        } catch (NumberFormatException e) {
            throw new NumberFormatException(args[2]);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            errorHandler.log(L.WARN, this.getClass(), e);
        }
    }

    private void consoleRegister(String[] args, Sender sender, String notEnoughArgsMsg) throws PassEncryptUtil.CannotPerformOperationException {
        Verify.isTrue(args.length >= 3, () -> new IllegalArgumentException(notEnoughArgsMsg));

        String userName = args[1];
        Verify.isTrue(userName.length() <= 100, () -> new IllegalArgumentException("Username can only be 100 characters long."));
        int permLevel = Integer.parseInt(args[2]);
        String passHash = PassEncryptUtil.createHash(args[0]);
        registerUser(new WebUser_old(userName, passHash, permLevel), sender);
    }

    private void playerRegister(String[] args, Sender sender) throws PassEncryptUtil.CannotPerformOperationException {
        boolean registerSenderAsUser = args.length == 1;
        if (registerSenderAsUser) {
            String user = sender.getName();
            String pass = PassEncryptUtil.createHash(args[0]);
            int permLvl = getPermissionLevel(sender);
            registerUser(new WebUser_old(user, pass, permLvl), sender);
        } else if (sender.hasPermission(Permissions.MANAGE_WEB.getPermission())) {
            consoleRegister(args, sender, notEnoughArgsMsg);
        } else {
            sender.sendMessage(locale.getString(CommandLang.FAIL_NO_PERMISSION));
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

    private void registerUser(WebUser_old webUser, Sender sender) {
        processing.submitCritical(() -> {
            String userName = webUser.getName();
            try {
                Database database = dbSystem.getDatabase();
                boolean userExists = database.query(WebUserQueries.fetchWebUser(userName)).isPresent();
                if (userExists) {
                    sender.sendMessage(locale.getString(CommandLang.FAIL_WEB_USER_EXISTS));
                    return;
                }
                database.executeTransaction(new RegisterWebUserTransaction(webUser))
                        .get(); // Wait for completion

                sender.sendMessage(locale.getString(CommandLang.WEB_USER_REGISTER_SUCCESS, userName));
                sendLink(sender);
                logger.info(locale.getString(CommandLang.WEB_USER_REGISTER_NOTIFY, userName, webUser.getPermLevel()));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (DBOpException | ExecutionException e) {
                errorHandler.log(L.WARN, this.getClass(), e);
            }
        });
    }

    private void sendLink(Sender sender) {
        String url = PlanSystem.getMainAddress(webServer, dbSystem);
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
