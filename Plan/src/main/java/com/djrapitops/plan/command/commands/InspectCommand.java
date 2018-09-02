package com.djrapitops.plan.command.commands;

import com.djrapitops.plan.api.exceptions.database.DBOpException;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.info.connection.ConnectionSystem;
import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.locale.lang.CmdHelpLang;
import com.djrapitops.plan.system.locale.lang.CommandLang;
import com.djrapitops.plan.system.locale.lang.DeepHelpLang;
import com.djrapitops.plan.system.processing.Processing;
import com.djrapitops.plan.system.processing.processors.info.InfoProcessors;
import com.djrapitops.plan.system.settings.Permissions;
import com.djrapitops.plan.system.webserver.WebServer;
import com.djrapitops.plan.utilities.MiscUtils;
import com.djrapitops.plan.utilities.uuid.UUIDUtility;
import com.djrapitops.plugin.command.CommandNode;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.CommandUtils;
import com.djrapitops.plugin.command.ISender;
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.logging.error.ErrorHandler;

import javax.inject.Inject;
import java.util.UUID;

/**
 * This command is used to refresh Inspect page and display link.
 *
 * @author Rsl1122
 * @since 1.0.0
 */
public class InspectCommand extends CommandNode {

    private final Locale locale;
    private final Database database;
    private final WebServer webServer;
    private final InfoProcessors processorFactory;
    private final Processing processing;
    private final ConnectionSystem connectionSystem;
    private final UUIDUtility uuidUtility;
    private final ErrorHandler errorHandler;

    @Inject
    public InspectCommand(
            Locale locale,
            InfoProcessors processorFactory,
            Processing processing,
            Database database,
            WebServer webServer,
            ConnectionSystem connectionSystem,
            UUIDUtility uuidUtility,
            ErrorHandler errorHandler
    ) {
        super("inspect", Permissions.INSPECT.getPermission(), CommandType.PLAYER_OR_ARGS);
        this.processorFactory = processorFactory;
        this.processing = processing;
        this.connectionSystem = connectionSystem;
        setArguments("<player>");

        this.locale = locale;
        this.database = database;
        this.webServer = webServer;
        this.uuidUtility = uuidUtility;
        this.errorHandler = errorHandler;

        setShortHelp(locale.getString(CmdHelpLang.INSPECT));
        setInDepthHelp(locale.getArray(DeepHelpLang.INSPECT));
    }

    @Override
    public void onCommand(ISender sender, String commandLabel, String[] args) {
        String playerName = MiscUtils.getPlayerName(args, sender);

        if (playerName == null) {
            sender.sendMessage(locale.getString(CommandLang.FAIL_NO_PERMISSION));
        }

        runInspectTask(playerName, sender);
    }

    private void runInspectTask(String playerName, ISender sender) {
        processing.submitNonCritical(() -> {
            try {
                UUID uuid = uuidUtility.getUUIDOf(playerName);
                if (uuid == null) {
                    sender.sendMessage(locale.getString(CommandLang.FAIL_USERNAME_NOT_VALID));
                    return;
                }

                if (!database.check().isPlayerRegistered(uuid)) {
                    sender.sendMessage(locale.getString(CommandLang.FAIL_USERNAME_NOT_KNOWN));
                    return;
                }

                checkWebUserAndNotify(sender);
                processing.submit(processorFactory.inspectCacheRequestProcessor(uuid, sender, playerName, this::sendInspectMsg));
            } catch (DBOpException e) {
                sender.sendMessage("§eDatabase exception occurred: " + e.getMessage());
                errorHandler.log(L.ERROR, this.getClass(), e);
            }
        });
    }

    private void checkWebUserAndNotify(ISender sender) {
        if (CommandUtils.isPlayer(sender) && webServer.isAuthRequired()) {
            boolean senderHasWebUser = database.check().doesWebUserExists(sender.getName());

            if (!senderHasWebUser) {
                sender.sendMessage("§e" + locale.getString(CommandLang.NO_WEB_USER_NOTIFY));
            }
        }
    }

    private void sendInspectMsg(ISender sender, String playerName) {
        sender.sendMessage(locale.getString(CommandLang.HEADER_INSPECT, playerName));

        String url = connectionSystem.getMainAddress() + "/player/" + playerName;
        String linkPrefix = locale.getString(CommandLang.LINK_PREFIX);

        boolean console = !CommandUtils.isPlayer(sender);
        if (console) {
            sender.sendMessage(linkPrefix + url);
        } else {
            sender.sendMessage(linkPrefix);
            sender.sendLink("   ", locale.getString(CommandLang.LINK_CLICK_ME), url);
        }

        sender.sendMessage(">");
    }
}