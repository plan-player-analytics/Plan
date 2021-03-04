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
package utilities.mocks;

import com.djrapitops.plan.PlanPlugin;
import utilities.TestResources;

import java.io.File;
import java.nio.file.Files;

import static org.mockito.Mockito.when;

/**
 * Abstract Mocker for methods that can be used for both Bungee and Bukkit.
 *
 * @author AuroraLS3
 */
abstract class Mocker {

    PlanPlugin planMock;

    File getFile(String fileName) {
        // Read the resource from jar to a temporary file
        File file = new File(new File(planMock.getDataFolder(), "jar"), fileName);
        TestResources.copyResourceIntoFile(file, fileName);
        return file;
    }

    private void withPluginFile(String fileName) {
        if (planMock.getDataFolder() == null) {
            throw new IllegalStateException("withDataFolder needs to be called before setting files");
        }
        try {
            File file = getFile("/assets/plan/" + fileName);
            when(planMock.getResource("assets/plan/" + fileName)).thenAnswer(invocationOnMock -> Files.newInputStream(file.toPath()));
        } catch (NullPointerException e) {
            System.out.println("File is missing! " + fileName);
        }
    }

    void withPluginFiles() {
        for (String fileName : new String[]{
                "bungeeconfig.yml",
                "config.yml",
                "DefaultServerInfoFile.yml",
                "themes/theme.yml",

                "web/server.html",
                "web/player.html",
                "web/players.html",
                "web/network.html",
                "web/error.html",

                "web/css/style.css",
                "web/css/sb-admin-2.css",

                "web/js/color-selector.js",
                "web/js/graphs.js",
                "web/js/network-values.js",
                "web/js/pingTable.js",
                "web/js/player-values.js",
                "web/js/sb-admin-2.js",
                "web/js/server-values.js",
                "web/js/sessionAccordion.js",
                "web/js/xmlhttprequests.js",

                "web/vendor/bootstrap/js/bootstrap.bundle.min.js",
                "web/vendor/datatables/datatables.min.css",
                "web/vendor/datatables/datatables.min.js",
                "web/vendor/jquery/jquery.min.js",
                "web/vendor/fontawesome-free/css/all.min.css",
                "web/vendor/fontawesome-free/webfonts/fa-brands-400.eot",
                "web/vendor/fontawesome-free/webfonts/fa-brands-400.ttf",
                "web/vendor/fontawesome-free/webfonts/fa-brands-400.woff",
                "web/vendor/fontawesome-free/webfonts/fa-brands-400.woff2",
                "web/vendor/fontawesome-free/webfonts/fa-regular-400.eot",
                "web/vendor/fontawesome-free/webfonts/fa-regular-400.ttf",
                "web/vendor/fontawesome-free/webfonts/fa-regular-400.woff",
                "web/vendor/fontawesome-free/webfonts/fa-regular-400.woff2",
                "web/vendor/fontawesome-free/webfonts/fa-solid-900.eot",
                "web/vendor/fontawesome-free/webfonts/fa-solid-900.ttf",
                "web/vendor/fontawesome-free/webfonts/fa-solid-900.woff",
                "web/vendor/fontawesome-free/webfonts/fa-solid-900.woff2",
                "web/vendor/fullcalendar/fullcalendar.min.js",
                "web/vendor/fullcalendar/fullcalendar.min.css",
                "web/vendor/highcharts/drilldown.js",
                "web/vendor/highcharts/highcharts-more.js",
                "web/vendor/highcharts/highstock.js",
                "web/vendor/highcharts/map.js",
                "web/vendor/highcharts/no-data-to-display.js",
                "web/vendor/highcharts/world.js",
                "web/vendor/momentjs/moment.js"
        }) {
            withPluginFile(fileName);
        }
    }

}
