/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.pluginbridge.plan.kingdoms;

import com.djrapitops.plan.api.PlanAPI;
import com.djrapitops.plan.data.element.AnalysisContainer;
import com.djrapitops.plan.data.element.InspectContainer;
import com.djrapitops.plan.data.plugin.ContainerSize;
import com.djrapitops.plan.data.plugin.PluginData;
import com.djrapitops.plan.utilities.html.Html;
import org.kingdoms.constants.kingdom.OfflineKingdom;
import org.kingdoms.constants.player.OfflineKingdomPlayer;
import org.kingdoms.manager.game.GameManagement;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * PluginData for Kingdoms and Kingdoms+ plugins.
 *
 * @author Rsl1122
 */
public class KingdomsData extends PluginData {

    public KingdomsData() {
        super(ContainerSize.TAB, "Kingdoms");
        super.setIconColor("amber");
        super.setPluginIcon("shield");
    }

    @Override
    public InspectContainer getPlayerData(UUID uuid, InspectContainer inspectContainer) {
        OfflineKingdomPlayer kingdomPlayer = GameManagement.getPlayerManager().getOfflineKingdomPlayer(uuid);
        String kingdomName = kingdomPlayer.getKingdomName();

        if (kingdomName == null) {
            inspectContainer.addValue(getWithIcon("Kingdom", "shield", "amber"), "No Kingdom");
        } else {
            OfflineKingdom kingdom = GameManagement.getKingdomManager().getOfflineKingdom(kingdomName);
            if (kingdom != null) {
                String king = kingdom.getKingName();
                String link = Html.LINK.parse(PlanAPI.getInstance().getPlayerInspectPageLink(king), king);
                inspectContainer.addValue(getWithIcon("Kingdom", "shield", "amber"), kingdomName);
                inspectContainer.addValue(getWithIcon("King", "user", "amber"), link);
            }
        }

        return inspectContainer;
    }

    @Override
    public AnalysisContainer getServerData(Collection<UUID> collection, AnalysisContainer analysisContainer) {
        Map<String, OfflineKingdom> kingdoms = GameManagement.getKingdomManager().getKingdomList();

        analysisContainer.addValue(getWithIcon("Number of Kingdoms", "shield", "amber"), kingdoms.size());

        if (!kingdoms.isEmpty()) {
            analysisContainer.addHtml("kingdomsAccordion", KingdomAccordionCreator.createAccordion(kingdoms));

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
            analysisContainer.addPlayerTableValues(getWithIcon("Kingdom", "shield"), userKingDoms);
        }

        return analysisContainer;
    }
}