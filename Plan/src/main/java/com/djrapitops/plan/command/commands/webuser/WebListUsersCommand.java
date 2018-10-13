package com.djrapitops.plan.command.commands.webuser;

import com.djrapitops.plan.data.WebUser;
import com.djrapitops.plan.system.database.DBSystem;
import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.locale.lang.CmdHelpLang;
import com.djrapitops.plan.system.locale.lang.CommandLang;
import com.djrapitops.plan.system.locale.lang.ManageLang;
import com.djrapitops.plan.system.processing.Processing;
import com.djrapitops.plan.system.settings.Permissions;
import com.djrapitops.plan.utilities.comparators.WebUserComparator;
import com.djrapitops.plugin.command.CommandNode;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.Sender;
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.logging.error.ErrorHandler;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

/**
 * Subcommand for checking WebUser list.
 *
 * @author Rsl1122
 * @since 3.5.2
 */
@Singleton
public class WebListUsersCommand extends CommandNode {

    private final Locale locale;
    private final Processing processing;
    private final DBSystem dbSystem;
    private final ErrorHandler errorHandler;

    @Inject
    public WebListUsersCommand(
            Locale locale,
            Processing processing,
            DBSystem dbSystem,
            ErrorHandler errorHandler
    ) {
        super("list", Permissions.MANAGE_WEB.getPerm(), CommandType.CONSOLE);

        this.locale = locale;
        this.processing = processing;
        this.dbSystem = dbSystem;
        this.errorHandler = errorHandler;

        setShortHelp(locale.getString(CmdHelpLang.WEB_LIST));
    }

    @Override
    public void onCommand(Sender sender, String commandLabel, String[] args) {
        processing.submitNonCritical(() -> {
            try {
                List<WebUser> users = dbSystem.getDatabase().fetch().getWebUsers();
                users.sort(new WebUserComparator());
                sender.sendMessage(locale.getString(CommandLang.HEADER_WEB_USERS, users.size()));
                for (WebUser user : users) {
                    sender.sendMessage(locale.getString(CommandLang.WEB_USER_LIST, user.getName(), user.getPermLevel()));
                }
                sender.sendMessage(">");
            } catch (Exception e) {
                errorHandler.log(L.ERROR, this.getClass(), e);
                sender.sendMessage(locale.getString(ManageLang.PROGRESS_FAIL, e.getMessage()));
            }
        });
    }

}
