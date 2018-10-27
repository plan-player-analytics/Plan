package com.djrapitops.plan.command.commands.webuser;

import com.djrapitops.plan.system.database.DBSystem;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.locale.lang.CmdHelpLang;
import com.djrapitops.plan.system.locale.lang.CommandLang;
import com.djrapitops.plan.system.locale.lang.ManageLang;
import com.djrapitops.plan.system.processing.Processing;
import com.djrapitops.plan.system.settings.Permissions;
import com.djrapitops.plugin.command.CommandNode;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.Sender;
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.logging.error.ErrorHandler;
import com.djrapitops.plugin.utilities.Verify;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;

/**
 * Subcommand for deleting a WebUser.
 *
 * @author Rsl1122
 * @since 3.5.2
 */
@Singleton
public class WebDeleteCommand extends CommandNode {

    private final Locale locale;
    private final Processing processing;
    private final DBSystem dbSystem;
    private final ErrorHandler errorHandler;

    @Inject
    public WebDeleteCommand(
            Locale locale,
            Processing processing,
            DBSystem dbSystem,
            ErrorHandler errorHandler
    ) {
        super("delete|remove", Permissions.MANAGE_WEB.getPerm(), CommandType.PLAYER_OR_ARGS);

        this.locale = locale;
        this.processing = processing;
        this.dbSystem = dbSystem;
        this.errorHandler = errorHandler;

        setShortHelp(locale.getString(CmdHelpLang.WEB_DELETE));
        setArguments("<username>");
    }

    @Override
    public void onCommand(Sender sender, String commandLabel, String[] args) {
        Verify.isTrue(args.length >= 1,
                () -> new IllegalArgumentException(locale.getString(CommandLang.FAIL_REQ_ONE_ARG, Arrays.toString(this.getArguments()))));

        String user = args[0];

        processing.submitNonCritical(() -> {
            try {
                Database db = dbSystem.getDatabase();
                if (!db.check().doesWebUserExists(user)) {
                    sender.sendMessage("§c[Plan] User Doesn't exist.");
                    return;
                }
                db.remove().webUser(user);
                sender.sendMessage(locale.getString(ManageLang.PROGRESS_SUCCESS));
            } catch (Exception e) {
                errorHandler.log(L.ERROR, this.getClass(), e);
                sender.sendMessage(locale.getString(ManageLang.PROGRESS_FAIL, e.getMessage()));
            }
        });
    }

}
