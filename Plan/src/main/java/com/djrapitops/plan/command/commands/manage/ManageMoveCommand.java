package com.djrapitops.plan.command.commands.manage;

import com.djrapitops.plan.system.database.DBSystem;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.settings.Permissions;
import com.djrapitops.plan.system.settings.locale.Locale;
import com.djrapitops.plan.system.settings.locale.Msg;
import com.djrapitops.plan.utilities.Condition;
import com.djrapitops.plan.utilities.ManageUtils;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.ISender;
import com.djrapitops.plugin.command.SubCommand;
import com.djrapitops.plugin.task.AbsRunnable;
import com.djrapitops.plugin.task.RunnableFactory;
import com.djrapitops.plugin.utilities.Verify;

/**
 * This manage SubCommand is used to move all data from one database to another.
 * <p>
 * Destination database will be cleared.
 *
 * @author Rsl1122
 * @since 2.3.0
 */
public class ManageMoveCommand extends SubCommand {

    public ManageMoveCommand() {
        super("move",
                CommandType.PLAYER_OR_ARGS,
                Permissions.MANAGE.getPermission(),
                Locale.get(Msg.CMD_USG_MANAGE_MOVE).toString(),
                "<fromDB> <toDB> [-a]");
    }

    @Override
    public boolean onCommand(ISender sender, String commandLabel, String[] args) {
        if (!Condition.isTrue(args.length >= 2, Locale.get(Msg.CMD_FAIL_REQ_ARGS).parse(this.getArguments()), sender)) {
            return true;
        }

        String fromDB = args[0].toLowerCase();
        boolean isCorrectDB = "sqlite".equals(fromDB) || "mysql".equals(fromDB);

        if (!Condition.isTrue(isCorrectDB, Locale.get(Msg.MANAGE_FAIL_INCORRECT_DB) + fromDB, sender)) {
            return true;
        }

        String toDB = args[1].toLowerCase();
        isCorrectDB = "sqlite".equals(toDB) || "mysql".equals(toDB);

        if (!Condition.isTrue(isCorrectDB, Locale.get(Msg.MANAGE_FAIL_INCORRECT_DB) + toDB, sender)) {
            return true;
        }

        if (!Condition.isTrue(!Verify.equalsIgnoreCase(fromDB, toDB), Locale.get(Msg.MANAGE_FAIL_SAME_DB).toString(), sender)) {
            return true;
        }

        if (!Condition.isTrue(Verify.contains("-a", args), Locale.get(Msg.MANAGE_FAIL_CONFIRM).parse(Locale.get(Msg.MANAGE_NOTIFY_REMOVE).parse(args[1])), sender)) {
            return true;
        }

        try {
            final Database fromDatabase = DBSystem.getActiveDatabaseByName(fromDB);
            final Database toDatabase = DBSystem.getActiveDatabaseByName(toDB);

            runMoveTask(fromDatabase, toDatabase, sender);
        } catch (Exception e) {
            sender.sendMessage(Locale.get(Msg.MANAGE_FAIL_FAULTY_DB).toString());
        }
        return true;
    }

    private void runMoveTask(final Database fromDatabase, final Database toDatabase, ISender sender) {
        RunnableFactory.createNew(new AbsRunnable("DBMoveTask") {
            @Override
            public void run() {
                try {
                    sender.sendMessage(Locale.get(Msg.MANAGE_INFO_START).parse());

                    ManageUtils.clearAndCopy(toDatabase, fromDatabase);
                    sender.sendMessage(Locale.get(Msg.MANAGE_INFO_MOVE_SUCCESS).toString());
                    boolean movedToCurrentDatabase = Verify.equalsIgnoreCase(toDatabase.getConfigName(), Database.getActive().getConfigName());
                    Condition.isTrue(!movedToCurrentDatabase, Locale.get(Msg.MANAGE_INFO_CONFIG_REMINDER).toString(), sender);
                } catch (Exception e) {
                    Log.toLog(this.getClass().getName() + " " + getTaskName(), e);
                    sender.sendMessage(Locale.get(Msg.MANAGE_INFO_FAIL).toString());
                } finally {
                    this.cancel();
                }
            }
        }).runTaskAsynchronously();
    }
}
