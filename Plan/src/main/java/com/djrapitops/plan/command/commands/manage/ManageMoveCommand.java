package com.djrapitops.plan.command.commands.manage;

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.system.database.DBSystem;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.locale.lang.CmdHelpLang;
import com.djrapitops.plan.system.locale.lang.CommandLang;
import com.djrapitops.plan.system.locale.lang.DeepHelpLang;
import com.djrapitops.plan.system.locale.lang.ManageLang;
import com.djrapitops.plan.system.processing.Processing;
import com.djrapitops.plan.system.settings.Permissions;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plugin.command.CommandNode;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.ISender;
import com.djrapitops.plugin.utilities.Verify;

import java.util.Arrays;

/**
 * This manage SubCommand is used to move all data from one database to another.
 * <p>
 * Destination database will be cleared.
 *
 * @author Rsl1122
 * @since 2.3.0
 */
public class ManageMoveCommand extends CommandNode {

    private final Locale locale;

    public ManageMoveCommand(PlanPlugin plugin) {
        super("move", Permissions.MANAGE.getPermission(), CommandType.PLAYER_OR_ARGS);

        locale = plugin.getSystem().getLocaleSystem().getLocale();

        setArguments("<fromDB>", "<toDB>", "[-a]");
        setShortHelp(locale.getString(CmdHelpLang.MANAGE_MOVE));
        setInDepthHelp(locale.getArray(DeepHelpLang.MANAGE_MOVE));
    }

    @Override
    public void onCommand(ISender sender, String commandLabel, String[] args) {
        Verify.isTrue(args.length >= 2,
                () -> new IllegalArgumentException(locale.getString(CommandLang.FAIL_REQ_ARGS, 2, Arrays.toString(this.getArguments()))));

        String fromDB = args[0].toLowerCase();
        boolean isCorrectDB = Verify.equalsOne(fromDB, "sqlite", "mysql");
        Verify.isTrue(isCorrectDB,
                () -> new IllegalArgumentException(locale.getString(ManageLang.FAIL_INCORRECT_DB, fromDB)));

        String toDB = args[1].toLowerCase();
        isCorrectDB = Verify.equalsOne(toDB, "sqlite", "mysql");
        Verify.isTrue(isCorrectDB,
                () -> new IllegalArgumentException(locale.getString(ManageLang.FAIL_INCORRECT_DB, toDB)));

        Verify.isFalse(fromDB.equalsIgnoreCase(toDB),
                () -> new IllegalArgumentException(locale.getString(ManageLang.FAIL_SAME_DB)));

        if (!Verify.contains("-a", args)) {
            sender.sendMessage(locale.getString(ManageLang.CONFIRMATION, locale.getString(ManageLang.CONFIRM_OVERWRITE, toDB)));
            return;
        }

        try {
            final Database fromDatabase = DBSystem.getActiveDatabaseByName(fromDB);
            final Database toDatabase = DBSystem.getActiveDatabaseByName(toDB);

            runMoveTask(fromDatabase, toDatabase, sender);
        } catch (Exception e) {
            sender.sendMessage(locale.getString(ManageLang.PROGRESS_FAIL, e.getMessage()));
        }
    }

    private void runMoveTask(final Database fromDatabase, final Database toDatabase, ISender sender) {
        Processing.submitCritical(() -> {
            try {
                sender.sendMessage(locale.getString(ManageLang.PROGRESS_START));

                fromDatabase.backup().backup(toDatabase);

                sender.sendMessage(locale.getString(ManageLang.PROGRESS_SUCCESS));

                boolean movingToCurrentDB = toDatabase.getConfigName().equalsIgnoreCase(Database.getActive().getConfigName());
                if (movingToCurrentDB) {
                    sender.sendMessage(locale.getString(ManageLang.HOTSWAP_REMINDER, toDatabase.getConfigName()));
                }
            } catch (Exception e) {
                Log.toLog(this.getClass(), e);
                sender.sendMessage(locale.getString(ManageLang.PROGRESS_FAIL, e.getMessage()));
            }
        });
    }
}
