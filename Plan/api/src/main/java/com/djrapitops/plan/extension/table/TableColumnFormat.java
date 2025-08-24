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
package com.djrapitops.plan.extension.table;

public enum TableColumnFormat {
    /**
     * Boolean variables to be translated 'Yes' or 'No'.
     */
    BOOLEAN,
    /**
     * String variables to be formatted as links to player page.
     */
    PLAYER_NAME,
    /**
     * String variables to be formatted as colored (using § character).
     */
    CHAT_COLORED,
    /**
     * Number variables to be formatted as time amount (eg. 1h 30m 25s).
     */
    TIME_MILLISECONDS,
    /**
     * Number epoch millisecond to be formatted as date without second indicator.
     */
    DATE_YEAR,
    /**
     * Number epoch millisecond to be formatted as date with second indicator.
     */
    DATE_SECOND,
    /**
     * Default formatting, no extra formatting applied.
     */
    NONE
}
