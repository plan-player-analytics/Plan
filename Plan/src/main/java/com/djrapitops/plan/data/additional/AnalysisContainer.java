/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.data.additional;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

/**
 * Container used to parse data for Analysis page.
 * <p>
 * Similar to InspectContainer, but can contain data for each player for a bigger Player data table.
 * <p>
 * Can contain values: addValue("Total Examples", 1) parses into ("Total Examples: 1")
 * Html: addHtml(key, "{@code <html>}") parses into ("{@code <html>}")
 * Tables: addTable(key, TableContainer) parses into ("{@code <table>...</table}")
 * Player Data for a big table: {@code addTableData("header", Map<UUID, value>)} parses a new column to Plugin data player table.
 * <p>
 * Has methods for adding icons to Strings:
 * getWithIcon("text", "cube") parses into {@code "<i class=\"fa fa-cube\"></i> text"}
 * getWithColoredIcon("text", "cube", "light-green") parses into {@code "<i class=\"col-light-green fa fa-cube\"></i> text"}
 *
 * @author Rsl1122
 * @see TableContainer
 * @see InspectContainer
 * @since 4.1.0
 */
public class AnalysisContainer extends InspectContainer {

    private TreeMap<String, Map<UUID, Serializable>> playerTableValues;

    public AnalysisContainer() {
        playerTableValues = new TreeMap<>();
    }

    public TreeMap<String, Map<UUID, Serializable>> getPlayerTableValues() {
        return playerTableValues;
    }
}