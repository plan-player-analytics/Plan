package com.djrapitops.plan.command.commands;

//import com.djrapitops.plan.Phrase;
import com.djrapitops.plan.Plan;
import com.djrapitops.plan.PlanCommand;
import com.djrapitops.plan.command.CommandType;
import com.djrapitops.plan.command.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HelpCommand extends SubCommand {

    private final Plan plugin;
    private final PlanCommand command;

    public HelpCommand(Plan plugin, PlanCommand command) {
        super("help,?", "plan.?", "Show command list.", CommandType.CONSOLE);

        this.plugin = plugin;
        this.command = command;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {

        ChatColor operatorColor = ChatColor.DARK_GREEN;

        ChatColor textColor = ChatColor.GRAY;

        sender.sendMessage(textColor + "-- [" + operatorColor + "PLAN - Player Analytics" + textColor + "] --");

        for (SubCommand command : this.command.getCommands()) {
            if (command.getName().equalsIgnoreCase(getName())) {
                continue;
            }

            if (!sender.hasPermission(command.getPermission())) {
                continue;
            }

            if (!(sender instanceof Player) && command.getCommandType() == CommandType.PLAYER) {
                continue;
            }

            sender.sendMessage(operatorColor + "/plan " + command.getFirstName() + textColor + " - " + command.getUsage());
        }

        return true;
    }

}
