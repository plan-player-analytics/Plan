package main.java.com.djrapitops.plan.ui;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import main.java.com.djrapitops.plan.Plan;
import static org.bukkit.plugin.java.JavaPlugin.getPlugin;

/**
 *
 * @author Rsl1122
 */
public enum Html {

    REPLACE0("REPLACE0"),
    REPLACE1("REPLACE1"),
    WARN_INACCURATE("<div class=\"warn\">Data might be inaccurate, player has just registered.</div>"),
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
    COLOR_a("<span class=\"green\">"),
    COLOR_b("<span class=\"aqua\">"),
    COLOR_c("<span class=\"red\">"),
    COLOR_d("<span class=\"pink\">"),
    COLOR_e("<span class=\"yellow\">"),
    COLOR_f("<span class=\"white\">"),
    SPAN("" + REPLACE0 + "</span>"),
    BUTTON("<a class=\"button\" href=\"" + REPLACE0 + "\">" + REPLACE1 + "</a>"),
    BUTTON_CLASS("class=\"button\""),
    LINK("<a class=\"link\" href=\"" + REPLACE0 + "\">" + REPLACE1 + "</a>"),
    LINK_CLASS("class=\"link\""),
    IMG("<img src=\"" + REPLACE0 + "\">"),
    TOP_TOWNS("<p><b>Top 20 Towns</b></p>"),
    TOP_FACTIONS("<p><b>Top 20 Factions</b></p>"),
    TOTAL_BALANCE("<p>Server Total Balance: " + REPLACE0 + "</p>"),
    TOTAL_VOTES("<p>Players have voted total of " + REPLACE0 + " times.</p>"),
    PLOT_OPTIONS("<p>Plot options: " + REPLACE0 + "</p>"),
    FRIENDS("<p>Friends with " + REPLACE0 + "</p>"),
    BALANCE("<p>Balance: " + REPLACE0 + "</p>"), 
    BANNED("| " + SPAN.parse(COLOR_4.parse() + "Banned")),
    OPERATOR(", Operator (Op)"),
    ONLINE("| " + SPAN.parse(COLOR_2.parse() + "Online")),
    OFFLINE("| " + SPAN.parse(COLOR_4.parse() + "Offline")),
    ACTIVE("Player is Active"),
    INACTIVE("Player is inactive"),
    ERROR_LIST("Error Creating List</p>"),
    HIDDEN("Hidden (config)"),
    ERROR_NOT_SET("Error: Replace rule was not set"),
    FACTION_NOT_FOUND("Faction not found"),
    FACTION_NO_LEADER("No leader"),
    FACTION_NO_FACTIONS("No Factions"),
    WARPS("<br/>Warps: " + REPLACE0),
    ACHIEVEMENTS("<br/>Achievements: " + REPLACE0 + "/" + REPLACE1),
    JAILED("| Jailed"),
    MUTED("| Muted"),
    VOTES("<br/>Has voted " + REPLACE0 + "times"),
    FACTION("<br/>Faction: " + REPLACE0 + " | Power: " + REPLACE1 + "/REPLACE2"),
    TOWN("<br/>Town: " + REPLACE0),
    TOWN_NO_TOWNS("No Towns"),
    GRAPH_BANNED("Banned"),
    GRAPH_UNKNOWN("Unknown"),
    GRAPH_INACTIVE("Inactive"),
    GRAPH_ACTIVE("Active"),
    GRAPH_ONLINE("Players Online"),
    GRAPH_PLAYERS("Players"),
    GRAPH_DATE("Date"),
    TABLE_START_3("<table class=\"sortable table\"><thead><tr><th>REPLACE0</th><th>REPLACE1</th><th>REPLACE2</th></tr></thead><tbody>"),
    TABLE_START_4("<table class=\"sortable table\"><thead><tr><th>REPLACE0</th><th>REPLACE1</th><th>REPLACE2</th><th>REPLACE3</th></tr></thead><tbody>"),
    TABLE_SESSIONS_START(TABLE_START_3.parse("Session Started", "Session Ended", "Session Length")),
    TABLE_KILLS_START(TABLE_START_3.parse("Date", "Killed", "With")),
    TABLE_FACTIONS_START(TABLE_START_4.parse("Faction", "Power", "Land", "Leader")),
    TABLE_TOWNS_START(TABLE_START_4.parse("Town", "Residents", "Land", "Mayor")),
    TABLELINE_2("<tr><td><b>" + REPLACE0 + "</b></td><td>" + REPLACE1 + "</td></tr>"),
    TABLELINE_3("<tr><td><b>" + REPLACE0 + "</b></td><td>" + REPLACE1 + "</td><td>REPLACE2</td></tr>"),
    TABLELINE_4("<tr><td><b>" + REPLACE0 + "</b></td><td>" + REPLACE1 + "</td><td>REPLACE2</td><td>REPLACE3</td></tr>"),
    TABLELINE_PLAYERS("<tr><td>REPLACE0</td><td>REPLACE1</td><td sorttable_customkey=\"REPLACE2\">REPLACE3</td><td>REPLACE4</td><td sorttable_customkey=\"REPLACE5\">REPLACE6</td>" + "<td sorttable_customkey=\"REPLACE7\">REPLACE8</td><td>REPLACE9</td></tr>"),
    TABLELINE_3_CUSTOMKEY("<tr><td sorttable_customkey=\"REPLACE0\">REPLACE1</td><td sorttable_customkey=\"REPLACE2\">REPLACE3</td><td sorttable_customkey=\"REPLACE4\">REPLACE5</td></tr>"),
    TABLELINE_3_CUSTOMKEY_1("<tr><td sorttable_customkey=\"REPLACE0\">REPLACE1</td><td>REPLACE2</td><td>REPLACE3</td></tr>"),
    ERROR_TABLE_2(TABLELINE_2.parse("No data", "No data")),
    TABLE_END("</tbody></table>"),
    SESSIONDATA_NONE("No Session Data available"),
    KILLDATA_NONE("No Kills"),;

    private String html;

    private Html(String html) {
        this.html = html;
    }

    public String parse() {
        return html;
    }

    public String parse(String... p) {
        String returnValue = this.html;
        for (int i = 0; i < p.length; i++) {
            returnValue = returnValue.replace("REPLACE" + i, p[i]);
        }
        return returnValue;
    }

    public void setHtml(String html) {
        this.html = html;
    }

    public static void loadLocale(File localeFile) {
        try {
            Scanner localeScanner = new Scanner(localeFile, "UTF-8");
            List<String> localeRows = new ArrayList<>();
            boolean html = false;
            while (localeScanner.hasNextLine()) {
                String line = localeScanner.nextLine();
                if (line.equals("<<<<<<HTML>>>>>>")) {
                    html = true;
                    continue;
                }
                if (!html) {
                    continue;
                }
                localeRows.add(line);
            }
            for (String localeRow : localeRows) {
                try {
                    String[] split = localeRow.split(" <> ");
                    Html.valueOf(split[0]).setHtml(split[1]);
                } catch (IllegalArgumentException e) {
                    getPlugin(Plan.class).logError("There is a miswritten line in locale on line " + localeRows.indexOf(localeRow));
                }
            }
        } catch (IOException e) {

        }
    }
}
