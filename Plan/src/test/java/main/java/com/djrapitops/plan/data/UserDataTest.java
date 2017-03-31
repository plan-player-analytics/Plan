/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.java.main.java.com.djrapitops.plan.data;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import main.java.com.djrapitops.plan.data.DemographicsData;
import main.java.com.djrapitops.plan.data.SessionData;
import main.java.com.djrapitops.plan.data.UserData;
import org.bukkit.GameMode;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.junit.Before;
import org.junit.Ignore;

/**
 *
 * @author Risto
 */
public class UserDataTest {

    private UserData test;

    public UserDataTest() {
    }

    @Before
    public void setUp() {
        DemographicsData demData = null;
        test = new UserData(UUID.fromString("7f8149a0-b5a5-4fcd-80b5-6cff083a99f1"), 0, null, true, GameMode.CREATIVE, demData, "Testname", true);
    }

    @Test
    public void testAddIpAddress() throws UnknownHostException {
        InetAddress ip = InetAddress.getByName("247.183.163.155");
        InetAddress ip2 = InetAddress.getByName("95.19.221.226");
        test.addIpAddress(ip);
        test.addIpAddress(ip2);
        test.addIpAddress(ip2);
        test.addIpAddress(null);
        assertTrue("Didn't add 1", test.getIps().contains(ip));
        assertTrue("Didn't add 2", test.getIps().contains(ip2));
        assertTrue("Added null", !test.getIps().contains(null));
        assertTrue("Added multiples", test.getIps().size() == 2);
    }

    @Test
    public void testAddIpAddresses() throws UnknownHostException {
        List<InetAddress> ips = new ArrayList<>();
        InetAddress ip = InetAddress.getByName("247.183.163.155");
        InetAddress ip2 = InetAddress.getByName("95.19.221.226");
        ips.add(ip);
        ips.add(ip2);
        ips.add(ip2);
        ips.add(null);
        test.addIpAddresses(ips);
        assertTrue("Didn't add 1", test.getIps().contains(ip));
        assertTrue("Didn't add 2", test.getIps().contains(ip2));
        assertTrue("Added null", !test.getIps().contains(null));
        assertTrue("Added multiples", test.getIps().size() == 2);
    }

    @Ignore
    @Test
    public void testAddLocation() {
    }

    @Ignore
    @Test
    public void testAddLocations() {
    }

    @Test
    public void testAddNickname() {
        String one = "Test1";
        String two = "Test2";
        boolean n = test.addNickname(one);
        boolean n1 = test.addNickname(two);
        boolean n2 = test.addNickname(two);
        test.addNickname(null);
        assertTrue("Didn't add 1", test.getNicknames().contains(one));
        assertTrue("Didn't add 2", test.getNicknames().contains(two));
        assertTrue("1 is new", n);
        assertTrue("2 is new", n1);
        assertTrue("2 is not new", !n2);
        assertTrue("Added null", !test.getNicknames().contains(null));
        assertTrue("Added multiples", test.getNicknames().size() == 2);
    }

    @Test
    public void testAddNicknames() {
        String one = "Test1";
        String two = "Test2";
        List<String> o = new ArrayList<>();
        o.add(one);
        o.add(two);
        o.add(two);
        o.add(null);
        test.addNicknames(o);
        assertTrue("Didn't add 1", test.getNicknames().contains(one));
        assertTrue("Didn't add 2", test.getNicknames().contains(two));
        assertTrue("Added null", !test.getNicknames().contains(null));
        assertTrue("Added multiples", test.getNicknames().size() == 2);
    }

    @Test
    public void testSetGMTime() {
        test.setGMTime(GameMode.SURVIVAL, 1L);
        assertTrue("" + test.getGmTimes().get(GameMode.SURVIVAL), test.getGmTimes().get(GameMode.SURVIVAL) == 1L);
    }

    @Test
    public void testSetGMTimeWhenGMTimesNull() {
        test.setGmTimes(null);
        test.setGMTime(GameMode.SURVIVAL, 1L);
        assertTrue("" + test.getGmTimes().get(GameMode.SURVIVAL), test.getGmTimes().get(GameMode.SURVIVAL) == 1L);
    }

    @Test
    public void testSetGMTimeNull() {
        test.setGMTime(null, 0L);
        assertTrue("Added null", !test.getGmTimes().containsKey(null));
    }

    @Test
    public void testSetAllGMTimes() {
        HashMap<GameMode, Long> gmTimes = new HashMap<>();
        gmTimes.put(null, 0L);
        test.setGmTimes(gmTimes);
        test.setAllGMTimes(1L, 2L, 3L, 4L);
        HashMap<GameMode, Long> times = test.getGmTimes();
        assertTrue("Cleared gmTimes", !times.containsKey(null));
        assertTrue("Not equal 0", times.get(GameMode.SURVIVAL) == 1L);
        assertTrue("Not equal 1", times.get(GameMode.CREATIVE) == 2L);
        assertTrue("Not equal 2", times.get(GameMode.ADVENTURE) == 3L);
        assertTrue("Not equal 3", times.get(GameMode.SPECTATOR) == 4L);
    }

    @Test
    public void testAddSession() {
        SessionData correct = new SessionData(0, 1);
        test.addSession(correct);
        assertTrue("Didn't add correct one", test.getSessions().contains(correct));
    }

    @Test
    public void testAddSessionIncorrect() {
        SessionData incorrect = new SessionData(0);
        test.addSession(incorrect);
        assertTrue("Added incorrect one", !test.getSessions().contains(incorrect));
    }

    @Test
    public void testAddSessionNull() {
        SessionData incorrect = null;
        test.addSession(incorrect);
        assertTrue("Added null", !test.getSessions().contains(incorrect));
    }

    @Test
    public void testAddSessions() {
        SessionData correct = new SessionData(0, 1);
        SessionData incorrect = new SessionData(0);
        List<SessionData> o = new ArrayList<>();
        o.add(correct);
        o.add(incorrect);
        o.add(null);
        test.addSessions(o);
        assertTrue("Didn't add correct one", test.getSessions().contains(correct));
        assertTrue("Added incorrect one", !test.getSessions().contains(incorrect));
        assertTrue("Added null", !test.getSessions().contains(incorrect));
    }

    @Test
    public void testSetCurrentSession() {
        SessionData s = new SessionData(0);
        test.setCurrentSession(s);
        assertEquals(test.getCurrentSession(), s);
    }

    @Test
    public void testUpdateBanned() {
        test.updateBanned(true);
        assertTrue("Not true", test.isBanned());
        test.updateBanned(false);
        assertTrue("True", !test.isBanned());
    }

    @Test
    public void testIsAccessed() {
        test.access();
        assertTrue("Not accessed, even though accessing", test.isAccessed());
        test.access();
        test.stopAccessing();
        assertTrue("Not accessed, even though accessing", test.isAccessed());
        test.stopAccessing();
        assertTrue("Accessed, even though not accessing", !test.isAccessed());
    }

    @Test
    public void testAccess() {
        test.access();
        assertTrue("Not accessed, even though accessing", test.isAccessed());
    }

    @Test
    public void testStopAccessing() {
        test.access();
        test.stopAccessing();
        assertTrue("Accessed, even though not accessing", !test.isAccessed());
    }

    @Test
    public void testEquals() {
        assertTrue("Not Equals!", test.equals(new UserData(UUID.fromString("7f8149a0-b5a5-4fcd-80b5-6cff083a99f1"), 0, null, true, GameMode.CREATIVE, null, "Testname", true)));
    }

    @Test
    public void testEqualsNot() {
        UserData notEqual = new UserData(UUID.fromString("7f8149a0-b5a5-4fcd-80b5-6cff083a99f1"), 0, null, true, GameMode.CREATIVE, null, "WRONG", true);
        assertTrue("Equals!", !notEqual.equals(test));
    }

    @Test
    public void testEqualsNot2() {
        Object o = "NOT";
        assertTrue("Equals!", !test.equals(o));
    }

}
