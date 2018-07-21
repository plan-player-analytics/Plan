package com.djrapitops.plan.utilities.html;

import org.apache.commons.text.StringSubstitutor;

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

    /**
     * @deprecated Use com.djrapitops.plan.utilities.html.icon.Icon instead
     */
    @Deprecated
    FONT_AWESOME_ICON("<i class=\"fa fa-${0}\"></i>"),
    /**
     * @deprecated Use com.djrapitops.plan.utilities.html.icon.Icon instead
     */
    @Deprecated
    FA_COLORED_ICON("<i class=\"col-${0} fa fa-${1}\"></i>"),
    SPAN("${0}</span>"),
    BUTTON("<a class=\"button\" href=\"${0}\">${1}</a>"),
    BUTTON_CLASS("class=\"button\""),
    LINK("<a class=\"link\" href=\"${0}\">${1}</a>"),
    LINK_A("<a href=\"${0}\">${1}</a>"),
    LINK_TOOLTIP("<a title=\"${2}\" class=\"link\" href=\"${0}\">${1}</a>"),
    LINK_EXTERNAL("<a class=\"link\" target=\"_blank\" href=\"${0}\">${1}</a>"),
    LINK_CLASS("class=\"link\""),
    IMG("<img src=\"${0}\">"),

    PARAGRAPH("<p>${0}</p>"),
    HEADER("<h1>${0}</h1>"),
    HEADER_2("<h2>${0}</h2>"),

    DIV_W_CLASS("<div class=\"${0}\">${1}</div>"),
    DIV_W_CLASS_STYLE("<div class=\"${0}\" style=\"${1}\">${2}</div>"),

    ROW("<div class=\"row\">${0}</div>"),
    CARD("<div class=\"card\">${0}</div>"),
    BODY("<div class=\"body\">${0}</div>"),
    PANEL("<div class=\"panel panel-default\">${0}</div>"),
    PANEL_BODY("<div class=\"panel-body\">${0}</div>"),
    HELP_BUBBLE("<div class=\"col-xs-6 col-sm-6 col-lg-6\"><a href=\"javascript:void(0)\" class=\"help material-icons pull-right\" data-trigger=\"focus\" data-toggle=\"popover\" data-placement=\"left\" data-container=\"body\" data-html=\"true\" data-original-title=\"${0}\" data-content=\"${1}\">help_outline</a></div>"),

    TABLE_END("</tbody></table>"),
    TABLE("<table class=\"table table-striped\">"),
    TABLE_SCROLL("<table class=\"table table-striped scrollbar\">"),
    TABLE_JQUERY("<table class=\"table table-bordered table-striped table-hover ${0} dataTable\">"),
    TABLE_COLORED("<table class=\"bg-${0} table table-striped\">"),
    TABLE_HEAD("<thead>${0}</thead>"),
    TABLE_BODY("<tbody>${0}</tbody>"),
    TABLE_START_2("<table class=\"table table-striped\"><thead><tr><th>${0}</th><th>${1}</th></tr></thead><tbody>"),
    TABLE_START_3("<table class=\"table table-striped\"><thead><tr><th>${0}</th><th>${1}</th><th>${2}</th></tr></thead><tbody>"),
    TABLE_START_4("<table class=\"table table-striped\"><thead><tr><th>${0}</th><th>${1}</th><th>${2}</th><th>${3}</th></tr></thead><tbody>"),
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

        StringSubstitutor sub = new StringSubstitutor(replaceMap);
        sub.setEnableSubstitutionInVariables(false);
        return sub.replace(html);
    }
}
