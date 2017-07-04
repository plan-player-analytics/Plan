/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.java.main.java.com.djrapitops.plan.data.cache;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import main.java.com.djrapitops.plan.data.cache.LocationCache;
import org.bukkit.Location;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import test.java.utils.MockUtils;

/**
 *
 * @author Rsl1122
 */
public class LocationCacheTest {

    private LocationCache test;

    /**
     *
     */
    public LocationCacheTest() {
    }

    /**
     *
     */
    @Before
    public void setUp() {
        test = new LocationCache();
    }

    /**
     *
     */
    @Test
    public void testAddLocation() {
        Location loc = new Location(MockUtils.mockWorld(), 0, 0, 0);
        UUID uuid = MockUtils.getPlayerUUID();
        test.addLocation(uuid, loc);
        assertTrue("Didn't contain location", test.getLocationsForSaving(uuid).contains(loc));
    }

    /**
     *
     */
    @Test
    public void testAddLocations() {
        Location loc = new Location(MockUtils.mockWorld(), 0, 0, 0);
        Location loc2 = new Location(MockUtils.mockWorld(), 1, 1, 1);
        UUID uuid = MockUtils.getPlayerUUID();
        test.addLocations(uuid, Arrays.asList(new Location[]{loc, loc2}));
        List<Location> result = test.getLocationsForSaving(uuid);
        assertTrue("Didn't contain location", result.contains(loc));
        assertTrue("Didn't contain location", result.contains(loc2));
    }

    /**
     *
     */
    @Test
    public void testClearLocations() {
        Location loc = new Location(MockUtils.mockWorld(), 0, 0, 0);
        UUID uuid = MockUtils.getPlayerUUID();
        test.addLocation(uuid, loc);
        test.clearLocations(uuid);
        assertTrue("contains location", !test.getLocationsForSaving(uuid).contains(loc));
    }

}
