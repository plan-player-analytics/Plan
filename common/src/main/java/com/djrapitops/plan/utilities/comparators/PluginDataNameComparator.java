/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.djrapitops.plan.utilities.comparators;

import com.djrapitops.plan.data.plugin.PluginData;

import java.util.Comparator;

/**
 * Comparator for PluginData for Alphabetical Name order.
 *
 * @author Rsl1122
 */
public class PluginDataNameComparator implements Comparator<PluginData> {

    @Override
    public int compare(PluginData u1, PluginData u2) {
        return u1.getSourcePlugin().compareTo(u2.getSourcePlugin());
    }
}
