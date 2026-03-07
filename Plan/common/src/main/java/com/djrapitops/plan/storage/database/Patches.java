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
package com.djrapitops.plan.storage.database;

import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.storage.database.transactions.init.RemoveIncorrectTebexPackageDataPatch;
import com.djrapitops.plan.storage.database.transactions.patches.*;
import net.playeranalytics.plugin.server.PluginLogger;

/**
 * Stores the patch list in a separate class so that they're easy to add to.
 *
 * @author AuroraLS3
 */
public class Patches {

    private Patches() {
        // Static utility class
    }

    public static Patch[] getAll(PluginLogger logger, PlanConfig config) {
        return new Patch[]{
                new Version10Patch(),
                new GeoInfoLastUsedPatch(),
                new SessionAFKTimePatch(),
                new KillsServerIDPatch(),
                new WorldTimesSeverIDPatch(),
                new WorldsServerIDPatch(),
                new NicknameLastSeenPatch(),
                new VersionTableRemovalPatch(),
                new DiskUsagePatch(),
                new WorldsOptimizationPatch(),
                new KillsOptimizationPatch(),
                new NicknamesOptimizationPatch(),
                new TransferTableRemovalPatch(),
                // new BadAFKThresholdValuePatch(),
                new DeleteIPsPatch(),
                new ExtensionShowInPlayersTablePatch(),
                new ExtensionTableRowValueLengthPatch(),
                new CommandUsageTableRemovalPatch(),
                new BadNukkitRegisterValuePatch(),
                new LinkedToSecurityTablePatch(),
                new LinkUsersToPlayersSecurityTablePatch(),
                new LitebansTableHeaderPatch(),
                new UserInfoHostnamePatch(),
                new ServerIsProxyPatch(),
                new ServerTableRowPatch(),
                new PlayerTableRowPatch(),
                new ExtensionTableProviderValuesForPatch(),
                new RemoveIncorrectTebexPackageDataPatch(),
                new ExtensionTableProviderFormattersPatch(),
                new ServerPlanVersionPatch(),
                new RemoveDanglingUserDataPatch(),
                new RemoveDanglingServerDataPatch(),
                new GeoInfoOptimizationPatch(),
                new PingOptimizationPatch(),
                new UserInfoOptimizationPatch(),
                new WorldTimesOptimizationPatch(),
                new SessionsOptimizationPatch(),
                new UserInfoHostnameAllowNullPatch(),
                new RegisterDateMinimizationPatch(),
                new UsersTableNameLengthPatch(),
                new SessionJoinAddressPatch(),
                new RemoveUsernameFromAccessLogPatch(),
                new ComponentColumnToExtensionDataPatch(),
                new BadJoinAddressDataCorrectionPatch(),
                new AfterBadJoinAddressDataCorrectionPatch(),
                new CorrectWrongCharacterEncodingPatch(logger, config),
                new UpdateWebPermissionsPatch(),
                new WebGroupDefaultGroupsPatch(),
                new WebGroupAddMissingAdminGroupPatch(),
                new LegacyPermissionLevelGroupsPatch(),
                new SecurityTableGroupPatch(),
                new ExtensionStringValueLengthPatch(),
                new CookieTableIpAddressPatch(),
                new TPSTableMSPTPatch(),
                new AllowlistIncorrectUniqueConstraintPatch(),
                new TPSTableIdPatch(),
                new DeleteUrlOpenEventsFromExtensionComponentsPatch()
        };
    }
}
