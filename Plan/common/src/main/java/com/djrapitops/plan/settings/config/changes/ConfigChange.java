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
package com.djrapitops.plan.settings.config.changes;

import com.djrapitops.plan.settings.config.Config;
import com.djrapitops.plan.settings.config.ConfigNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Represents a change made to the config structure.
 *
 * @author AuroraLS3
 */
public interface ConfigChange {

    boolean hasBeenApplied(Config config);

    void apply(Config config);

    String getAppliedMessage();

    class Moved extends Removed {

        final String newPath;

        public Moved(String oldPath, String newPath) {
            super(oldPath);
            this.newPath = newPath;
        }

        @Override
        public synchronized void apply(Config config) {
            if (!config.moveChild(oldPath, newPath)) {
                throw new IllegalStateException("Failed to move config node from '" + oldPath + "' to '" + newPath + "'");
            }
        }

        @Override
        public String getAppliedMessage() {
            return "Moved " + oldPath + " to " + newPath;
        }
    }

    class MoveLevelDown implements ConfigChange {

        final String oldPath;
        final String newPath;

        public MoveLevelDown(String oldPath, String newPath) {
            this.oldPath = oldPath;
            this.newPath = newPath;
        }

        @Override
        public boolean hasBeenApplied(Config config) {
            return config.getNode(oldPath).isEmpty() || config.getNode(newPath).isPresent();
        }

        @Override
        public synchronized void apply(Config config) {
            if (!config.moveChild(oldPath, "Temp." + oldPath)) {
                throw new IllegalStateException("Failed to move config node from '" + oldPath + "' to 'Temp." + oldPath + "' while moving to '" + newPath + "'");
            }
            if (!config.moveChild("Temp." + oldPath, newPath)) {
                throw new IllegalStateException("Failed to move config node from 'Temp." + oldPath + "' to '" + newPath + "' while moving from '" + oldPath + "'");
            }
        }

        @Override
        public String getAppliedMessage() {
            return "Moved " + oldPath + " to " + newPath;
        }
    }

    class MovedValue implements ConfigChange {

        final String oldPath;
        final String newPath;

        public MovedValue(String oldPath, String newPath) {
            this.oldPath = oldPath;
            this.newPath = newPath;
        }

        @Override
        public boolean hasBeenApplied(Config config) {
            return config.getNode(oldPath)
                    .map(ConfigNode::getString)
                    .map(String::trim)
                    .filter(Predicate.not(String::isEmpty))
                    .isEmpty()
                    && config.getNode(newPath).isPresent();
        }

        @Override
        public void apply(Config config) {
            Optional<ConfigNode> oldNode = config.getNode(oldPath);
            if (oldNode.isPresent()) {
                ConfigNode node = oldNode.get();
                config.getNode(newPath)
                        .orElseGet(() -> config.addNode(newPath))
                        .copyValue(node);
                // Set value to null
                node.set(null);
                node.setComment(new ArrayList<>());
            }
        }

        @Override
        public String getAppliedMessage() {
            return "Moved " + oldPath + " to " + newPath;
        }
    }

    class Copied extends Removed {

        final String newPath;

        public Copied(String oldPath, String newPath) {
            super(oldPath);
            this.newPath = newPath;
        }

        @Override
        public synchronized void apply(Config config) {
            config.getNode(oldPath).ifPresent(oldNode -> config.addNode(newPath).copyAll(oldNode));
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
            return config.getNode(oldPath).isEmpty();
        }

        @Override
        public synchronized void apply(Config config) {
            if (!config.removeNode(oldPath)) {
                throw new IllegalStateException("Failed to remove config node from '" + oldPath + "'");
            }
        }

        @Override
        public String getAppliedMessage() {
            return "Removed " + oldPath;
        }
    }

    class RemovedComment implements ConfigChange {
        private final String path;

        public RemovedComment(String path) {
            this.path = path;
        }

        @Override
        public boolean hasBeenApplied(Config config) {
            Optional<ConfigNode> node = config.getNode(path);
            return node.isEmpty() || node.get().getComment().isEmpty();
        }

        @Override
        public void apply(Config config) {
            config.getNode(path).ifPresent(node -> node.setComment(Collections.emptyList()));
        }

        @Override
        public String getAppliedMessage() {
            return "Removed Comment from " + path;
        }
    }

    class BooleanToString implements ConfigChange {
        private final String oldPath;
        private final String newPath;
        private final String valueIfTrue;
        private final String valueIfFalse;

        public BooleanToString(String oldPath, String newPath, String valueIfTrue, String valueIfFalse) {
            this.oldPath = oldPath;
            this.newPath = newPath;
            this.valueIfTrue = valueIfTrue;
            this.valueIfFalse = valueIfFalse;
        }

        @Override
        public boolean hasBeenApplied(Config config) {
            Optional<ConfigNode> oldNode = config.getNode(oldPath);
            return oldNode.isEmpty();
        }

        @Override
        public void apply(Config config) {
            boolean oldValue = config.getBoolean(oldPath);
            config.set(newPath, oldValue ? valueIfTrue : valueIfFalse);
            config.removeNode(oldPath);
        }

        @Override
        public String getAppliedMessage() {
            return "Moved " + oldPath + " to " + newPath + " and turned Boolean to String.";
        }
    }
}
