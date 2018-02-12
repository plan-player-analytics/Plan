/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.djrapitops.plan.utilities.comparators;

import com.djrapitops.plan.data.container.UserInfo;

import java.util.Comparator;

/**
 * Comparator for UserInfo so that most recently seen is first.
 *
 * @author Rsl1122
 */
public class UserInfoLastPlayedComparator implements Comparator<UserInfo> {

    @Override
    public int compare(UserInfo u1, UserInfo u2) {
        return Long.compare(u2.getLastSeen(), u1.getLastSeen());
    }
}
