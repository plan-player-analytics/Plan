/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.djrapitops.plan.utilities.comparators;

import com.djrapitops.plan.utilities.analysis.Point;

import java.util.Comparator;

/**
 * @author Rsl1122
 * @since 3.5.2
 */
public class PointComparator implements Comparator<Point> {

    @Override
    public int compare(Point o1, Point o2) {
        return Double.compare(o1.getX(), o2.getX());
    }

}
