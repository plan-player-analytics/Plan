/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.pluginbridge.plan.mcmmo;

import com.djrapitops.plan.data.element.AnalysisContainer;
import com.djrapitops.plan.data.element.InspectContainer;
import com.djrapitops.plan.data.element.TableContainer;
import com.djrapitops.plan.data.plugin.ContainerSize;
import com.djrapitops.plan.data.plugin.PluginData;
import com.djrapitops.plan.utilities.FormatUtils;
import com.djrapitops.plan.utilities.html.icon.Color;
import com.djrapitops.plan.utilities.html.icon.Family;
import com.djrapitops.plan.utilities.html.icon.Icon;
import com.gmail.nossr50.database.DatabaseManager;
import com.gmail.nossr50.datatypes.player.PlayerProfile;
import com.gmail.nossr50.datatypes.skills.SkillType;
import com.gmail.nossr50.mcMMO;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * PluginData for McMmo plugin.
 *
 * @author Rsl1122
 */
public class McMmoData extends PluginData {

    public McMmoData() {
        super(ContainerSize.THIRD, "MCMMO");
        setPluginIcon(Icon.called("compass").of(Color.INDIGO).of(Family.REGULAR).build());
    }

    @Override
    public InspectContainer getPlayerData(UUID uuid, InspectContainer inspectContainer) {
        DatabaseManager db = mcMMO.getDatabaseManager();

        PlayerProfile profile = db.loadPlayerProfile("", uuid, false);

        String skillS = getWithIcon("Skill", Icon.called("star"));
        String levelS = getWithIcon("Level", Icon.called("plus"));
        TableContainer skillTable = new TableContainer(skillS, levelS);
        skillTable.setColor("indigo");

        List<SkillType> skills = Arrays.stream(SkillType.values()).distinct().collect(Collectors.toList());
        for (SkillType skill : skills) {
            skillTable.addRow(StringUtils.capitalize(skill.getName().toLowerCase()), profile.getSkillLevel(skill));
        }

        return inspectContainer;
    }

    @Override
    public AnalysisContainer getServerData(Collection<UUID> uuids, AnalysisContainer analysisContainer) {
        String skillS = getWithIcon("Skill", Icon.called("star"));
        String tLevel = getWithIcon("Total Level", Icon.called("plus"));
        String aLevel = getWithIcon("Average Level", Icon.called("plus"));

        DatabaseManager databaseManager = mcMMO.getDatabaseManager();

        TableContainer skillTable = new TableContainer(skillS, tLevel, aLevel);
        skillTable.setColor("indigo");

        List<PlayerProfile> profiles = uuids.stream()
                .map(uuid -> databaseManager.loadPlayerProfile("", uuid, false))
                .filter(Objects::nonNull)
                .filter(PlayerProfile::isLoaded)
                .collect(Collectors.toList());
        if (profiles.isEmpty()) {
            skillTable.addRow("No players");
        }

        List<SkillType> skills = Arrays.stream(SkillType.values()).distinct().collect(Collectors.toList());

        for (SkillType skill : skills) {
            long total = profiles.stream().mapToInt(p -> p.getSkillLevel(skill)).sum();
            skillTable.addRow(
                    StringUtils.capitalize(skill.getName().toLowerCase()),
                    total,
                    FormatUtils.cutDecimals(total * 1.0 / profiles.size())
            );
        }

        return analysisContainer;
    }
}