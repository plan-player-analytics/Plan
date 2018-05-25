package com.djrapitops.plan.command.commands.manage;

import com.djrapitops.plan.system.database.DBSystem;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.settings.Permissions;
import com.djrapitops.plan.system.settings.locale.Locale;
import com.djrapitops.plan.system.settings.locale.Msg;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plugin.command.CommandNode;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.ISender;
import com.djrapitops.plugin.task.AbsRunnable;
import com.djrapitops.plugin.task.RunnableFactory;
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

    public ManageMoveCommand() {
        super("move", Permissions.MANAGE.getPermission(), CommandType.PLAYER_OR_ARGS);
        setShortHelp(Locale.get(Msg.CMD_USG_MANAGE_MOVE).toString());
        setArguments("<fromDB>", "<toDB>", "[-a]");
    }

    @Override
    public void onCommand(ISender sender, String commandLabel, String[] args) {
        Verify.isTrue(args.length >= 2,
                () -> new IllegalArgumentException(Locale.get(Msg.CMD_FAIL_REQ_ARGS).parse(Arrays.toString(this.getArguments()))));

        String fromDB = args[0].toLowerCase();
        boolean isCorrectDB = Verify.equalsOne(fromDB, "sqlite", "mysql");
        Verify.isTrue(isCorrectDB,
                () -> new IllegalArgumentException(Locale.get(Msg.MANAGE_FAIL_INCORRECT_DB) + fromDB));

        String toDB = args[1].toLowerCase();
        isCorrectDB = Verify.equalsOne(toDB, "sqlite", "mysql");
        Verify.isTrue(isCorrectDB,
                () -> new IllegalArgumentException(Locale.get(Msg.MANAGE_FAIL_INCORRECT_DB) + fromDB));

        Verify.isFalse(fromDB.equalsIgnoreCase(toDB),
                () -> new IllegalArgumentException(Locale.get(Msg.MANAGE_FAIL_SAME_DB).toString()));

        if (!Verify.contains("-a", args)) {
            sender.sendMessage(Locale.get(Msg.MANAGE_FAIL_CONFIRM).parse(Locale.get(Msg.MANAGE_NOTIFY_OVERWRITE).parse(args[0])));
            return;
        }

        try {
            final Database fromDatabase = DBSystem.getActiveDatabaseByName(fromDB);
            final Database toDatabase = DBSystem.getActiveDatabaseByName(toDB);

            runMoveTask(fromDatabase, toDatabase, sender);
        } catch (Exception e) {
            sender.sendMessage(Locale.get(Msg.MANAGE_FAIL_FAULTY_DB).toString());
        }
    }

    private void runMoveTask(final Database fromDatabase, final Database toDatabase, ISender sender) {
        RunnableFactory.createNew(new AbsRunnable("DBMoveTask") {
            @Override
            public void run() {
                try {
                    sender.sendMessage(Locale.get(Msg.MANAGE_INFO_START).parse());

                    fromDatabase.backup().backup(toDatabase);

                    sender.sendMessage(Locale.get(Msg.MANAGE_INFO_MOVE_SUCCESS).toString());

                    boolean movingToCurrentDB = toDatabase.getConfigName().equalsIgnoreCase(Database.getActive().getConfigName());
                    if (movingToCurrentDB) {
                        sender.sendMessage(Locale.get(Msg.MANAGE_INFO_CONFIG_REMINDER).toString());
                    }
                } catch (Exception e) {
                    Log.toLog(this.getClass(), e);
                    sender.sendMessage(Locale.get(Msg.MANAGE_INFO_FAIL).toString());
                } finally {
                    this.cancel();
                }
            }
        }).runTaskAsynchronously();
    }
}
