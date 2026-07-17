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
package utilities;

import net.playeranalytics.plugin.server.PluginLogger;

import java.util.ArrayList;
import java.util.List;

public class TestPluginLogger implements PluginLogger {

    private final List<String> logMessages = new ArrayList<>();

    private boolean addToList = false;

    public void logMessages(boolean addToList) {
        this.addToList = addToList;
    }

    public List<String> getLogMessages() {
        return logMessages;
    }

    @Override
    public PluginLogger info(String s) {
        System.out.println("[INFO] " + s);
        if (addToList) logMessages.add(s);
        return this;
    }

    @Override
    public PluginLogger warn(String s) {
        System.out.println("[WARN] " + s);
        if (addToList) logMessages.add(s);
        return this;
    }

    @Override
    public PluginLogger error(String s) {
        System.out.println("[ERROR] " + s);
        if (addToList) logMessages.add(s);
        return this;
    }

    @Override
    public PluginLogger warn(String s, Throwable throwable) {
        warn(s);
        throwable.printStackTrace();
        return this;
    }

    @Override
    public PluginLogger error(String s, Throwable throwable) {
        error(s);
        throwable.printStackTrace();
        return this;
    }
}
