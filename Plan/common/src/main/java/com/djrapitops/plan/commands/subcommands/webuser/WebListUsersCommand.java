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
package com.djrapitops.plan.commands.subcommands.webuser;

import com.djrapitops.plan.delivery.domain.auth.User;
import com.djrapitops.plan.processing.Processing;
import com.djrapitops.plan.settings.Permissions;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.locale.lang.CmdHelpLang;
import com.djrapitops.plan.settings.locale.lang.CommandLang;
import com.djrapitops.plan.settings.locale.lang.ManageLang;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.queries.objects.WebUserQueries;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import com.djrapitops.plugin.command.CommandNode;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.Sender;
import com.djrapitops.plugin.logging.L;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

/**
 * Subcommand for checking WebUser list.
 *
 * @author Rsl1122
 */
@Singleton
public class WebListUsersCommand extends CommandNode {

    private final Locale locale;
    private final Processing processing;
    private final DBSystem dbSystem;
    private final ErrorLogger errorLogger;

    @Inject
    public WebListUsersCommand(
            Locale locale,
            Processing processing,
            DBSystem dbSystem,
            ErrorLogger errorLogger
    ) {
        super("list", Permissions.MANAGE_WEB.getPerm(), CommandType.CONSOLE);

        this.locale = locale;
        this.processing = processing;
        this.dbSystem = dbSystem;
        this.errorLogger = errorLogger;

        setShortHelp(locale.getString(CmdHelpLang.WEB_LIST));
    }

    @Override
    public void onCommand(Sender sender, String commandLabel, String[] args) {
        Database.State dbState = dbSystem.getDatabase().getState();
        if (dbState != Database.State.OPEN) {
            sender.sendMessage(locale.getString(CommandLang.FAIL_DATABASE_NOT_OPEN, dbState.name()));
            return;
        }

        processing.submitNonCritical(() -> {
            try {
                List<User> users = dbSystem.getDatabase().query(WebUserQueries.fetchAllUsers());
                sender.sendMessage(locale.getString(CommandLang.HEADER_WEB_USERS, users.size()));
                for (User user : users) {
                    sender.sendMessage(locale.getString(CommandLang.WEB_USER_LIST, user.getUsername(), user.getPermissionLevel()));
                }
                sender.sendMessage(">");
            } catch (Exception e) {
                errorLogger.log(L.ERROR, this.getClass(), e);
                sender.sendMessage(locale.getString(ManageLang.PROGRESS_FAIL, e.getMessage()));
            }
        });
    }

}
