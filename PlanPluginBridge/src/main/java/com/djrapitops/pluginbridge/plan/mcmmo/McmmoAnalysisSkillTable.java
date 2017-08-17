package com.djrapitops.pluginbridge.plan.mcmmo;

import com.gmail.nossr50.datatypes.player.McMMOPlayer;
import com.gmail.nossr50.datatypes.player.PlayerProfile;
import com.gmail.nossr50.datatypes.skills.SkillType;
import com.gmail.nossr50.util.player.UserManager;
import main.java.com.djrapitops.plan.data.additional.AnalysisType;
import main.java.com.djrapitops.plan.data.additional.PluginData;
import main.java.com.djrapitops.plan.ui.html.Html;
import main.java.com.djrapitops.plan.utilities.FormatUtils;
import main.java.com.djrapitops.plan.utilities.analysis.MathUtils;
import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import static org.bukkit.Bukkit.getOnlinePlayers;

/**
 * PluginData class for McMMO-plugin.
 * <p>
 * Registered to the plugin by McmmoHook
 *
 * @author Rsl1122
 * @see McmmoHook
 * @since 3.2.1
 */
public class McmmoAnalysisSkillTable extends PluginData {

    /**
     * Class Constructor, sets the parameters of the PluginData object.
     */
    public McmmoAnalysisSkillTable() {
        super("McMMO", "analysis_table", AnalysisType.HTML);
        final String skill = Html.FONT_AWESOME_ICON.parse("star") + " Skill";
        final String tLevel = Html.FONT_AWESOME_ICON.parse("plus") + " Total Level";
        final String aLevel = Html.FONT_AWESOME_ICON.parse("plus") + " Average Level";
        final String notice = "Only online players shown. " + Html.LINK_EXTERNAL.parse("https://github.com/mcMMO-Dev/mcMMO/blob/master/src/main/java/com/gmail/nossr50/util/player/UserManager.java#L105", "More info") + "<br>";
        super.setPrefix(notice + Html.TABLE_START_3.parse(skill, tLevel, aLevel));
        super.setSuffix(Html.TABLE_END.parse());
    }

    @Override
    public String getHtmlReplaceValue(String modifierPrefix, UUID uuid) {
        List<PlayerProfile> profiles = getOnlinePlayers().stream()
                .filter(Objects::nonNull)
                .map(UserManager::getOfflinePlayer)
                .filter(Objects::nonNull)
                .map(McMMOPlayer::getProfile)
                .collect(Collectors.toList());
        if (profiles.isEmpty()) {
            return parseContainer("", Html.TABLELINE_3.parse("No players online", "", ""));
        }

        final List<SkillType> skills = new ArrayList<>();
        skills.addAll(Arrays.stream(SkillType.values()).distinct().collect(Collectors.toList()));

        final StringBuilder html = new StringBuilder();
        for (SkillType skill : skills) {
            long total = MathUtils.sumInt(profiles.stream().map(p -> (Serializable) p.getSkillLevel(skill)));
            html.append(Html.TABLELINE_3.parse(
                    StringUtils.capitalize(skill.getName().toLowerCase()),
                    Long.toString(total),
                    FormatUtils.cutDecimals(MathUtils.average((int) total, profiles.size()))
            ));
        }
        return parseContainer("", html.toString());
    }

    @Override
    public Serializable getValue(UUID uuid) {
        return -1;
    }
}
