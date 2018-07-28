/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.settings.theme;

/**
 * Enum class used for getting the Html colors that match the config settings.
 *
 * @author Rsl1122
 */
public enum ThemeVal {

    THEME_DEFAULT("DefaultColor", "light-green"),

    FONT_STYLESHEET("Font.FontStyleSheet", "https://fonts.googleapis.com/css?family=Roboto:400,700&subset=latin,cyrillic-ext"),
    FONT_FAMILY("Font.FontFamily", "\"Roboto\", sans-serif"),

    RED("Colors.red", "#E91E63"),
    PINK("Colors.pink", "#F44336"),
    PURPLE("Colors.purple", "#9C27B0"),
    DEEP_PURPLE("Colors.deep-purple", "#673AB7"),
    INDIGO("Colors.indigo", "#3F51B5"),
    BLUE("Colors.blue", "#2196F3"),
    LIGHT_BLUE("Colors.light-blue", "#03A9F4"),
    CYAN("Colors.cyan", "#00BCD4"),
    TEAL("Colors.teal", "#009688"),
    GREEN("Colors.green", "#4CAF50"),
    LIGHT_GREEN("Colors.light-green", "#8BC34A"),
    LIME("Colors.lime", "#CDDC39"),
    YELLOW("Colors.yellow", "#ffe821"),
    AMBER("Colors.amber", "#FFC107"),
    ORANGE("Colors.orange", "#FF9800"),
    DEEP_ORANGE("Colors.deep-orange", "#FF5722"),
    BROWN("Colors.brown", "#795548"),
    GREY("Colors.grey", "#9E9E9E"),
    BLUE_GREY("Colors.blue-grey", "#607D8B"),
    BLACK("Colors.black", "#000000"),
    WHITE("Colors.Extra.White", "#fff"),

    GRAPH_PUNCHCARD("GraphColors.PunchCard", "#222"),
    GRAPH_PLAYERS_ONLINE("GraphColors.PlayersOnline", "#1E90FF"),
    GRAPH_TPS_HIGH("GraphColors.TPS.High", "#267F00"),
    GRAPH_TPS_MED("GraphColors.TPS.Medium", "#e5cc12"),
    GRAPH_TPS_LOW("GraphColors.TPS.Low", "#b74343"),
    GRAPH_CPU("GraphColors.CPU", "#e0d264"),
    GRAPH_RAM("GraphColors.RAM", "#7dcc24"),
    GRAPH_CHUNKS("GraphColors.Chunks", "#b58310"),
    GRAPH_ENTITIES("GraphColors.Entities", "#ac69ef"),
    GRAPH_WORLD_PIE("GraphColors.WorldPie", "\"#0099C6\", \"#66AA00\", \"#316395\", \"#994499\", \"#22AA99\", \"#AAAA11\", \"#6633CC\", \"#E67300\", \"#329262\", \"#5574A6\""),
    GRAPH_GM_PIE("GraphColors.GMDrilldown", "\"#438c99\", \"#639A67\", \"#D8EBB5\", \"#D9BF77\""),
    GRAPH_ACTIVITY_PIE("GraphColors.ActivityPie", "\"#4CAF50\", \"#8BC34A\", \"#CDDC39\", \"#FFC107\", \"#607D8B\""),
    GRAPH_SERVER_PREF_PIE("GraphColors.ServerPreferencePie", "\"#0099C6\", \"#66AA00\", \"#316395\", \"#994499\", \"#22AA99\", \"#AAAA11\", \"#6633CC\", \"#E67300\", \"#329262\", \"#5574A6\""),
    GRAPH_AVG_PING("GraphColors.Ping.Avg", "#ffc107"),
    GRAPH_MAX_PING("GraphColors.Ping.Max", "#ffa000"),
    GRAPH_MIN_PING("GraphColors.Ping.Min", "#ffd54f"),
    WORLD_MAP_HIGH("GraphColors.WorldMap_High", "#267f00"),
    WORLD_MAP_LOW("GraphColors.WorldMap_Low", "#EEFFEE"),

    PARSED_SESSION_ACCORDION("ParsedElements.SessionAccordion", "teal"),
    PARSED_SERVER_ACCORDION("ParsedElements.ServerAccordion", "light-green");

    private final String themePath;
    private final String defaultValue;

    ThemeVal(String themePath, String defaultValue) {
        this.themePath = themePath;
        this.defaultValue = defaultValue;
    }

    public String getThemePath() {
        return themePath;
    }

    public String getDefaultValue() {
        return defaultValue;
    }
}
