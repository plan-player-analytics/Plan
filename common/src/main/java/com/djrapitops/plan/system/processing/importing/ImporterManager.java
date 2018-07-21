/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.processing.importing;


import com.djrapitops.plan.system.processing.importing.importers.Importer;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Fuzzlemann
 */
public class ImporterManager {

    private static final List<Importer> registry = new ArrayList<>();

    /**
     * Constructor used to hide the public constructor
     */
    private ImporterManager() {
        throw new IllegalStateException("Utility class");
    }

    public static void registerImporter(Importer importer) {
        if (importer == null) {
            throw new NullPointerException("Importer cannot be null");
        }

        String firstName = importer.getNames().get(0);

        if (firstName == null) {
            throw new IllegalArgumentException("No Importer name given");
        }

        if (getImporter(firstName) != null) {
            removeImporter(firstName);
        }

        registry.add(importer);
    }

    public static List<Importer> getImporters() {
        return registry;
    }

    public static Importer getImporter(String name) {
        return registry.stream()
                .filter(importer -> importer.getNames().contains(name))
                .findAny()
                .orElse(null);
    }

    public static void removeImporter(String name) {
        registry.removeIf(importer -> importer.getNames().contains(name));
    }
}
