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
package com.djrapitops.pluginbridge.plan.kingdoms;

import com.djrapitops.plan.api.PlanAPI;
import com.djrapitops.plan.data.element.AnalysisContainer;
import com.djrapitops.plan.data.element.InspectContainer;
import com.djrapitops.plan.data.plugin.ContainerSize;
import com.djrapitops.plan.data.plugin.PluginData;
import com.djrapitops.plan.data.store.keys.AnalysisKeys;
import com.djrapitops.plan.data.store.mutators.PlayersMutator;
import com.djrapitops.plan.utilities.html.Html;
import com.djrapitops.plan.utilities.html.icon.Color;
import com.djrapitops.plan.utilities.html.icon.Family;
import com.djrapitops.plan.utilities.html.icon.Icon;
import org.kingdoms.constants.kingdom.OfflineKingdom;
import org.kingdoms.constants.player.OfflineKingdomPlayer;
import org.kingdoms.manager.game.GameManagement;

import java.util.*;

/**
 * PluginData for Kingdoms and Kingdoms+ plugins.
 *
 * @author Rsl1122
 */
class KingdomsData extends PluginData {

    KingdomsData() {
        super(ContainerSize.TAB, "Kingdoms");
        setPluginIcon(Icon.called("crown").of(Color.AMBER).build());
    }

    @Override
    public InspectContainer getPlayerData(UUID uuid, InspectContainer inspectContainer) {
        OfflineKingdomPlayer kingdomPlayer = GameManagement.getPlayerManager().getOfflineKingdomPlayer(uuid);
        String kingdomName = kingdomPlayer.getKingdomName();

        if (kingdomName == null) {
            inspectContainer.addValue(getWithIcon("Kingdom", Icon.called("fort-awesome").of(Family.BRAND).of(Color.AMBER)), "No Kingdom");
        } else {
            OfflineKingdom kingdom = GameManagement.getKingdomManager().getOfflineKingdom(kingdomName);
            if (kingdom != null) {
                String king = kingdom.getKingName();
                String link = Html.LINK.parse(PlanAPI.getInstance().getPlayerInspectPageLink(king), king);
                inspectContainer.addValue(getWithIcon("Kingdom", Icon.called("fort-awesome").of(Family.BRAND).of(Color.AMBER)), kingdomName);
                inspectContainer.addValue(getWithIcon("King", Icon.called("chess-king").of(Color.AMBER)), link);
            }
        }

        return inspectContainer;
    }

    @Override
    public AnalysisContainer getServerData(Collection<UUID> collection, AnalysisContainer analysisContainer) {
        Map<String, OfflineKingdom> kingdoms = GameManagement.getKingdomManager().getKingdomList();

        analysisContainer.addValue(getWithIcon("Number of Kingdoms", Icon.called("fort-awesome").of(Family.BRAND).of(Color.AMBER)), kingdoms.size());

        if (!kingdoms.isEmpty()) {
            KingdomsAccordion kingdomsAccordion = new KingdomsAccordion(
                    kingdoms,
                    Optional.ofNullable(analysisData).flatMap(c -> c.getValue(AnalysisKeys.PLAYERS_MUTATOR))
                            .orElse(new PlayersMutator(new ArrayList<>()))
            );

            analysisContainer.addHtml("kingdomsAccordion", kingdomsAccordion.toHtml());

            Map<UUID, String> userKingDoms = new HashMap<>();
            for (Map.Entry<String, OfflineKingdom> entry : kingdoms.entrySet()) {
                String kingdomName = entry.getKey();
                OfflineKingdom kingdom = entry.getValue();
                UUID king = kingdom.getKing();
                for (UUID member : kingdom.getMembersList()) {
                    if (member.equals(king)) {
                        userKingDoms.put(member, "<b>" + kingdomName + "</b>");

                    } else {
                        userKingDoms.put(member, kingdomName);
                    }
                }
            }
            analysisContainer.addPlayerTableValues(getWithIcon("Kingdom", Icon.called("fort-awesome").of(Family.BRAND)), userKingDoms);
        }

        return analysisContainer;
    }
}