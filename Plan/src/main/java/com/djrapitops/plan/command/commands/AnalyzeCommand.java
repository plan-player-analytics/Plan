package main.java.com.djrapitops.plan.command.commands;

import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.CommandUtils;
import com.djrapitops.plugin.command.ISender;
import com.djrapitops.plugin.command.SubCommand;
import com.djrapitops.plugin.task.AbsRunnable;
import com.djrapitops.plugin.utilities.Verify;
import main.java.com.djrapitops.plan.Permissions;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.locale.Locale;
import main.java.com.djrapitops.plan.locale.Msg;
import main.java.com.djrapitops.plan.systems.info.InformationManager;
import main.java.com.djrapitops.plan.systems.info.server.ServerInfo;
import org.bukkit.ChatColor;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * This subcommand is used to run the analysis and access the /server link.
 *
 * @author Rsl1122
 * @since 2.0.0
 */
public class AnalyzeCommand extends SubCommand {

    private final Plan plugin;
    private final InformationManager infoManager;

    /**
     * Subcommand Constructor.
     *
     * @param plugin Current instance of Plan
     */
    public AnalyzeCommand(Plan plugin) {
        super("analyze, analyse, analysis, a",
                CommandType.CONSOLE,
                Permissions.ANALYZE.getPermission(),
                Locale.get(Msg.CMD_USG_ANALYZE).parse(),
                "[ServerName or ID]");
        this.plugin = plugin;
        infoManager = plugin.getInfoManager();
    }

    public static void sendAnalysisMessage(Collection<ISender> senders, UUID serverUUID) throws SQLException {
        if (Verify.isEmpty(senders)) {
            return;
        }
        Plan plugin = Plan.getInstance();
        Optional<String> serverName = plugin.getDB().getServerTable().getServerName(serverUUID);
        serverName.ifPresent(name -> {
            String target = "/server/" + name;
            String url = plugin.getInfoManager().getLinkTo(target);
            String message = Locale.get(Msg.CMD_INFO_LINK).toString();

            for (ISender sender : senders) {
                sender.sendMessage(Locale.get(Msg.CMD_HEADER_ANALYZE).toString());
                // Link
                boolean console = !CommandUtils.isPlayer(sender);
                if (console) {
                    sender.sendMessage(message + url);
                } else {
                    sender.sendMessage(message);
                    sender.sendLink("   ", Locale.get(Msg.CMD_INFO_CLICK_ME).toString(), url);
                }
                sender.sendMessage(Locale.get(Msg.CMD_CONSTANT_FOOTER).toString());
            }
        });
    }

    @Override
    public String[] addHelp() {
        return Locale.get(Msg.CMD_HELP_ANALYZE).toArray();
    }

    @Override
    public boolean onCommand(ISender sender, String commandLabel, String[] args) {

        // TODO Write a command for listing servers.

        UUID serverUUID = Plan.getServerUUID();
        if (args.length >= 1 && plugin.getInfoManager().isUsingAnotherWebServer()) {
            try {
                List<ServerInfo> bukkitServers = plugin.getDB().getServerTable().getBukkitServers();
                Optional<ServerInfo> server = bukkitServers.stream().filter(info -> {
                    StringBuilder idBuilder = new StringBuilder(args[0]);
                    if (args.length > 1) {
                        for (int i = 1; i < args.length; i++) {
                            idBuilder.append(" ").append(args[i]);
                        }
                    }
                    String serverIdentifier = idBuilder.toString();
                    return Integer.toString(info.getId()).equals(serverIdentifier) || info.getName().equalsIgnoreCase(serverIdentifier);
                }).findFirst();
                if (server.isPresent()) {
                    serverUUID = server.get().getUuid();
                }
            } catch (SQLException e) {
                Log.toLog(this.getClass().getName(), e);
                return true;
            }
        }

        updateCache(sender, serverUUID);

        sender.sendMessage(Locale.get(Msg.CMD_INFO_FETCH_DATA).toString());
        if (plugin.getInfoManager().isAuthRequired() && CommandUtils.isPlayer(sender)) {
            plugin.getRunnableFactory().createNew(new AbsRunnable("WebUser exist check task") {
                @Override
                public void run() {
                    try {
                        boolean senderHasWebUser = plugin.getDB().getSecurityTable().userExists(sender.getName());
                        if (!senderHasWebUser) {
                            sender.sendMessage(ChatColor.YELLOW + "[Plan] You might not have a web user, use /plan register <password>");
                        }
                    } catch (Exception e) {
                        Log.toLog(this.getClass().getName() + getName(), e);
                    } finally {
                        this.cancel();
                    }
                }
            }).runTaskAsynchronously();
        }
        return true;
    }

    private void updateCache(ISender sender, UUID serverUUID) {
        int bootAnID = plugin.getBootAnalysisTaskID();
        if (bootAnID != -1) {
            plugin.getServer().getScheduler().cancelTask(bootAnID);
        }
        infoManager.addAnalysisNotification(sender, serverUUID);
        infoManager.refreshAnalysis(serverUUID);
    }
}
