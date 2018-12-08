package com.djrapitops.plan.system.settings.paths;

import com.djrapitops.plan.system.settings.paths.key.BooleanSetting;
import com.djrapitops.plan.system.settings.paths.key.Setting;
import com.djrapitops.plan.system.settings.paths.key.StringSetting;

/**
 * {@link Setting} values that are in "Export" section.
 *
 * @author Rsl1122
 */
public class ExportSettings {

    public static final Setting<String> HTML_EXPORT_PATH = new StringSetting("Export.HTML_Export_path");
    public static final Setting<String> JSON_EXPORT_PATH = new StringSetting("Export.JSON_Export_path");
    public static final Setting<Boolean> JS_AND_CSS = new BooleanSetting("Export.Parts.JavaScript_and_CSS");
    public static final Setting<Boolean> PLAYER_PAGES = new BooleanSetting("Export.Parts.Player_pages");
    public static final Setting<Boolean> PLAYER_JSON = new BooleanSetting("Export.Parts.Player_JSON");
    public static final Setting<Boolean> PLAYERS_PAGE = new BooleanSetting("Export.Parts.Players_page");
    public static final Setting<Boolean> SERVER_PAGE = new BooleanSetting("Export.Parts.Server_page");
    public static final Setting<Boolean> SERVER_JSON = new BooleanSetting("Export.Parts.Server_JSON");

    private ExportSettings() {
        /* static variable class */
    }
}