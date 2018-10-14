package com.djrapitops.pluginbridge.plan.kingdoms;

import com.djrapitops.plan.data.store.keys.PlayerKeys;
import com.djrapitops.plan.data.store.mutators.PlayersMutator;
import com.djrapitops.plan.data.store.mutators.SessionsMutator;
import com.djrapitops.plan.utilities.html.HtmlStructure;
import com.djrapitops.plan.utilities.html.icon.Color;
import com.djrapitops.plan.utilities.html.icon.Icon;
import com.djrapitops.plan.utilities.html.icon.Icons;
import com.djrapitops.plan.utilities.html.structure.Accordion;
import com.djrapitops.plan.utilities.html.structure.AccordionElement;
import com.djrapitops.plan.utilities.html.structure.AccordionElementContentBuilder;
import org.kingdoms.constants.kingdom.OfflineKingdom;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Utility for creating Kingdoms Accordion Html.
 *
 * @author Rsl1122
 */
class KingdomsAccordion extends Accordion {

    private final Map<String, OfflineKingdom> kingdoms;
    private final PlayersMutator playersMutator;

    KingdomsAccordion(Map<String, OfflineKingdom> kingdoms, PlayersMutator playersMutator) {
        super("kingdoms_accordion");
        this.kingdoms = kingdoms;
        this.playersMutator = playersMutator;

        addElements();
    }

    private void addElements() {
        for (Map.Entry<String, OfflineKingdom> entry : kingdoms.entrySet()) {
            String kingdomName = entry.getKey();

            OfflineKingdom kingdom = entry.getValue();
            String kingName = kingdom.getKingName();
            String kingdomLore = kingdom.getKingdomLore();

            int might = kingdom.getMight();
            int resourcePoints = kingdom.getResourcepoints();

            List<UUID> members = kingdom.getMembersList();
            int memberCount = members.size();

            PlayersMutator memberMutator = this.playersMutator.filterBy(
                    player -> player.getValue(PlayerKeys.UUID)
                            .map(members::contains).orElse(false)
            );

            SessionsMutator memberSessionsMutator = new SessionsMutator(memberMutator.getSessions());

            long playerKills = memberSessionsMutator.toPlayerKillCount();
            long mobKills = memberSessionsMutator.toMobKillCount();
            long deaths = memberSessionsMutator.toDeathCount();

            String separated = HtmlStructure.separateWithDots(("Members: " + memberCount), kingName);

            String htmlID = "kingdom_" + kingdomName.replace(" ", "-");

            String leftSide = new AccordionElementContentBuilder()
                    .addHtml(kingdomLore != null ? "<p>" + kingdomLore + "</p>" : "")
                    .addRowBold(Icon.called("chess-king").of(Color.AMBER), "King", kingName)
                    .addRowBold(Icon.called("users").of(Color.AMBER), "Members", memberCount)
                    .addRowBold(Icon.called("bolt").of(Color.AMBER), "Might", might)
                    .addRowBold(Icon.called("cubes").of(Color.AMBER), "Resources", resourcePoints)
                    .toHtml();

            String rightSide = new AccordionElementContentBuilder()
                    .addRowBold(Icons.PLAYER_KILLS, "Player Kills", playerKills)
                    .addRowBold(Icons.MOB_KILLS, "Mob Kills", mobKills)
                    .addRowBold(Icons.DEATHS, "Deaths", deaths)
                    .toHtml();

            addElement(new AccordionElement(htmlID, kingdomName + "<span class=\"pull-right\">" + separated + "</span>")
                    .setColor("amber")
                    .setLeftSide(leftSide)
                    .setRightSide(rightSide));

        }
    }
}