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
package com.djrapitops.plan.storage.database.queries.filter.filters;

import com.djrapitops.plan.delivery.domain.datatransfer.InputFilterDto;
import com.djrapitops.plan.storage.database.queries.filter.CompleteSetException;
import com.djrapitops.plan.storage.database.queries.filter.Filter;
import com.djrapitops.plan.utilities.dev.Untrusted;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

public abstract class MultiOptionFilter implements Filter {

    @Override
    public String[] getExpectedParameters() {
        return new String[]{"selected"};
    }

    protected List<String> getSelected(@Untrusted InputFilterDto query) {
        @Untrusted String selectedJSON = query.get("selected").orElseThrow(IllegalArgumentException::new);
        @Untrusted List<String> selected = new Gson().fromJson(selectedJSON, new TypeToken<List<String>>() {}.getType());
        if (selected.isEmpty()) throw new CompleteSetException();
        return selected;
    }
}
