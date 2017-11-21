/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.java.com.djrapitops.plan.utilities.comparators;

import main.java.com.djrapitops.plan.data.UserInfo;

import java.util.Comparator;

/**
 * Comparator for UserInfo for Alphabetical Name order.
 *
 * @author Rsl1122
 */
public class UserInfoNameComparator implements Comparator<UserInfo> {

    @Override
    public int compare(UserInfo u1, UserInfo u2) {
        return u1.getName().compareTo(u2.getName());
    }
}
