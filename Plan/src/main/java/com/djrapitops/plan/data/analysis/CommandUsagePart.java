/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.java.com.djrapitops.plan.data.analysis;

import main.java.com.djrapitops.plan.ui.html.tables.CommandUseTableCreator;
import main.java.com.djrapitops.plan.utilities.HtmlUtils;

import java.util.Map;

/**
 * Part responsible for all CommandUsage related analysis.
 * <p>
 * Command Usage Table.
 * <p>
 * Placeholder values can be retrieved using the get method.
 * <p>
 * Contains following place-holders: uniquecommands, totalcommands, commanduse
 *
 * @author Rsl1122
 * @since 3.5.2
 */
public class CommandUsagePart extends RawData<CommandUsagePart> {

    private final Map<String, Integer> commandUsage;

    public CommandUsagePart(Map<String, Integer> commandUsage) {
        this.commandUsage = commandUsage;
    }

    @Override
    public void analyse() {
        addValue("uniquecommands", getUniqueCommands() + "");
        addValue("totalcommands", getCommandTotal() + "");
        String commandUsageTable = CommandUseTableCreator.createSortedCommandUseTable(commandUsage);
        addValue("commanduse", HtmlUtils.removeXSS(commandUsageTable));
    }

    public int getUniqueCommands() {
        return commandUsage.keySet().size();
    }

    public long getCommandTotal() {
        return commandUsage.values().stream().mapToLong(i -> i).sum();
    }

    public Map<String, Integer> getCommandUsage() {
        return commandUsage;
    }
}
