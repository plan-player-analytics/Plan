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
package com.djrapitops.plan.storage.database.transactions.patches;

import com.djrapitops.plan.exceptions.database.DBOpException;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.DatabaseSettings;
import com.djrapitops.plan.storage.database.DBType;
import com.djrapitops.plan.storage.database.queries.Query;
import net.playeranalytics.plugin.server.PluginLogger;
import org.intellij.lang.annotations.Language;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

/**
 * Corrects database charset and collation.
 *
 * @author AuroraLS3
 */
@Singleton
public class CorrectWrongCharacterEncodingPatch extends Patch {

    private final PluginLogger logger;
    private final PlanConfig config;
    private List<String> correctionSqlQueries;

    @Inject
    public CorrectWrongCharacterEncodingPatch(PluginLogger logger, PlanConfig config) {
        this.logger = logger;
        this.config = config;
    }

    @Override
    public boolean hasBeenApplied() {
        if (dbType != DBType.MYSQL) return true;

        correctionSqlQueries = query(getBadTableCorrectionQueries());
        // Fix for MariaDB mysql.user table being a view
        correctionSqlQueries.removeIf(sql -> sql.startsWith("ALTER TABLE `user`"));
        return correctionSqlQueries.isEmpty();
    }

    public Query<List<String>> getBadTableCorrectionQueries() {
        String databaseName = config.get(DatabaseSettings.MYSQL_DATABASE);

        @Language("MySQL")
        String selectTablesWithWrongCharset = "SELECT CONCAT('ALTER TABLE `',  table_name, '` CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;')\n" +
                "FROM information_schema.TABLES AS T, information_schema.`COLLATION_CHARACTER_SET_APPLICABILITY` AS C\n" +
                "WHERE C.collation_name = T.table_collation\n" +
                "AND T.table_name LIKE 'plan\\_%'\n" +
                "AND T.table_schema = '" + databaseName + "'\n" +
                "AND\n" +
                "(\n" +
                "    C.CHARACTER_SET_NAME != 'utf8mb4'\n" +
                "    OR\n" +
                "    C.COLLATION_NAME != 'utf8mb4_general_ci'\n" +
                ")";
        @Language("MySQL")
        String selectColumnsWithWrongCharset = "SELECT CONCAT('ALTER TABLE `', table_name, '` MODIFY `', column_name, '` ', DATA_TYPE, '(', CHARACTER_MAXIMUM_LENGTH, ') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci', (CASE WHEN IS_NULLABLE = 'NO' THEN ' NOT NULL' ELSE '' END), ';')\n" +
                "FROM information_schema.COLUMNS \n" +
                "WHERE TABLE_SCHEMA = '" + databaseName + "'\n" +
                "AND table_name LIKE 'plan\\_%'\n" +
                "AND DATA_TYPE = 'varchar'\n" +
                "AND\n" +
                "(\n" +
                "    CHARACTER_SET_NAME != 'utf8mb4'\n" +
                "    OR\n" +
                "    COLLATION_NAME != 'utf8mb4_general_ci'\n" +
                ")";
        @Language("MySQL")
        String sql = selectTablesWithWrongCharset + " UNION " + selectColumnsWithWrongCharset;
        return db -> db.queryList(sql, resultSet -> resultSet.getString(1));
    }

    @Override
    protected void applyPatch() {
        try {
            for (String correctionSqlQuery : correctionSqlQueries) {
                execute(correctionSqlQuery);
            }
            @Language("MySQL")
            String sql = "ALTER DATABASE " + config.get(DatabaseSettings.MYSQL_DATABASE) + " CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci";
            execute(sql);
        } catch (DBOpException e) {
            logger.warn("CorrectWrongCharacterEncodingPatch could not be applied: " + e.getMessage());
            logger.warn("Apply it manually or correct access privileges for MySQL user.");
            logger.warn("Instructions for manual application https://github.com/plan-player-analytics/Plan/issues/2293");
        } finally {
            correctionSqlQueries.clear();
        }
    }
}
