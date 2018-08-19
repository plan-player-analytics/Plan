package com.djrapitops.plan.command.commands.webuser;

import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.locale.lang.CmdHelpLang;
import com.djrapitops.plan.system.locale.lang.CommandLang;
import com.djrapitops.plan.system.locale.lang.ManageLang;
import com.djrapitops.plan.system.processing.Processing;
import com.djrapitops.plan.system.settings.Permissions;
import com.djrapitops.plugin.command.CommandNode;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.ISender;
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.logging.error.ErrorHandler;
import com.djrapitops.plugin.utilities.Verify;

import javax.inject.Inject;
import java.util.Arrays;

/**
 * Subcommand for deleting a WebUser.
 *
 * @author Rsl1122
 * @since 3.5.2
 */
public class WebDeleteCommand extends CommandNode {

    private final Locale locale;
    private final Database database;
    private final ErrorHandler errorHandler;

    @Inject
    public WebDeleteCommand(Locale locale, Database database, ErrorHandler errorHandler) {
        super("delete|remove", Permissions.MANAGE_WEB.getPerm(), CommandType.PLAYER_OR_ARGS);

        this.locale = locale;
        this.database = database;
        this.errorHandler = errorHandler;

        setShortHelp(locale.getString(CmdHelpLang.WEB_DELETE));
        setArguments("<username>");
    }

    @Override
    public void onCommand(ISender sender, String commandLabel, String[] args) {
        Verify.isTrue(args.length >= 1,
                () -> new IllegalArgumentException(locale.getString(CommandLang.FAIL_REQ_ONE_ARG, Arrays.toString(this.getArguments()))));

        String user = args[0];

        Processing.submitNonCritical(() -> {
            try {
                if (!database.check().doesWebUserExists(user)) {
                    sender.sendMessage("§c[Plan] User Doesn't exist.");
                    return;
                }
                database.remove().webUser(user);
                sender.sendMessage(locale.getString(ManageLang.PROGRESS_SUCCESS));
            } catch (Exception e) {
                errorHandler.log(L.ERROR, this.getClass(), e);
                sender.sendMessage(locale.getString(ManageLang.PROGRESS_FAIL, e.getMessage()));
            }
        });
    }

}
