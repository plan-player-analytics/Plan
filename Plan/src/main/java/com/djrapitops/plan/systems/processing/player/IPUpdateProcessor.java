/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.systems.processing.player;

import com.djrapitops.plugin.api.utility.log.Log;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.container.GeoInfo;
import main.java.com.djrapitops.plan.systems.cache.GeolocationCache;

import java.sql.SQLException;
import java.util.UUID;

/**
 * Updates the IP and Geolocation of a user.
 *
 * @author Rsl1122
 */
public class IPUpdateProcessor extends PlayerProcessor {

    private final String ip;
    private final long time;

    public IPUpdateProcessor(UUID uuid, String ip, long time) {
        super(uuid);
        this.ip = ip;
        this.time = time;
    }

    @Override
    public void process() {
        UUID uuid = getUUID();
        String country = GeolocationCache.getCountry(ip);
        try {
            Plan.getInstance().getDB().getIpsTable().saveGeoInfo(uuid, new GeoInfo(ip, country, time));
        } catch (SQLException e) {
            Log.toLog(this.getClass().getName(), e);
        }
    }
}