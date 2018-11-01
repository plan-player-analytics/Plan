package com.djrapitops.plan.system.database.databases;

import java.util.Optional;

/**
 * An enum which stores the name, the config name and if the Database supports MySQL Queries
 *
 * @author Fuzzlemann
 * @since 4.5.1
 */
public enum DBType {

    MySQL("MySQL", true),
    SQLite("SQLite", false),
    H2("H2", true);

    private final String name;
    private final String configName;
    private final boolean supportingMySQLQueries;

    DBType(String name, boolean supportingMySQLQueries) {
        this.name = name;
        this.configName = name.toLowerCase().trim();
        this.supportingMySQLQueries = supportingMySQLQueries;
    }

    /**
     * Gets the name of the {@code DBType}
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the config name of the {@code DBType}
     *
     * @return the config name
     */
    public String getConfigName() {
        return configName;
    }

    /**
     * Used to check if the {@code DBType} supports <b>most</b> MySQL Queries.<p>
     * When specific Statements are not compatible, the {@code DBType} should be checked.
     *
     * @return if the database supports MySQL Queries
     */
    public boolean supportsMySQLQueries() {
        return supportingMySQLQueries;
    }

    /**
     * Gets an {@code Optional<DBType>} which matches {@code name}.<p>
     * This method is case-insensitive.<p>
     * The {@code Optional<DBType>} is empty when no {@code DBType} is found.
     *
     * @param name the name of the {@code DBType}
     * @return an {@code Optional<DBType>}
     */
    public static Optional<DBType> getForName(String name) {
        for (DBType dbType : DBType.values()) {
            if (dbType.getName().equalsIgnoreCase(name)) {
                return Optional.of(dbType);
            }
        }

        return Optional.empty();
    }

    /**
     * Checks if the name of a {@code DBType} corresponds to {@code name}.<p>
     * This method is case-insensitive.
     *
     * @param name the name of the {@code DBType}
     * @return if the {@code DBType} exists
     * @see DBType#getForName(String)
     */
    public static boolean exists(String name) {
        for (DBType dbType : DBType.values()) {
            if (dbType.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }

        return false;
    }
}
