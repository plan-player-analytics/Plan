/*
*    Player Analytics Bukkit plugin for monitoring server activity.
*    Copyright (C) 2016  Risto Lahtela / Rsl1122
*
*    This program is free software: you can redistribute it and/or modify
*    it under the terms of the Plan License. (licence.yml)
*    Modified software can only be redistributed if allowed in the licence.
*
*    This program is distributed in the hope that it will be useful,
*    but WITHOUT ANY WARRANTY; without even the implied warranty of
*    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*    License for more details.
*
*    You should have received a copy of the License
*    along with this program. 
*    If not it should be visible on the distribution page.
*    Or here
*    https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/licence.yml
 */
package com.djrapitops.plandebugger;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.utilities.FormatUtils;
import com.djrapitops.plandebugger.config.ConfigSetter;
import com.djrapitops.plandebugger.config.SettingsList;
import com.djrapitops.plandebugger.tests.IndependentTestRunner;
import com.djrapitops.plandebugger.tests.PluginTestRunner;
import com.djrapitops.plandebugger.tests.TestRunner;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.List;
import main.java.com.djrapitops.plan.Settings;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Rsl1122
 */
public class PlanDebugger extends JavaPlugin {

    private Plan plan;
    private ConfigSetter configSetter;
    private String originalDB;
    private String debugTime;

    @Override
    public void onEnable() {
        plan = getPlugin(Plan.class);
        runConsoleCommand("plan manage backup mysql");
        runConsoleCommand("plan manage backup sqlite");
        originalDB = plan.getConfig().getString(Settings.DB_TYPE.getPath());
        configSetter = new ConfigSetter(this, plan);
        debugTime = FormatUtils.formatTimeStamp(new Date().getTime()+"").replaceAll(" ", "");
        int[] r = new int[]{0, 0, 0};
        TestRunner iTestRunner = new IndependentTestRunner(this, plan);
        List<String> errors = iTestRunner.runAllTests();
        r[0] = iTestRunner.getTestsRun();
        r[1] = iTestRunner.getTestsFailed();
        r[2] = iTestRunner.getTestsError();
        TestRunner testRunner = new PluginTestRunner(this, plan);
        for (SettingsList s : SettingsList.values()) {
            configSetter.setSettings(s);
            errors.addAll(testRunner.runAllTests());
        }
        log("\nTEST RESULTS - Run: "+r[0]+" Success: "+(r[0]-r[1]-r[2])+" Failed: "+r[1]+" Errors: "+r[2]+"\n");
        log("ERRORS:");
        for (String error : errors) {
            logError("Error: "+error);
        }
    }

    @Override
    public void onDisable() {
        plan.getConfig().set(Settings.DB_TYPE.getPath(), originalDB);
        configSetter.resetSettings();
        log("Tests Complete!");
        log("Restore the old database by using the restore command.");
    }

    public void log(String message) {
        getLogger().info(message);
        toLog(message);
    }

    public void logError(String message) {
        getLogger().severe(message);
        toLog(message);
    }

    public void toLog(String message) {
        File folder = getDataFolder();
        if (!folder.exists()) {
            folder.mkdir();
        }
        File log = new File(getDataFolder(), "DebugLog-" + debugTime + ".txt");
        try {
            if (!log.exists()) {
                log.createNewFile();
            }
            FileWriter fw = new FileWriter(log, true);
            try (PrintWriter pw = new PrintWriter(fw)) {
                String timestamp = FormatUtils.formatTimeStamp(new Date().getTime()+"");
                pw.println("["+timestamp+"] "+message + "\n");
                pw.flush();
            }
        } catch (IOException e) {
            logError("Failed to create log.txt file");
        }
    }
    
    public void runConsoleCommand(String command) {
        getServer().dispatchCommand(getServer().getConsoleSender(), command);
    }
}
