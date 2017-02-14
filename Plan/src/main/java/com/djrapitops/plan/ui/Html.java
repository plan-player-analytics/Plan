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
    LINK_CLASS("class=\"link\""),
    TABLE_START("<table class=\"table\">"),
    TABLE_END("</table>"),
    TABLELINE("<tr class=\"tableline\"><td><b>" + REPLACE0 + "</b></td>\r\n<td>" + REPLACE1 + "</td></tr>"),
    ERROR_TABLE("<p class=\"red\">Error Calcuclating Table (No data)</p>"),
    IMG("<img src=\"" + REPLACE0 + "\">"),
    TOP_TOWNS("<p><b>Top 20 Towns</b></p>"),
    TOP_FACTIONS("<p><b>Top 20 Factions</b></p>"),
    TOTAL_BALANCE("<p>Server Total Balance: " + REPLACE0 + "</p>"),
    TOTAL_VOTES("<p>Players have voted total of " + REPLACE0 + " times.</p>"),
    TOWN("<p>Town: " + REPLACE0 + "</p>"),
    PLOT_OPTIONS("<p>Plot options: " + REPLACE0 + "</p>"),
    FRIENDS("<p>Friends with " + REPLACE0 + "</p>"),
    FACTION("<p>Faction: " + REPLACE0 + "</p>"),
    BALANCE("<p>Balance: " + REPLACE0 + "</p>"),
    VOTES("<p>Player has voted " + REPLACE0 + " times.</p>"),
    BANNED("| " + SPAN.parse(COLOR_4.parse() + "Banned")),
    OPERATOR(", Operator (Op)"),
    ONLINE("| " + SPAN.parse(COLOR_2.parse() + "Online")),
    OFFLINE("| " + SPAN.parse(COLOR_4.parse() + "Offline")),
    ACTIVE("| Player is Active"),
    INACTIVE("| Player is inactive"),
    ERROR_LIST("Error Creating List</p>"),
    HIDDEN("Hidden (config)");

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
            returnValue = returnValue.replaceAll("REPLACE" + i, p[i]);
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
