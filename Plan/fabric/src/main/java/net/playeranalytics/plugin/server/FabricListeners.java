/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package net.playeranalytics.plugin.server;

import net.playeranalytics.plan.gathering.listeners.FabricListener;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class FabricListeners implements Listeners {

    private final Set<FabricListener> listeners;

    public FabricListeners() {
        this.listeners = Collections.newSetFromMap(new ConcurrentHashMap<>());
    }

    @Override
    public void registerListener(Object listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Listener can not be null!");
        } else if (listener instanceof FabricListener fabricListener) {
            if (!fabricListener.isEnabled()) {
                fabricListener.register();
                listeners.add(fabricListener);
            }
        } else {
            throw new IllegalArgumentException("Listener needs to be of type " + listener.getClass().getName() + ", but was " + listener.getClass());
        }
    }

    @Override
    public void unregisterListener(Object listener) {
        ((FabricListener) listener).disable();
        listeners.remove(listener);
    }

    @Override
    public void unregisterListeners() {
        listeners.forEach(FabricListener::disable);
        listeners.clear();
    }
}
