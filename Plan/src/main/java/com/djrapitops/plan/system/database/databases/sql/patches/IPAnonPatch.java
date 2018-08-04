package com.djrapitops.plan.system.database.databases.sql.patches;

import com.djrapitops.plan.api.exceptions.database.DBInitException;
import com.djrapitops.plan.api.exceptions.database.DBOpException;
import com.djrapitops.plan.data.container.GeoInfo;
import com.djrapitops.plan.system.database.databases.sql.SQLDB;
import com.djrapitops.plan.system.database.databases.sql.processing.ExecStatement;
import com.djrapitops.plan.system.database.databases.sql.processing.QueryStatement;
import com.djrapitops.plan.system.database.databases.sql.tables.GeoInfoTable;
import com.djrapitops.plan.system.database.databases.sql.tables.move.Version18TransferTable;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plugin.api.utility.log.Log;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class IPAnonPatch extends Patch {

    public IPAnonPatch(SQLDB db) {
        super(db);
    }

    @Override
    public boolean hasBeenApplied() {
        String sql = "SELECT * FROM " + GeoInfoTable.TABLE_NAME +
                " WHERE " + GeoInfoTable.Col.IP + " NOT LIKE ? LIMIT 1";

        return query(new QueryStatement<Boolean>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, "%x%");
            }

            @Override
            public Boolean processResults(ResultSet set) throws SQLException {
                return !set.next();
            }
        });
    }

    @Override
    public void apply() {
        Map<UUID, List<GeoInfo>> allGeoInfo = db.getGeoInfoTable().getAllGeoInfo();
        anonymizeIPs(allGeoInfo);
        groupHashedIPs();
    }

    private void anonymizeIPs(Map<UUID, List<GeoInfo>> allGeoInfo) {
        String sql = "UPDATE " + GeoInfoTable.TABLE_NAME + " SET " +
                GeoInfoTable.Col.IP + "=?, " +
                GeoInfoTable.Col.IP_HASH + "=? " +
                "WHERE " + GeoInfoTable.Col.IP + "=?";

        db.executeBatch(new ExecStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                for (List<GeoInfo> geoInfos : allGeoInfo.values()) {
                    for (GeoInfo geoInfo : geoInfos) {
                        addToBatch(statement, geoInfo);
                    }
                }
            }

            private void addToBatch(PreparedStatement statement, GeoInfo geoInfo) throws SQLException {
                try {
                    String oldIP = geoInfo.getIp();
                    if (oldIP.endsWith(".xx.xx") || oldIP.endsWith("xx..")) {
                        return;
                    }
                    GeoInfo updatedInfo = new GeoInfo(
                            InetAddress.getByName(oldIP),
                            geoInfo.getGeolocation(),
                            geoInfo.getDate()
                    );
                    statement.setString(1, updatedInfo.getIp());
                    statement.setString(2, updatedInfo.getIpHash());
                    statement.setString(3, geoInfo.getIp());
                    statement.addBatch();
                } catch (UnknownHostException | UnsupportedEncodingException | NoSuchAlgorithmException e) {
                    if (Settings.DEV_MODE.isTrue()) {
                        Log.toLog(this.getClass(), e);
                    }
                }
            }
        });
    }

    private void groupHashedIPs() {
        try {
            new Version18TransferTable(db).alterTableV18();
        } catch (DBInitException e) {
            throw new DBOpException(e.getMessage(), e);
        }
    }
}
