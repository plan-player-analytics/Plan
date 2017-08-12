package main.java.com.djrapitops.plan.ui.html;

import com.djrapitops.plugin.utilities.Verify;

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
    FONT_AWESOME_ICON("<i class=\"fa fa-REPLACE0\" aria-hidden=\"true\"></i>"),
    MINOTAR_SMALL_IMG("<img style=\"float: left; padding: 2px 2px 0px 2px\" alt=\"REPLACE0\" src=\"https://minotar.net/avatar/REPLACE0/19\">"),
    SPAN("REPLACE0</span>"),
    BUTTON("<a class=\"button\" href=\"REPLACE0\">REPLACE1</a>"),
    BUTTON_CLASS("class=\"button\""),
    LINK("<a class=\"link\" href=\"REPLACE0\">REPLACE1</a>"),
    LINK_EXTERNAL("<a class=\"link\" target=\"_blank\" href=\"REPLACE0\">REPLACE1</a>"),
    LINK_CLASS("class=\"link\""),
    IMG("<img src=\"REPLACE0\">"),
    COLUMNS_DIV_WRAPPER("<div class=\"columns\">REPLACE0</div>"),
    COLUMN_DIV_WRAPPER("<div class=\"about box column\">REPLACE0</div>"),
    HEADER("<div class=\"headerbox\" style=\"width: 95%;\"><div class=\"header-icon\"><div class=\"header-label\"><i class=\"fa fa-cube\" aria-hidden=\"true\"></i><span class=\"header-text\"> REPLACE0</span></div></div></div>"),
    PLUGIN_DATA_WRAPPER("<div class=\"plugin-data\">REPLACE0</div>"),
    PLUGIN_CONTAINER_START("<div class=\"plugin-container\">"),
    //
    TABLE_START_2("<table class=\"sortable table\"><thead><tr><th>REPLACE0</th><th>REPLACE1</th></tr></thead><tbody>"),
    TABLE_START_3("<table class=\"sortable table\"><thead><tr><th>REPLACE0</th><th>REPLACE1</th><th>REPLACE2</th></tr></thead><tbody>"),
    TABLE_START_4("<table class=\"sortable table\"><thead><tr><th>REPLACE0</th><th>REPLACE1</th><th>REPLACE2</th><th>REPLACE3</th></tr></thead><tbody>"),
    TABLE_SESSIONS_START(TABLE_START_3.parse("Session Started", "Session Ended", "Session Length")),
    TABLE_KILLS_START(TABLE_START_3.parse("Date", "Killed", "With")),
    TABLE_FACTIONS_START(TABLE_START_4.parse(FONT_AWESOME_ICON.parse("flag") + " Faction", FONT_AWESOME_ICON.parse("bolt") + " Power", FONT_AWESOME_ICON.parse("map-o") + " Land", FONT_AWESOME_ICON.parse("user") + " Leader")),
    TABLE_TOWNS_START(TABLE_START_4.parse(FONT_AWESOME_ICON.parse("bank") + " Town", FONT_AWESOME_ICON.parse("users") + " Residents", FONT_AWESOME_ICON.parse("map-o") + " Land", FONT_AWESOME_ICON.parse("user") + " Mayor")),
    TABLELINE_2("<tr><td><b>REPLACE0</b></td><td>REPLACE1</td></tr>"),
    TABLELINE_3("<tr><td><b>REPLACE0</b></td><td>REPLACE1</td><td>REPLACE2</td></tr>"),
    TABLELINE_4("<tr><td><b>REPLACE0</b></td><td>REPLACE1</td><td>REPLACE2</td><td>REPLACE3</td></tr>"),
    TABLELINE_PLAYERS("<tr><td>REPLACE0</td><td>REPLACE1</td><td sorttable_customkey=\"REPLACE2\">REPLACE3</td><td>REPLACE4</td><td sorttable_customkey=\"REPLACE5\">REPLACE6</td>" + "<td sorttable_customkey=\"REPLACE7\">REPLACE8</td><td>REPLACE9</td></tr>"),
    TABLELINE_3_CUSTOMKEY("<tr><td sorttable_customkey=\"REPLACE0\">REPLACE1</td><td sorttable_customkey=\"REPLACE2\">REPLACE3</td><td sorttable_customkey=\"REPLACE4\">REPLACE5</td></tr>"),
    TABLELINE_3_CUSTOMKEY_1("<tr><td sorttable_customkey=\"REPLACE0\">REPLACE1</td><td>REPLACE2</td><td>REPLACE3</td></tr>"),
    ERROR_TABLE_2(TABLELINE_2.parse("No data", "No data")),
    TABLE_END("</tbody></table>"), //    KILLDATA_NONE("No Kills"),
    ;

    private String html;

    Html(String html) {
        this.html = html;
    }

    /**
     * @return
     */
    public String parse() {
        return html;
    }

    /**
     * @param p
     * @return
     */
    public String parse(String... p) {
        Verify.nullCheck(p);
        String returnValue = this.html;
        for (int i = 0; i < p.length; i++) {
            returnValue = returnValue.replace("REPLACE" + i, p[i]);
        }
        return returnValue;
    }

    /**
     * @param html
     */
    public void setHtml(String html) {
        this.html = html;
    }
}
