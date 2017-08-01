/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.java.com.djrapitops.plan.utilities.comparators;

import main.java.com.djrapitops.plan.data.handling.info.HandlingInfo;

import java.util.Comparator;

/**
 * @author Rsl1122
 */
public class HandlingInfoTimeComparator implements Comparator<HandlingInfo> {

    @Override
    public int compare(HandlingInfo o1, HandlingInfo o2) {
        return Long.compare(o1.getTime(), o2.getTime());
    }

}
