/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.pluginbridge.plan.mcmmo;

import com.gmail.nossr50.database.DatabaseManager;
import com.gmail.nossr50.datatypes.player.McMMOPlayer;
import com.gmail.nossr50.datatypes.player.PlayerProfile;
import com.gmail.nossr50.datatypes.skills.SkillType;
import com.gmail.nossr50.mcMMO;
import com.gmail.nossr50.util.player.UserManager;
import com.djrapitops.plan.data.element.AnalysisContainer;
import com.djrapitops.plan.data.element.InspectContainer;
import com.djrapitops.plan.data.element.TableContainer;
import com.djrapitops.plan.data.plugin.ContainerSize;
import com.djrapitops.plan.data.plugin.PluginData;
import com.djrapitops.plan.utilities.FormatUtils;
import com.djrapitops.plan.utilities.analysis.MathUtils;
import com.djrapitops.plan.utilities.html.Html;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import static org.bukkit.Bukkit.getOnlinePlayers;

/**
 * PluginData for McMmo plugin.
 *
 * @author Rsl1122
 */
public class McMmoData extends PluginData {

    public McMmoData() {
        super(ContainerSize.THIRD, "MCMMO");
        super.setIconColor("indigo");
        super.setPluginIcon("compass");
    }

    @Override
    public InspectContainer getPlayerData(UUID uuid, InspectContainer inspectContainer) throws Exception {
        DatabaseManager db = mcMMO.getDatabaseManager();

        PlayerProfile profile = db.loadPlayerProfile(uuid);

        String skillS = Html.FONT_AWESOME_ICON.parse("star") + " Skill";
        String levelS = Html.FONT_AWESOME_ICON.parse("plus") + " Level";
        TableContainer skillTable = new TableContainer(skillS, levelS);
        skillTable.setColor("indigo");

        List<SkillType> skills = new ArrayList<>();
        skills.addAll(Arrays.stream(SkillType.values()).distinct().collect(Collectors.toList()));
        for (SkillType skill : skills) {
            skillTable.addRow(StringUtils.capitalize(skill.getName().toLowerCase()), profile.getSkillLevel(skill));
        }

        return inspectContainer;
    }

    @Override
    public AnalysisContainer getServerData(Collection<UUID> collection, AnalysisContainer analysisContainer) throws Exception {
        String skillS = Html.FONT_AWESOME_ICON.parse("star") + " Skill";
        String tLevel = Html.FONT_AWESOME_ICON.parse("plus") + " Total Level";
        String aLevel = Html.FONT_AWESOME_ICON.parse("plus") + " Average Level";

        analysisContainer.addValue("Only Online Players Shown", "Skills available on Inspect pages.");

        TableContainer skillTable = new TableContainer(skillS, tLevel, aLevel);
        skillTable.setColor("indigo");

        List<PlayerProfile> profiles = getOnlinePlayers().stream()
                .filter(Objects::nonNull)
                .map(UserManager::getOfflinePlayer)
                .filter(Objects::nonNull)
                .map(McMMOPlayer::getProfile)
                .collect(Collectors.toList());
        if (profiles.isEmpty()) {
            skillTable.addRow("No players online");
        }

        List<SkillType> skills = Arrays.stream(SkillType.values()).distinct().collect(Collectors.toList());

        for (SkillType skill : skills) {
            long total = MathUtils.sumInt(profiles.stream().map(p -> (Serializable) p.getSkillLevel(skill)));
            skillTable.addRow(
                    StringUtils.capitalize(skill.getName().toLowerCase()),
                    Long.toString(total),
                    FormatUtils.cutDecimals(MathUtils.average((int) total, profiles.size()))
            );
        }

        return analysisContainer;
    }
}