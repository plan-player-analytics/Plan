/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.djrapitops.pluginbridge.plan;

import com.djrapitops.pluginbridge.plan.advancedachievements.AdvancedAchievementsHook;
import com.djrapitops.pluginbridge.plan.askyblock.ASkyBlockHook;
import com.djrapitops.pluginbridge.plan.essentials.EssentialsHook;
import com.djrapitops.pluginbridge.plan.factions.FactionsHook;
import com.djrapitops.pluginbridge.plan.jobs.JobsHook;
import com.djrapitops.pluginbridge.plan.mcmmo.McmmoHook;
import com.djrapitops.pluginbridge.plan.ontime.OnTimeHook;
import com.djrapitops.pluginbridge.plan.towny.TownyHook;
import com.djrapitops.pluginbridge.plan.vault.VaultHook;
import main.java.com.djrapitops.plan.Settings;
import main.java.com.djrapitops.plan.data.additional.HookHandler;

/**
 *
 * @author Rsl1122
 */
public class Bridge {

    public static void hook(HookHandler handler) {
        try {
            if (Settings.ENABLED_AA.isTrue()) {
                AdvancedAchievementsHook advancedAchievementsHook = new AdvancedAchievementsHook(handler);
            }
        } catch (NoClassDefFoundError e) {
        }
        try {
            if (Settings.ENABLED_ESS.isTrue()) {
                EssentialsHook essentialsHook = new EssentialsHook(handler);
            }
        } catch (NoClassDefFoundError e) {
        }
        try {
            if (Settings.ENABLED_FAC.isTrue()) {
                FactionsHook factionsHook = new FactionsHook(handler);
            }
        } catch (NoClassDefFoundError e) {
        }
        try {
            if (Settings.ENABLED_MCM.isTrue()) {
                McmmoHook mcMmoHook = new McmmoHook(handler);
            }
        } catch (NoClassDefFoundError e) {
        }
        try {
            if (Settings.ENABLED_JOB.isTrue()) {
                JobsHook jobsHook = new JobsHook(handler);
            }
        } catch (NoClassDefFoundError e) {
        }
        try {
            if (Settings.ENABLED_ONT.isTrue()) {
                OnTimeHook onTimeHook = new OnTimeHook(handler);
            }
        } catch (NoClassDefFoundError e) {
        }
        try {
            if (Settings.ENABLED_TOW.isTrue()) {
                TownyHook townyHook = new TownyHook(handler);
            }
        } catch (NoClassDefFoundError e) {
        }
        try {
            if (Settings.ENABLED_VAU.isTrue()) {
                VaultHook vaultHook = new VaultHook(handler);
            }
        } catch (NoClassDefFoundError e) {
        }
        try {
            ASkyBlockHook askyblockHook = new ASkyBlockHook(handler);
        } catch (NoClassDefFoundError e) {
        }
    }
}
