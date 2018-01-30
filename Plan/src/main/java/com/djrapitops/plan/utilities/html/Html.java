package com.djrapitops.plan.utilities.html;

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
    FONT_AWESOME_ICON("<i class=\"fa fa-${0}\"></i>"),
    FA_COLORED_ICON("<i class=\"col-${0} fa fa-${1}\"></i>"),
    GREEN_THUMB("<i class=\"fa fa-thumbs-up g\"></i>"),
    YELLOW_FLAG("<i class=\"fa fa-flag o\"></i>"),
    RED_WARN("<i class=\"fa fa-exclamation-circle r\"></i>"),
    SPAN("${0}</span>"),
    BUTTON("<a class=\"button\" href=\"${0}\">${1}</a>"),
    BUTTON_CLASS("class=\"button\""),
    LINK("<a class=\"link\" href=\"${0}\">${1}</a>"),
    LINK_A("<a href=\"${0}\">${1}</a>"),
    LINK_TOOLTIP("<a title=\"${2}\" class=\"link\" href=\"${0}\">${1}</a>"),
    LINK_EXTERNAL("<a class=\"link\" target=\"_blank\" href=\"${0}\">${1}</a>"),
    LINK_CLASS("class=\"link\""),
    IMG("<img src=\"${0}\">"),
    //
    PARAGRAPH("<p>${0}</p>"),
    HEADER("<h1>${0}</h1>"),
    HEADER_2("<h2>${0}</h2>"),
    //
    DIV_W_CLASS("<div class=\"${0}\">${1}</div>"),
    DIV_W_CLASS_STYLE("<div class=\"${0}\" style=\"${1}\">${2}</div>"),
    //
    ROW("<div class=\"row\">${0}</div>"),
    CARD("<div class=\"card\">${0}</div>"),
    BODY("<div class=\"body\">${0}</div>"),
    PANEL("<div class=\"panel panel-default\">${0}</div>"),
    PANEL_BODY("<div class=\"panel-body\">${0}</div>"),

    //
    TABLE_END("</tbody></table>"),
    TABLE("<table class=\"table table-striped\">"),
    TABLE_SCROLL("<table class=\"table table-striped scrollbar\">"),
    TABLE_COLORED("<table class=\"bg-${0} table table-striped\">"),
    TABLE_HEAD("<thead>${0}</thead>"),
    TABLE_BODY("<tbody>${0}</tbody>"),
    TABLE_START_2("<table class=\"table table-striped\"><thead><tr><th>${0}</th><th>${1}</th></tr></thead><tbody>"),
    TABLE_START_3("<table class=\"table table-striped\"><thead><tr><th>${0}</th><th>${1}</th><th>${2}</th></tr></thead><tbody>"),
    TABLE_START_4("<table class=\"table table-striped\"><thead><tr><th>${0}</th><th>${1}</th><th>${2}</th><th>${3}</th></tr></thead><tbody>"),
    TABLE_SESSIONS(DIV_W_CLASS_STYLE.parse("box-footer scrollbar", "padding: 2px;",
            TABLE_START_4.parse("Player", "Started", "Length", "World - Time") + "${3}" + TABLE_END.parse())
    ),
    TABLE_PLAYERS_FOOTER("<table class=\"table table-bordered table-striped table-hover player-table dataTable\"><thead><tr>" +
            "<th><i class=\"fa fa-user\"></i> Name</th>" +
            "<th><i class=\"fa fa-check\"></i> Activity Index</th>" +
            "<th><i class=\"fa fa-clock-o\"></i> Playtime</th>" +
            "<th><i class=\"fa fa-calendar-plus-o\"></i> Sessions</th>" +
            "<th><i class=\"fa fa-user-plus\"></i> Registered</th>" +
            "<th><i class=\"fa fa-calendar-check-o\"></i> Last Seen</th>" +
            "<th><i class=\"fa fa-globe\"></i> Geolocation</th></thead>" +
            "<tfoot><tr><th><i class=\"fa fa-user\"></i> Name</th>" +
            "<th><i class=\"fa fa-check\"></i> Activity Index</th>" +
            "<th><i class=\"fa fa-clock-o\"></i> Playtime</th>" +
            "<th><i class=\"fa fa-calendar-plus-o\"></i> Sessions</th>" +
            "<th><i class=\"fa fa-user-plus\"></i> Registered</th>" +
            "<th><i class=\"fa fa-calendar-check-o\"></i> Last Seen</th>" +
            "<th><i class=\"fa fa-globe\"></i> Geolocation</th>" +
            "</tr></tfoot><tbody>${0}</tbody></table>"),
    TABLE_PLAYERS("<table class=\"table table-bordered table-striped table-hover player-table dataTable\"><thead><tr>" +
            "<th><i class=\"fa fa-user\"></i> Name</th>" +
            "<th><i class=\"fa fa-check\"></i> Activity Index</th>" +
            "<th><i class=\"fa fa-clock-o\"></i> Playtime</th>" +
            "<th><i class=\"fa fa-calendar-plus-o\"></i> Sessions</th>" +
            "<th><i class=\"fa fa-user-plus\"></i> Registered</th>" +
            "<th><i class=\"fa fa-calendar-check-o\"></i> Last Seen</th>" +
            "<th><i class=\"fa fa-globe\"></i> Geolocation</th></thead>" +
            "<tbody>${0}</tbody></table>"),
    TABLE_SESSIONS_START(TABLE_START_3.parse("Session Started", "Session Ended", "Session Length")),
    TABLE_KILLS_START(TABLE_START_3.parse(FONT_AWESOME_ICON.parse("clock-o") + " Time", "Killed", "With")),
    TABLE_FACTIONS_START(TABLE_START_4.parse(FONT_AWESOME_ICON.parse("flag") + " Faction", FONT_AWESOME_ICON.parse("bolt") + " Power", FONT_AWESOME_ICON.parse("map-o") + " Land", FONT_AWESOME_ICON.parse("user") + " Leader")),
    TABLE_TOWNS_START(TABLE_START_4.parse(FONT_AWESOME_ICON.parse("bank") + " Town", FONT_AWESOME_ICON.parse("users") + " Residents", FONT_AWESOME_ICON.parse("map-o") + " Land", FONT_AWESOME_ICON.parse("user") + " Mayor")),
    TABLELINE_2("<tr><td><b>${0}</b></td><td>${1}</td></tr>"),
    TABLELINE_3("<tr><td><b>${0}</b></td><td>${1}</td><td>${2}</td></tr>"),
    TABLELINE_4("<tr><td><b>${0}</b></td><td>${1}</td><td>${2}</td><td>${3}</td></tr>"),
    TABLELINE_PLAYERS("<tr><td>${0}</td><td>${1}</td><td data-order=\"${2}\">${3}</td><td>${4}</td><td data-order=\"${5}\">${6}</td>" + "<td data-order=\"${7}\">${8}</td><td>${9}</td></tr>"),
    TABLELINE_PLAYERS_PLAYERS_PAGE("<tr><td>${0}</td><td data-order=\"${1}\">${2}</td><td>${3}</td><td>${4}</td>" + "<td>${5}</td><td>${6}</td></tr>"),
    TABLELINE_3_CUSTOMKEY("<tr><td sorttable_customkey=\"${0}\">${1}</td><td sorttable_customkey=\"${2}\">${3}</td><td sorttable_customkey=\"${4}\">${5}</td></tr>"),
    TABLELINE_3_CUSTOMKEY_1("<tr><td sorttable_customkey=\"${0}\">${1}</td><td>${2}</td><td>${3}</td></tr>");

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
        Map<String, Serializable> replaceMap = new HashMap<>();

        for (int i = 0; i < p.length; i++) {
            replaceMap.put(String.valueOf(i), p[i]);
        }

        StrSubstitutor sub = new StrSubstitutor(replaceMap);
        sub.setEnableSubstitutionInVariables(false);
        return sub.replace(html);
    }
}
