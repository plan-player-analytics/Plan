/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.java.com.djrapitops.plan.utilities.comparators;

import main.java.com.djrapitops.plan.data.UserInfo;

import java.util.Comparator;

/**
 * @author Risto
 */
public class UserDataLastPlayedComparator implements Comparator<UserInfo> {

    @Override
    public int compare(UserInfo u1, UserInfo u2) {
        return Long.compare(u2.getLastSeen(), u1.getLastSeen());
    }
}
