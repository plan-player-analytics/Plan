/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.pluginbridge.plan;

import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plugin.logging.error.ErrorHandler;
import com.djrapitops.pluginbridge.plan.aac.AdvancedAntiCheatHook;
import com.djrapitops.pluginbridge.plan.advancedachievements.AdvancedAchievementsHook;
import com.djrapitops.pluginbridge.plan.advancedban.AdvancedBanHook;
import com.djrapitops.pluginbridge.plan.askyblock.ASkyBlockHook;
import com.djrapitops.pluginbridge.plan.banmanager.BanManagerHook;
import com.djrapitops.pluginbridge.plan.buycraft.BuyCraftHook;
import com.djrapitops.pluginbridge.plan.discordsrv.DiscordSRVHook;
import com.djrapitops.pluginbridge.plan.essentials.EssentialsHook;
import com.djrapitops.pluginbridge.plan.factions.FactionsHook;
import com.djrapitops.pluginbridge.plan.griefprevention.GriefPreventionHook;
import com.djrapitops.pluginbridge.plan.griefprevention.plus.GriefPreventionPlusHook;
import com.djrapitops.pluginbridge.plan.jobs.JobsHook;
import com.djrapitops.pluginbridge.plan.kingdoms.KingdomsHook;
import com.djrapitops.pluginbridge.plan.litebans.LiteBansBukkitHook;
import com.djrapitops.pluginbridge.plan.luckperms.LuckPermsHook;
import com.djrapitops.pluginbridge.plan.mcmmo.McmmoHook;
import com.djrapitops.pluginbridge.plan.protocolsupport.ProtocolSupportHook;
import com.djrapitops.pluginbridge.plan.redprotect.RedProtectHook;
import com.djrapitops.pluginbridge.plan.superbvote.SuperbVoteHook;
import com.djrapitops.pluginbridge.plan.towny.TownyHook;
import com.djrapitops.pluginbridge.plan.vault.VaultHook;
import com.djrapitops.pluginbridge.plan.viaversion.ViaVersionBukkitHook;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Plugin bridge for Bukkit plugins.
 *
 * @author Rsl1122
 */
@Singleton
public class BukkitBridge extends AbstractBridge {

    private final AdvancedAntiCheatHook advancedAntiCheatHook;
    private final AdvancedAchievementsHook advancedAchievementsHook;
    private final AdvancedBanHook advancedBanHook;
    private final ASkyBlockHook aSkyBlockHook;
    private final BanManagerHook banManagerHook;
    private final BuyCraftHook buyCraftHook;
    private final DiscordSRVHook discordSRVHook;
    private final EssentialsHook essentialsHook;
    private final FactionsHook factionsHook;
    private final GriefPreventionHook griefPreventionHook;
    private final GriefPreventionPlusHook griefPreventionPlusHook;
    private final JobsHook jobsHook;
    private final KingdomsHook kingdomsHook;
    private final LiteBansBukkitHook liteBansHook;
    private final LuckPermsHook luckPermsHook;
    private final McmmoHook mcmmoHook;
//    private final PlaceholderAPIHook placeholderAPIHook;
private final ProtocolSupportHook protocolSupportHook;
    private final RedProtectHook redProtectHook;
    private final SuperbVoteHook superbVoteHook;
    private final TownyHook townyHook;
    private final VaultHook vaultHook;
    private final ViaVersionBukkitHook viaVersionHook;

    @Inject
    public BukkitBridge(
            PlanConfig config,
            ErrorHandler errorHandler,

            AdvancedAntiCheatHook advancedAntiCheatHook,
            AdvancedAchievementsHook advancedAchievementsHook,
            AdvancedBanHook advancedBanHook,
            ASkyBlockHook aSkyBlockHook,
            BanManagerHook banManagerHook,
            BuyCraftHook buyCraftHook,
            DiscordSRVHook discordSRVHook,
            EssentialsHook essentialsHook,
            FactionsHook factionsHook,
            GriefPreventionHook griefPreventionHook,
            GriefPreventionPlusHook griefPreventionPlusHook,
            JobsHook jobsHook,
            KingdomsHook kingdomsHook,
            LiteBansBukkitHook liteBansHook,
            LuckPermsHook luckPermsHook,
            McmmoHook mcmmoHook,
//            PlaceholderAPIHook placeholderAPIHook,
            ProtocolSupportHook protocolSupportHook,
            RedProtectHook redProtectHook,
            SuperbVoteHook superbVoteHook,
            TownyHook townyHook,
            VaultHook vaultHook,
            ViaVersionBukkitHook viaVersionHook
    ) {
        super(config, errorHandler);
        this.advancedAntiCheatHook = advancedAntiCheatHook;
        this.advancedAchievementsHook = advancedAchievementsHook;
        this.advancedBanHook = advancedBanHook;
        this.aSkyBlockHook = aSkyBlockHook;
        this.banManagerHook = banManagerHook;
        this.buyCraftHook = buyCraftHook;
        this.discordSRVHook = discordSRVHook;
        this.essentialsHook = essentialsHook;
        this.factionsHook = factionsHook;
        this.griefPreventionHook = griefPreventionHook;
        this.griefPreventionPlusHook = griefPreventionPlusHook;
        this.jobsHook = jobsHook;
        this.kingdomsHook = kingdomsHook;
        this.liteBansHook = liteBansHook;
        this.luckPermsHook = luckPermsHook;
        this.mcmmoHook = mcmmoHook;
//        this.placeholderAPIHook = placeholderAPIHook;
        this.protocolSupportHook = protocolSupportHook;
        this.redProtectHook = redProtectHook;
        this.superbVoteHook = superbVoteHook;
        this.townyHook = townyHook;
        this.vaultHook = vaultHook;
        this.viaVersionHook = viaVersionHook;
    }

    @Override
    Hook[] getHooks() {
        return new Hook[]{
                advancedAntiCheatHook,
                advancedAchievementsHook,
                advancedBanHook,
                aSkyBlockHook,
                banManagerHook,
                buyCraftHook,
                discordSRVHook,
                essentialsHook,
                factionsHook,
                griefPreventionHook,
                griefPreventionPlusHook,
                jobsHook,
                kingdomsHook,
                liteBansHook,
                luckPermsHook,
                mcmmoHook,
//                placeholderAPIHook,
                protocolSupportHook,
                // new ReactHook(),
                redProtectHook,
                superbVoteHook,
                townyHook,
                vaultHook,
                viaVersionHook
        };
    }
}