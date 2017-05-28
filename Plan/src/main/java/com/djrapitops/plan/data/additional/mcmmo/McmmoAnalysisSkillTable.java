package main.java.com.djrapitops.plan.data.additional.mcmmo;

import com.gmail.nossr50.datatypes.player.PlayerProfile;
import com.gmail.nossr50.datatypes.skills.SkillType;
import com.gmail.nossr50.util.player.UserManager;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import main.java.com.djrapitops.plan.data.additional.AnalysisType;
import main.java.com.djrapitops.plan.data.additional.PluginData;
import main.java.com.djrapitops.plan.ui.Html;
import main.java.com.djrapitops.plan.utilities.FormatUtils;
import main.java.com.djrapitops.plan.utilities.MathUtils;
import org.apache.commons.lang.StringUtils;
import static org.bukkit.Bukkit.getOfflinePlayers;

/**
 * PluginData class for McMMO-plugin.
 *
 * Registered to the plugin by McmmoHook
 *
 * @author Rsl1122
 * @since 3.2.1
 * @see McmmoHook
 */
public class McmmoAnalysisSkillTable extends PluginData {

    public McmmoAnalysisSkillTable() {
        super("McMMO", "analysistable", AnalysisType.HTML);
        final String skill = Html.FONT_AWESOME_ICON.parse("star") + " Skill";
        final String tLevel = Html.FONT_AWESOME_ICON.parse("plus") + " Total Level";
        final String aLevel = Html.FONT_AWESOME_ICON.parse("plus") + " Average Level";
        final String notice = "Only online players shown. " + Html.LINK_EXTERNAL.parse("https://github.com/mcMMO-Dev/mcMMO/blob/master/src/main/java/com/gmail/nossr50/util/player/UserManager.java#L105", "More info") + "<br>";
        super.setPrefix(notice + Html.TABLE_START_3.parse(skill, tLevel, aLevel));
        super.setSuffix(Html.TABLE_END.parse());
    }

    @Override
    public String getHtmlReplaceValue(String modifierPrefix, UUID uuid) {
        List<PlayerProfile> profiles = Arrays.stream(getOfflinePlayers())
                .filter(p -> p != null)
                .map(p -> UserManager.getOfflinePlayer(p))
                .filter(u -> u != null)
                .map(u -> u.getProfile())
                .collect(Collectors.toList());
        if (profiles.isEmpty()) {
            return parseContainer("", Html.TABLELINE_3.parse("No players online", "", ""));
        }

        final List<SkillType> skills = new ArrayList<>();
        skills.addAll(Arrays.stream(SkillType.values()).distinct().collect(Collectors.toList()));

        final StringBuilder html = new StringBuilder();
        for (SkillType skill : skills) {
            long total = MathUtils.sumInt(profiles.stream().map(p -> (Serializable) p.getSkillLevel(skill)));
            html.append(Html.TABLELINE_3.parse(StringUtils.capitalize(skill.getName().toLowerCase()), "" + total, FormatUtils.cutDecimals(MathUtils.average((int) total, profiles.size()))));
        }
        return parseContainer("", html.toString());
    }

    @Override
    public Serializable getValue(UUID uuid) {
        return -1;
    }
}
