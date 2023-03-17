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
package utilities;

import com.djrapitops.plan.storage.database.queries.QueryAllStatement;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility for quick lookups of table contents during testing.
 *
 * @author AuroraLS3
 */
public class TableContentsQuery extends QueryAllStatement<List<List<Object>>> {

    public TableContentsQuery(String tableName) {
        super("SELECT * FROM " + tableName);
    }

    @Override
    public List<List<Object>> processResults(ResultSet set) throws SQLException {
        List<List<Object>> rows = new ArrayList<>();
        int colCount = set.getMetaData().getColumnCount();

        List<Object> firstRow = new ArrayList<>();
        for (int i = 1; i <= colCount; i++) {
            firstRow.add(set.getMetaData().getColumnLabel(i));
        }
        rows.add(firstRow);
        while (set.next()) {
            List<Object> row = new ArrayList<>();
            for (int i = 1; i <= colCount; i++) {
                row.add(set.getObject(i));
            }
            rows.add(row);
        }
        return rows;
    }
}
