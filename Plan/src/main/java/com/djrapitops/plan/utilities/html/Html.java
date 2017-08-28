package main.java.com.djrapitops.plan.utilities.html;

import com.djrapitops.plugin.utilities.Verify;
import org.apache.commons.lang3.text.StrSubstitutor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Rsl1122
 */
public enum Html {

    COLOR_0("<span class=\"black\">"),
    COLOR_1("<span class=\"darkblue\">"),
    COLOR_2("<span class=\"darkgreen\">"),
    COLOR_3("<span class=\"darkaqua\">"),
    COLOR_4("<span class=\"darkred\">"),
    COLOR_5("<span class=\"darkpurple\">"),
    COLOR_6("<span class=\"gold\">"),
    COLOR_7("<span class=\"gray\">"),
    COLOR_8("<span class=\"darkgray\">"),
    COLOR_9("<span class=\"blue\">"),
    COLOR_A("<span class=\"green\">"),
    COLOR_B("<span class=\"aqua\">"),
    COLOR_C("<span class=\"red\">"),
    COLOR_D("<span class=\"pink\">"),
    COLOR_E("<span class=\"yellow\">"),
    COLOR_F("<span class=\"white\">"),
    //
    FONT_AWESOME_ICON("<i class=\"fa fa-${0}\" aria-hidden=\"true\"></i>"),
    SPAN("${0}</span>"),
    BUTTON("<a class=\"button\" href=\"${0}\">${1}</a>"),
    BUTTON_CLASS("class=\"button\""),
    LINK("<a class=\"link\" href=\"${0}\">${1}</a>"),
    LINK_EXTERNAL("<a class=\"link\" target=\"_blank\" href=\"${0}\">${1}</a>"),
    LINK_CLASS("class=\"link\""),
    IMG("<img src=\"${0}\">"),
    //
    TABLE_START_2("<table class=\"sortable table\"><thead><tr><th>${0}</th><th>${1}</th></tr></thead><tbody>"),
    TABLE_START_3("<table class=\"sortable table\"><thead><tr><th>${0}</th><th>${1}</th><th>${2}</th></tr></thead><tbody>"),
    TABLE_START_4("<table class=\"sortable table\"><thead><tr><th>${0}</th><th>${1}</th><th>${2}</th><th>${3}</th></tr></thead><tbody>"),
    TABLE_SESSIONS_START(TABLE_START_3.parse("Session Started", "Session Ended", "Session Length")),
    TABLE_KILLS_START(TABLE_START_3.parse(FONT_AWESOME_ICON.parse("clock-o") + " Time", "Killed", "With")),
    TABLE_FACTIONS_START(TABLE_START_4.parse(FONT_AWESOME_ICON.parse("flag") + " Faction", FONT_AWESOME_ICON.parse("bolt") + " Power", FONT_AWESOME_ICON.parse("map-o") + " Land", FONT_AWESOME_ICON.parse("user") + " Leader")),
    TABLE_TOWNS_START(TABLE_START_4.parse(FONT_AWESOME_ICON.parse("bank") + " Town", FONT_AWESOME_ICON.parse("users") + " Residents", FONT_AWESOME_ICON.parse("map-o") + " Land", FONT_AWESOME_ICON.parse("user") + " Mayor")),
    TABLELINE_2("<tr><td><b>${0}</b></td><td>${1}</td></tr>"),
    TABLELINE_3("<tr><td><b>${0}</b></td><td>${1}</td><td>${2}</td></tr>"),
    TABLELINE_4("<tr><td><b>${0}</b></td><td>${1}</td><td>${2}</td><td>${3}</td></tr>"),
    TABLELINE_PLAYERS("<tr><td>${0}</td><td>${1}</td><td sorttable_customkey=\"${2}\">${3}</td><td>${4}</td><td sorttable_customkey=\"${5}\">${6}</td>" + "<td sorttable_customkey=\"${7}\">${8}</td><td>${9}</td></tr>"),
    TABLELINE_3_CUSTOMKEY("<tr><td sorttable_customkey=\"${0}\">${1}</td><td sorttable_customkey=\"${2}\">${3}</td><td sorttable_customkey=\"${4}\">${5}</td></tr>"),
    TABLELINE_3_CUSTOMKEY_1("<tr><td sorttable_customkey=\"${0}\">${1}</td><td>${2}</td><td>${3}</td></tr>"),
    TABLE_END("</tbody></table>");

    private final String html;

    Html(String html) {
        this.html = html;
    }

    /**
     * @return The HTML String
     */
    public String parse() {
        return html;
    }

    /**
     * @param p The replacement Strings
     * @return The parsed HTML String
     */
    public String parse(Serializable... p) {
        Verify.nullCheck(p);

        Map<String, Serializable> replaceMap = new HashMap<>();

        for (int i = 0; i < p.length; i++) {
            replaceMap.put(String.valueOf(i), p[i].toString());
        }

        StrSubstitutor sub = new StrSubstitutor(replaceMap);

        return sub.replace(html);
    }
}
