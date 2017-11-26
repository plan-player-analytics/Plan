/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.java.com.djrapitops.plan.utilities.comparators;

import main.java.com.djrapitops.plan.data.additional.PluginData;

import java.util.Comparator;

/**
 * Comparator for UserInfo for Alphabetical Name order.
 *
 * @author Rsl1122
 */
public class PluginDataNameComparator implements Comparator<PluginData> {

    @Override
    public int compare(PluginData u1, PluginData u2) {
        return u1.getSourcePlugin().compareTo(u2.getSourcePlugin());
    }
}
