package com.djrapitops.plan.db.patches;

import com.djrapitops.plan.db.access.ExecStatement;
import com.djrapitops.plan.db.access.HasMoreThanZeroQueryStatement;
import com.djrapitops.plan.db.sql.tables.GeoInfoTable;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import static com.djrapitops.plan.db.sql.parsing.Sql.*;

/**
 * Patch for removing ip_hash values from plan_ips table.
 * <p>
 * The patch is a response to a concern:
 * "Hashed IP addresses are pseudonymised not anonymised and can be easily decoded using a rainbow table".
 *
 * @author Rsl1122
 */
public class DeleteIPHashesPatch extends Patch {

    private boolean hasNoHashColumn;

    @Override
    public boolean hasBeenApplied() {
        hasNoHashColumn = !hasColumn(GeoInfoTable.TABLE_NAME, "ip_hash");

        String sql = SELECT + "COUNT(1) as c" + FROM + GeoInfoTable.TABLE_NAME +
                WHERE + "ip_hash" + IS_NOT_NULL;

        return hasNoHashColumn || !query(new HasMoreThanZeroQueryStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) {
            }
        });
    }

    @Override
    protected void applyPatch() {
        if (hasNoHashColumn) {
            return;
        }

        String sql = "UPDATE " + GeoInfoTable.TABLE_NAME + " SET ip_hash=?" + WHERE + "ip_hash" + IS_NOT_NULL;
        execute(new ExecStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setNull(1, Types.VARCHAR);
            }
        });
    }

}