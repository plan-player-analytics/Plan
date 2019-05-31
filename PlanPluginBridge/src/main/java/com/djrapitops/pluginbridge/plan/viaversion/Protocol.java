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
package com.djrapitops.pluginbridge.plan.viaversion;

/**
 * Contains static method for formatting protocol version into readable form.
 *
 * @author Rsl1122
 */
public class Protocol {


    private Protocol() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * http://wiki.vg/Protocol_version_numbers
     *
     * @param protocolVersion ProtocolVersion
     * @return Minecraft Version (estimate)
     */
    public static String getMCVersion(int protocolVersion) {
        switch (protocolVersion) {
            case 485:
                return "1.14.2";
            case 480:
                return "1.14.1";
            case 477:
                return "1.14";
            case 404:
                return "1.13.2";
            case 401:
                return "1.13.1";
            case 393:
                return "1.13";
            case 340:
                return "1.12.2";
            case 338:
                return "1.12.1";
            case 335:
                return "1.12";
            case 316:
                return "1.11.2";
            case 315:
                return "1.11";
            case 210:
                return "1.10.2";
            case 110:
                return "1.9.4";
            case 109:
                return "1.9.2";
            case 107:
                return "1.9";
            case 47:
                return "1.8.9";
            case 5:
                return "1.7.10";
            case 4:
                return "1.7.5";
            default:
                return "Newer than 1.14.2 (" + protocolVersion + ")";
        }
    }
}
