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
package com.djrapitops.pluginbridge.plan.superbvote;

import com.djrapitops.plan.data.element.AnalysisContainer;
import com.djrapitops.plan.data.element.InspectContainer;
import com.djrapitops.plan.data.plugin.ContainerSize;
import com.djrapitops.plan.data.plugin.PluginData;
import com.djrapitops.plan.utilities.html.icon.Color;
import com.djrapitops.plan.utilities.html.icon.Icon;
import io.minimum.minecraft.superbvote.storage.VoteStorage;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * PluginData for SuperbVote plugin;
 *
 * @author Rsl1122
 */
class SuperbVoteData extends PluginData {
    private final VoteStorage store;

    SuperbVoteData(VoteStorage store) {
        super(ContainerSize.THIRD, "SuperbVote");
        setPluginIcon(Icon.called("check").of(Color.TEAL).build());
        this.store = store;
    }

    @Override
    public InspectContainer getPlayerData(UUID uuid, InspectContainer inspectContainer) {
        int votes = store.getVotes(uuid).getVotes();

        inspectContainer.addValue(getWithIcon("Votes", Icon.called("check").of(Color.TEAL)), votes);

        return inspectContainer;
    }

    @Override
    public AnalysisContainer getServerData(Collection<UUID> uuids, AnalysisContainer analysisContainer) {
        Map<UUID, Integer> votes = new HashMap<>();
        long total = 0;
        for (UUID uuid : uuids) {
            int votesCount = store.getVotes(uuid).getVotes();
            votes.put(uuid, votesCount);
            total += votesCount;
        }

        analysisContainer.addValue(getWithIcon("Total Votes", Icon.called("check").of(Color.TEAL)), total);

        analysisContainer.addPlayerTableValues(getWithIcon("Votes", Icon.called("check")), votes);

        return analysisContainer;
    }
}