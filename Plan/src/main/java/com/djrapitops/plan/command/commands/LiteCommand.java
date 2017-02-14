package main.java.com.djrapitops.plan.command.commands;

import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.PlanLiteHook;
import main.java.com.djrapitops.plan.command.CommandType;
import main.java.com.djrapitops.plan.command.SubCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

/**
 *
 * @author Rsl1122
 */
public class LiteCommand extends SubCommand {

    private Plan plugin;
    private PlanLiteHook hook;

    /**
     * Subcommand Constructor.
     *
     * @param plugin Current instance of Plan
     */
    public LiteCommand(Plan plugin) {
        super("lite", "plan.?", "Use PlanLite Commands", CommandType.CONSOLE, "<planlite command>");

        this.plugin = plugin;
        this.hook = plugin.getPlanLiteHook();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (hook.isEnabled()) {
            return hook.passCommand(sender, cmd, commandLabel, args);
        }
        return false;
    }
}
