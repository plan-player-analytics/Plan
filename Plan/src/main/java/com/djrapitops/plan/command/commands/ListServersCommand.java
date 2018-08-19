package com.djrapitops.plan.command.commands;

import com.djrapitops.plan.api.exceptions.database.DBOpException;
import com.djrapitops.plan.data.store.mutators.formatting.Formatter;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.info.server.Server;
import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.locale.lang.CmdHelpLang;
import com.djrapitops.plan.system.locale.lang.CommandLang;
import com.djrapitops.plan.system.locale.lang.DeepHelpLang;
import com.djrapitops.plan.system.settings.Permissions;
import com.djrapitops.plugin.command.ColorScheme;
import com.djrapitops.plugin.command.CommandNode;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.ISender;
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.logging.error.ErrorHandler;

import javax.inject.Inject;
import java.util.List;

/**
 * This SubCommand is used to list all servers found in the database.
 *
 * @author Rsl1122
 */
public class ListServersCommand extends CommandNode {

    private final Locale locale;
    private final ColorScheme colorScheme;
    private final Database database;
    private final ErrorHandler errorHandler;

    @Inject
    public ListServersCommand(Locale locale, ColorScheme colorScheme, Database database, ErrorHandler errorHandler) {
        super("servers|serverlist|listservers|sl|ls", Permissions.MANAGE.getPermission(), CommandType.CONSOLE);

        this.locale = locale;
        this.colorScheme = colorScheme;
        this.database = database;
        this.errorHandler = errorHandler;

        setShortHelp(locale.getString(CmdHelpLang.SERVERS));
        setInDepthHelp(locale.getArray(DeepHelpLang.SERVERS));
    }

    @Override
    public void onCommand(ISender sender, String commandLabel, String[] args) {
        String sCol = colorScheme.getSecondaryColor();
        String tCol = colorScheme.getTertiaryColor();
        Formatter<Server> serverFormatter = serverLister(sCol, tCol);
        try {
            sender.sendMessage(locale.getString(CommandLang.HEADER_SERVERS));
            sendServers(sender, serverFormatter);
            sender.sendMessage(">");
        } catch (DBOpException e) {
            sender.sendMessage("§cDatabase Exception occurred.");
            errorHandler.log(L.WARN, this.getClass(), e);
        }
    }

    private void sendServers(ISender sender, Formatter<Server> serverFormatter) {
        List<Server> servers = database.fetch().getServers();
        for (Server server : servers) {
            sender.sendMessage(serverFormatter.apply(server));
        }
    }

    private Formatter<Server> serverLister(String tertiaryColor, String secondaryColor) {
        return server -> "  " + tertiaryColor + server.getId() + secondaryColor + " : " + server.getName() + " : " + server.getWebAddress();
    }

}
