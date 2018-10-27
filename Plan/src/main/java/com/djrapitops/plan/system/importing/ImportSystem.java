package com.djrapitops.plan.system.importing;

import com.djrapitops.plan.system.SubSystem;
import com.djrapitops.plan.system.importing.importers.Importer;
import com.djrapitops.plugin.utilities.Verify;

import java.util.*;

/**
 * Abstract representation of an ImportSystem.
 *
 * @author Rsl1122
 */
public abstract class ImportSystem implements SubSystem {

    protected Map<String, Importer> importers;

    public ImportSystem() {
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