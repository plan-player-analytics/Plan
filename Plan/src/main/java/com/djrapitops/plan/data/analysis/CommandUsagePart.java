/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.java.com.djrapitops.plan.data.analysis;

import main.java.com.djrapitops.plan.utilities.html.HtmlUtils;
import main.java.com.djrapitops.plan.utilities.html.tables.CommandUseTableCreator;

import java.util.HashMap;
import java.util.Map;

/**
 * Part responsible for all CommandUsage related analysis.
 * <p>
 * Placeholder values can be retrieved using the get method.
 * <p>
 * Contains following placeholders after analyzed:
 * ${commandCount} - (Number)
 * ${commandUniqueCount} - (Number)
 * ${tableBodyCommands} - Table body for CommandUsage table.
 *
 * @author Rsl1122
 * @since 3.5.2
 */
public class CommandUsagePart extends RawData {

    private Map<String, Integer> commandUsage;

    public CommandUsagePart() {
        this.commandUsage = new HashMap<>();
    }

    @Override
    public void analyse() {
        addValue("commandUniqueCount", String.valueOf(getUniqueCommands()));
        addValue("commandCount", String.valueOf(getCommandTotal()));
        String commandUsageTable = CommandUseTableCreator.createTable(commandUsage);
        addValue("tableBodyCommands", HtmlUtils.removeXSS(commandUsageTable));
    }

    public void setCommandUsage(Map<String, Integer> commandUsage) {
        this.commandUsage = commandUsage;
    }

    public void addCommands(Map<String, Integer> commandUsage) {
        this.commandUsage.putAll(commandUsage);
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
