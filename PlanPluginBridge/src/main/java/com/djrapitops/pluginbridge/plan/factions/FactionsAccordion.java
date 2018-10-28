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
package com.djrapitops.pluginbridge.plan.factions;

import com.djrapitops.plan.data.store.keys.PlayerKeys;
import com.djrapitops.plan.data.store.mutators.PlayersMutator;
import com.djrapitops.plan.data.store.mutators.SessionsMutator;
import com.djrapitops.plan.utilities.formatting.Formatter;
import com.djrapitops.plan.utilities.html.HtmlStructure;
import com.djrapitops.plan.utilities.html.icon.Color;
import com.djrapitops.plan.utilities.html.icon.Family;
import com.djrapitops.plan.utilities.html.icon.Icon;
import com.djrapitops.plan.utilities.html.icon.Icons;
import com.djrapitops.plan.utilities.html.structure.Accordion;
import com.djrapitops.plan.utilities.html.structure.AccordionElement;
import com.djrapitops.plan.utilities.html.structure.AccordionElementContentBuilder;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.MPlayer;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Utility for creating Factions Accordion Html.
 *
 * @author Rsl1122
 */
class FactionsAccordion extends Accordion {

    private final List<Faction> factions;
    private final PlayersMutator playersMutator;

    private final Formatter<Long> timestampFormatter;
    private final Formatter<Double> decimalFormatter;

    FactionsAccordion(
            List<Faction> factions,
            PlayersMutator playersMutator,
            Formatter<Long> timestampFormatter,
            Formatter<Double> decimalFormatter
    ) {
        super("faction_accordion");
        this.factions = factions;
        this.playersMutator = playersMutator;
        this.timestampFormatter = timestampFormatter;
        this.decimalFormatter = decimalFormatter;

        addElements();
    }

    private void addElements() {
        for (Faction faction : factions) {
            String factionName = faction.getName();
            long createdAtMillis = faction.getCreatedAtMillis();
            String created = timestampFormatter.apply(createdAtMillis);
            double power = faction.getPower();
            double maxPower = faction.getPowerMax();
            String powerString = decimalFormatter.apply(power) + " / " + decimalFormatter.apply(maxPower);
            MPlayer leader = faction.getLeader();
            String leaderName = leader != null ? leader.getName() : "No Leader";

            int landCount = faction.getLandCount();

            Set<UUID> members = new HashSet<>();
            List<MPlayer> mPlayers = faction.getMPlayers();
            int memberCount = mPlayers.size();
            for (MPlayer mPlayer : mPlayers) {
                if (mPlayer == null) {
                    continue;
                }
                members.add(mPlayer.getUuid());
            }

            PlayersMutator memberMutator = this.playersMutator.filterBy(
                    player -> player.getValue(PlayerKeys.UUID)
                            .map(members::contains).orElse(false)
            );

            SessionsMutator memberSessionsMutator = new SessionsMutator(memberMutator.getSessions());

            long playerKills = memberSessionsMutator.toPlayerKillCount();
            long deaths = memberSessionsMutator.toDeathCount();

            String separated = HtmlStructure.separateWithDots(("Power: " + powerString), leaderName);

            String htmlID = "faction_" + factionName + "_" + createdAtMillis;

            String leftSide = new AccordionElementContentBuilder()
                    .addRowBold(Icon.called("calendar").of(Color.DEEP_PURPLE).of(Family.REGULAR), "Created", created)
                    .addRowBold(Icon.called("bolt").of(Color.PURPLE), "Power", powerString)
                    .addRowBold(Icon.called("user").of(Color.PURPLE), "Leader", leaderName)
                    .addRowBold(Icon.called("users").of(Color.PURPLE), "Members", memberCount)
                    .addRowBold(Icon.called("map").of(Color.PURPLE), "Land Count", landCount)
                    .toHtml();

            String rightSide = new AccordionElementContentBuilder()
                    .addRowBold(Icons.PLAYER_KILLS, "Player Kills", playerKills)
                    .addRowBold(Icons.DEATHS, "Deaths", deaths)
                    .toHtml();

            addElement(
                    new AccordionElement(htmlID, factionName + "<span class=\"pull-right\">" + separated + "</span>")
                            .setColor("deep-purple")
                            .setLeftSide(leftSide)
                            .setRightSide(rightSide)
            );
        }
    }
}