/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.java.com.djrapitops.plan;

import org.bukkit.command.CommandSender;

/**
 *
 * @author Risto
 */
public enum Permissions {

    HELP("plan.?"),
    INSPECT("plan.inspect"),
    QUICK_INSPECT("plan.qinspect"),
    QUICK_INSPECT_OTHER("plan.qinspect.other"),
    INSPECT_OTHER("plan.inspect.other"),
    ANALYZE("plan.analyze"),
    QUICK_ANALYZE("plan.qanalyze"),
    SEARCH("plan.search"),
    RELOAD("plan.reload"),
    INFO("plan.info"),
    IGNORE_COMMANDUSE("plan.ignore.commanduse"),
    MANAGE("plan.manage");

    private final String permission;

    private Permissions(String permission) {
        this.permission = permission;
    }

    public boolean userHasThisPermission(CommandSender p) {
        return p.hasPermission(permission);
    }
    
    public String getPermission() {
        return permission;
    }
}
