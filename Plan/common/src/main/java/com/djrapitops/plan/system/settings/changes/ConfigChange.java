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
package com.djrapitops.plan.system.settings.changes;

import com.djrapitops.plugin.config.Config;
import com.djrapitops.plugin.config.ConfigNode;

import java.lang.reflect.Field;
import java.util.Collection;

/**
 * Represents a change made to the config structure.
 *
 * @author Rsl1122
 */
public interface ConfigChange {

    boolean hasBeenApplied(Config config);

    void apply(Config config);

    String getAppliedMessage();

    class Moved extends Removed {

        private final String newPath;

        public Moved(String oldPath, String newPath) {
            super(oldPath);
            this.newPath = newPath;
        }

        @Override
        public void apply(Config config) {
            ConfigNode newNode = config.getConfigNode(newPath);
            ConfigNode oldNode = config.getConfigNode(oldPath);
            newNode.copyDefaults(oldNode);
            newNode.set(oldNode.getString());
            removeNode(oldNode);
        }

        @Override
        public String getAppliedMessage() {
            return "Moved " + oldPath + " to " + newPath;
        }
    }

    class Copied extends Removed {

        private final String newPath;

        public Copied(String oldPath, String newPath) {
            super(oldPath);
            this.newPath = newPath;
        }

        @Override
        public void apply(Config config) {
            ConfigNode newNode = config.getConfigNode(newPath);
            ConfigNode oldNode = config.getConfigNode(oldPath);
            newNode.copyDefaults(oldNode);
            newNode.set(oldNode.getString());
        }

        @Override
        public String getAppliedMessage() {
            return "Copied value of " + oldPath + " to " + newPath;
        }
    }

    class Removed implements ConfigChange {
        final String oldPath;

        public Removed(String oldPath) {
            this.oldPath = oldPath;
        }

        @Override
        public boolean hasBeenApplied(Config config) {
            return !config.contains(oldPath);
        }

        @Override
        public synchronized void apply(Config config) {
            ConfigNode node = config.getConfigNode(oldPath);
            removeNode(node);
        }

        void removeNode(ConfigNode node) {
            ConfigNode parent = node.getParent();
            String key = node.getKey(false);
            parent.getChildren().remove(key);

            // Remove the child from child order, since it is otherwise trying to save null.
            try {
                Field field = ConfigNode.class.getDeclaredField("childOrder");
                field.setAccessible(true);
                Collection<String> childOrder = (Collection<String>) field.get(parent);
                childOrder.remove(key);
                field.set(parent, childOrder);
                field.setAccessible(false);
            } catch (IllegalAccessException | NoSuchFieldException e) {
                throw new IllegalStateException("Error removing config node", e);
            }
        }

        @Override
        public String getAppliedMessage() {
            return "Removed " + oldPath;
        }
    }

}
