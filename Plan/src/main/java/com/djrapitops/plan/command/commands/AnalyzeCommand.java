package main.java.com.djrapitops.plan.command.commands;

import com.djrapitops.plugin.api.TimeAmount;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.CommandUtils;
import com.djrapitops.plugin.command.ISender;
import com.djrapitops.plugin.command.SubCommand;
import com.djrapitops.plugin.task.AbsRunnable;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Permissions;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.command.ConditionUtils;
import main.java.com.djrapitops.plan.locale.Locale;
import main.java.com.djrapitops.plan.locale.Msg;
import main.java.com.djrapitops.plan.systems.info.InformationManager;
import main.java.com.djrapitops.plan.utilities.Check;
import main.java.com.djrapitops.plan.utilities.MiscUtils;
import main.java.com.djrapitops.plan.utilities.html.HtmlUtils;
import org.bukkit.ChatColor;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

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
                Locale.get(Msg.CMD_USG_ANALYZE).parse());
        this.plugin = plugin;
        infoManager = plugin.getInfoManager();
    }

    public static void sendAnalysisMessage(Collection<ISender> senders) {
        for (ISender sender : senders) {
            sender.sendMessage(Locale.get(Msg.CMD_HEADER_ANALYZE).toString());
            // Link
            String url = HtmlUtils.getServerAnalysisUrlWithProtocol();
            String message = Locale.get(Msg.CMD_INFO_LINK).toString();
            boolean console = !CommandUtils.isPlayer(sender);
            if (console) {
                sender.sendMessage(message + url);
            } else {
                sender.sendMessage(message);
                sender.sendLink("   ", Locale.get(Msg.CMD_INFO_CLICK_ME).toString(), url);
            }
            sender.sendMessage(Locale.get(Msg.CMD_CONSTANT_FOOTER).toString());
        }
    }

    @Override
    public String[] addHelp() {
        return Locale.get(Msg.CMD_HELP_ANALYZE).toArray();
    }

    @Override
    public boolean onCommand(ISender sender, String commandLabel, String[] args) {
        if (!Check.isTrue(ConditionUtils.pluginHasViewCapability(), Locale.get(Msg.CMD_FAIL_NO_DATA_VIEW).toString(), sender)) {
            return true;
        }

        Optional<Long> analysisRefreshDate = infoManager.getAnalysisRefreshDate();
        boolean forcedRefresh = args.length >= 1 && "-r".equals(args[0]);
        boolean refresh = !analysisRefreshDate.isPresent()
                || analysisRefreshDate.get() < MiscUtils.getTime() - TimeAmount.MINUTE.ms()
                || forcedRefresh;
        if (refresh) {
            updateCache(sender, refresh);
        }

        sender.sendMessage(Locale.get(Msg.CMD_INFO_FETCH_DATA).toString());
        if (plugin.getWebServer().isAuthRequired() && CommandUtils.isPlayer(sender)) {
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

    private void updateCache(ISender sender, boolean refresh) {
        if (refresh) {
            int bootAnID = plugin.getBootAnalysisTaskID();
            if (bootAnID != -1) {
                plugin.getServer().getScheduler().cancelTask(bootAnID);
            }
            infoManager.addAnalysisNotification(sender);
            infoManager.refreshAnalysis();
        } else {
            sendAnalysisMessage(Collections.singletonList(sender));
        }
    }
}
