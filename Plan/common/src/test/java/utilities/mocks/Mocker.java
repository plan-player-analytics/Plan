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

import java.io.*;

import static org.mockito.Mockito.doReturn;

/**
 * Abstract Mocker for methods that can be used for both Bungee and Bukkit.
 *
 * @author Rsl1122
 */
abstract class Mocker {

    PlanPlugin planMock;

    File getFile(String fileName) {
        // Read the resource from jar to a temporary file
        File file = new File(new File(planMock.getDataFolder(), "jar"), fileName);
        try {
            file.getParentFile().mkdirs();
            if (!file.exists() && !file.createNewFile()) {
                throw new FileNotFoundException("Could not create file: " + fileName);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        try (InputStream in = PlanPlugin.class.getResourceAsStream(fileName);
             OutputStream out = new FileOutputStream(file)) {

            int read;
            byte[] bytes = new byte[1024];

            while ((read = in.read(bytes)) != -1) {
                out.write(bytes, 0, read);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return file;
    }

    private void withPluginFile(String fileName) throws FileNotFoundException {
        if (planMock.getDataFolder() == null) {
            throw new IllegalStateException("withDataFolder needs to be called before setting files");
        }
        try {
            File file = getFile("/" + fileName);
            doReturn(new FileInputStream(file)).when(planMock).getResource(fileName);
        } catch (NullPointerException e) {
            System.out.println("File is missing! " + fileName);
        }
    }

    void withPluginFiles() throws FileNotFoundException {
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

                "web/css/main.css",
                "web/css/materialize.css",
                "web/css/style.css",
                "web/css/themes/all-themes.css",

                "web/js/demo.js",
                "web/js/admin.js",
                "web/js/helpers.js",
                "web/js/script.js",
                "web/js/charts/activityPie.js",
                "web/js/charts/lineGraph.js",
                "web/js/charts/horizontalBarGraph.js",
                "web/js/charts/stackGraph.js",
                "web/js/charts/performanceGraph.js",
                "web/js/charts/playerGraph.js",
                "web/js/charts/playerGraphNoNav.js",
                "web/js/charts/resourceGraph.js",
                "web/js/charts/diskGraph.js",
                "web/js/charts/tpsGraph.js",
                "web/js/charts/worldGraph.js",
                "web/js/charts/worldMap.js",
                "web/js/charts/punchCard.js",
                "web/js/charts/serverPie.js",
                "web/js/charts/worldPie.js",
                "web/js/charts/healthGauge.js",
                "web/js/charts/sessionCalendar.js",
                "web/js/charts/onlineActivityCalendar.js",

                "web/plugins/bootstrap/css/bootstrap.css",
                "web/plugins/node-waves/waves.css",
                "web/plugins/node-waves/waves.js",
                "web/plugins/animate-css/animate.css",
                "web/plugins/jquery-slimscroll/jquery.slimscroll.js",
                "web/plugins/jquery/jquery.min.js",
                "web/plugins/bootstrap/js/bootstrap.js",
                "web/plugins/jquery-datatable/skin/bootstrap/js/dataTables.bootstrap.js",
                "web/plugins/jquery-datatable/jquery.dataTables.js",
                "web/plugins/fullcalendar/fullcalendar.min.js",
                "web/plugins/fullcalendar/fullcalendar.min.css",
                "web/plugins/momentjs/moment.js"
        }) {
            withPluginFile(fileName);
        }
    }

}
