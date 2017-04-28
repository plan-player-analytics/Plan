/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.java.com.djrapitops.plan.data.handling;

import main.java.com.djrapitops.plan.data.UserData;

/**
 *
 * @author Rsl1122
 */
public class LogoutHandling {

    /**
     *
     * @param data
     * @param time
     * @param banned
     */
    public static void processLogoutInfo(UserData data, long time, boolean banned) {
        data.setPlayTime(data.getPlayTime() + (time - data.getLastPlayed()));
        data.setLastPlayed(time);        
        data.updateBanned(banned);
    }
}
