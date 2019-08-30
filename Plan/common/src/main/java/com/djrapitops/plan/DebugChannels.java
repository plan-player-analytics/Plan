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
package com.djrapitops.plan;

/**
 * Identifiers for different Debug channels.
 *
 * @author Rsl1122
 */
public class DebugChannels {


    private DebugChannels() {
        /* Static variable class */
    }

    public static final String ANALYSIS = "Analysis";
    @Deprecated
    public static final String INFO_REQUESTS = "InfoRequests";
    @Deprecated
    public static final String CONNECTIONS = "Connections";
    @Deprecated
    public static final String WEB_REQUESTS = "Web Requests";
    public static final String IMPORTING = "Importing";
    public static final String SQL = "SQL";
    public static final String DATA_EXTENSIONS = "DataExtensions";

}
