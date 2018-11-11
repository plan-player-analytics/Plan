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
package com.djrapitops.plan.utilities.html.icon;

/**
 * Class that contains commonly used {@link Icon}s.
 *
 * @see Icon
 */
public class Icons {

    public static final Icon PLAYTIME = Icon.called("clock").of(Color.GREEN).of(Family.REGULAR).build();
    public static final Icon SESSION_LENGTH = Icon.called("clock").of(Color.TEAL).of(Family.REGULAR).build();
    public static final Icon AFK_LENGTH = Icon.called("clock").of(Color.GREY).of(Family.REGULAR).build();
    public static final Icon PLAYER_KILLS = Icon.called("crosshairs").of(Color.RED).build();
    public static final Icon MOB_KILLS = Icon.called("crosshairs").of(Color.GREEN).build();
    public static final Icon DEATHS = Icon.called("skull").build();
    public static final Icon SESSION_COUNT = Icon.called("calendar-check").of(Color.TEAL).of(Family.REGULAR).build();
    public static final Icon OPERATOR = Icon.called("superpowers").of(Color.BLUE).of(Family.BRAND).build();
    public static final Icon BANNED = Icon.called("gavel").of(Color.RED).build();
    public static final Icon SERVER = Icon.called("server").of(Color.GREEN).build();

    public static final Icon GREEN_THUMB = Icon.called("thumbs-up").of(Color.GREEN).build();
    public static final Icon YELLOW_FLAG = Icon.called("flag").of(Color.AMBER).build();
    public static final Icon RED_WARN = Icon.called("exclamation-circle").of(Color.RED).build();
    public static final Icon GREEN_PLUS = Icon.called("plus").of(Color.GREEN).build();
    public static final Icon RED_MINUS = Icon.called("minus").of(Color.RED).build();
    public static final Icon HELP_RING = Icon.called("life-ring").of(Color.RED).of(Family.REGULAR).build();

    private Icons() {
        /* Static variable class */
    }

}
