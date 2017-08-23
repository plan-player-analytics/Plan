package main.java.com.djrapitops.plan.command.commands;

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
import main.java.com.djrapitops.plan.utilities.Check;
import org.bukkit.ChatColor;

/**
 * This subcommand is used to run the analysis and access the /server link.
 *
 * @author Rsl1122
 * @since 2.0.0
 */
public class AnalyzeCommand extends SubCommand {

    private final Plan plugin;

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

        // TODO Check if analysis is enabled.
//        if (!Check.isTrue(analysisCache.isAnalysisEnabled(), Locale.get(Msg.CMD_INFO_ANALYSIS_TEMP_DISABLE).toString(), sender)
//                && !analysisCache.isCached()) {
//            return true;
//        }

        sender.sendMessage(Locale.get(Msg.CMD_INFO_FETCH_DATA).toString());
        if (plugin.getUiServer().isAuthRequired() && CommandUtils.isPlayer(sender)) {
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
        updateCache(sender);
        return true;
    }

    private void updateCache(ISender sender) {
        // TODO
//        if (!analysisCache.isCached() || MiscUtils.getTime() - analysisCache.getData().getRefreshDate() > TimeAmount.MINUTE.ms()) {
//            int bootAnID = plugin.getBootAnalysisTaskID();
//            if (bootAnID != -1) {
//                plugin.getServer().getScheduler().cancelTask(bootAnID);
//            }
//            analysisCache.addNotification(sender);
//            analysisCache.updateCache();
//        } else {
//            analysisCache.sendAnalysisMessage(sender);
//        }
    }
}
