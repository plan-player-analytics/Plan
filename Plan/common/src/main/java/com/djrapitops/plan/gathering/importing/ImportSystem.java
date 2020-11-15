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
package com.djrapitops.plan.gathering.importing;

import com.djrapitops.plan.SubSystem;
import com.djrapitops.plan.gathering.importing.importers.Importer;
import com.djrapitops.plugin.utilities.Verify;

import java.util.*;

/**
 * Abstract representation of an ImportSystem.
 * <p>
 * TODO it is possible to remove the abstract part of this class by binding Importers to a Map with Dagger
 *
 * @author Rsl1122
 */
public abstract class ImportSystem implements SubSystem {

    protected final Map<String, Importer> importers;

    protected ImportSystem() {
        importers = new HashMap<>();
    }

    @Override
    public void enable() {
        registerImporters();
    }

    abstract void registerImporters();

    public void registerImporter(Importer importer) {
        Verify.nullCheck(importer, () -> new IllegalArgumentException("Importer cannot be null"));

        importers.put(importer.getName(), importer);
    }

    public Optional<Importer> getImporter(String name) {
        return Optional.ofNullable(importers.get(name));
    }

    public List<String> getImporterNames() {
        List<String> names = new ArrayList<>(importers.keySet());
        Collections.sort(names);
        return names;
    }

    @Override
    public void disable() {
        importers.clear();
    }
}