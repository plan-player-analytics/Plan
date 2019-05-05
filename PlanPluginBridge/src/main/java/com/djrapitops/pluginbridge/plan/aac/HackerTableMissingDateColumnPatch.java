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
package com.djrapitops.pluginbridge.plan.aac;

import com.djrapitops.plan.db.patches.Patch;

/**
 * Patch to fix a bug
 * https://github.com/plan-player-analytics/Plan/issues/1027
 * introduced in commit 530c4a2
 * https://github.com/plan-player-analytics/Plan/commit/530c4a2ea6fd56fd9a7aa3382f7571f31971dc1a#commitcomment-33415938
 *
 * @author Rsl1122
 */
public class HackerTableMissingDateColumnPatch extends Patch {

    @Override
    public boolean hasBeenApplied() {
        return hasColumn(HackerTable.TABLE_NAME, HackerTable.COL_DATE);
    }

    @Override
    protected void applyPatch() {
        addColumn(HackerTable.TABLE_NAME, HackerTable.COL_DATE + " bigint NOT NULL DEFAULT 0");
    }
}