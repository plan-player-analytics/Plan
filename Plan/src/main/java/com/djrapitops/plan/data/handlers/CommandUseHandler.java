package com.djrapitops.plan.data.handlers;

import java.util.HashMap;

/**
 *
 * @author Rsl1122
 */
public class CommandUseHandler {

    private HashMap<String, Integer> commandUse;

    /**
     * Class constructor.
     *
     * @param serverData ServerData in the DataCacheHandler.
     */
    public CommandUseHandler(HashMap<String, Integer> serverData) {
        this.commandUse = serverData;
    }

    /**
     * Adds command to the command usage.
     * @param command Used command, first part (eg. /plan)
     */
    public void handleCommand(String command) {
        if (!commandUse.containsKey(command)) {
            commandUse.put(command, 0);
        }
        commandUse.put(command, commandUse.get(command) + 1);
    }
}
