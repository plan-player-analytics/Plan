/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.settings.config.paths;

import com.djrapitops.plan.settings.config.ConfigNode;
import com.djrapitops.plan.settings.config.paths.key.*;

/**
 * {@link Setting} values that are in "Display_options" section.
 *
 * @author AuroraLS3
 */
public class DisplaySettings {

    public static final Setting<String> THEME = new StringSetting("Display_options.Theme");
    public static final Setting<String> PLAYER_HEAD_IMG_URL = new StringSetting("Display_options.Player_head_image_url");
    public static final Setting<Integer> SESSIONS_PER_PAGE = new IntegerSetting("Display_options.Sessions.Show_on_page");
    public static final Setting<Boolean> ORDER_WORLD_PIE_BY_PERCENTAGE = new BooleanSetting("Display_options.Sessions.Order_world_pies_by_percentage");
    public static final Setting<Integer> PLAYERS_PER_SERVER_PAGE = new IntegerSetting("Display_options.Players_table.Show_on_server_page");
    public static final Setting<Integer> PLAYERS_PER_PLAYERS_PAGE = new IntegerSetting("Display_options.Players_table.Show_on_players_page");
    public static final Setting<Boolean> OPEN_PLAYER_LINKS_IN_NEW_TAB = new BooleanSetting("Display_options.Open_player_links_in_new_tab");
    public static final Setting<Boolean> GAPS_IN_GRAPH_DATA = new BooleanSetting("Display_options.Graphs.Show_gaps_in_data");
    public static final Setting<Double> GRAPH_TPS_THRESHOLD_HIGH = new DoubleSetting("Display_options.Graphs.TPS.High_threshold");
    public static final Setting<Double> GRAPH_TPS_THRESHOLD_MED = new DoubleSetting("Display_options.Graphs.TPS.Medium_threshold");
    public static final Setting<Integer> GRAPH_DISK_THRESHOLD_HIGH = new IntegerSetting("Display_options.Graphs.Disk_space.High_threshold");
    public static final Setting<Integer> GRAPH_DISK_THRESHOLD_MED = new IntegerSetting("Display_options.Graphs.Disk_space.Medium_threshold");
    public static final Setting<String> CMD_COLOR_MAIN = new StringSetting("Display_options.Command_colors.Main");
    public static final Setting<String> CMD_COLOR_SECONDARY = new StringSetting("Display_options.Command_colors.Secondary");
    public static final Setting<String> CMD_COLOR_TERTIARY = new StringSetting("Display_options.Command_colors.Highlight");
    public static final Setting<String> WORLD_PIE = new StringSetting("Display_options.WorldPie");
    public static final Setting<ConfigNode> WORLD_ALIASES = new Setting<>("World_aliases.List", ConfigNode.class) {
        @Override
        public ConfigNode getValueFrom(ConfigNode node) {
            return node.getNode(path).orElseGet(() -> node.addNode(path));
        }
    };

    private DisplaySettings() {
        /* static variable class */
    }

}