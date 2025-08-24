/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2021 AuroraLS3
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.djrapitops.plan.settings.config;

import com.djrapitops.plan.utilities.UnitSemaphoreAccessLock;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * Represents a single node in a configuration file
 *
 * @author AuroraLS3
 */
public class ConfigNode {

    protected final UnitSemaphoreAccessLock nodeModificationLock = new UnitSemaphoreAccessLock();

    protected final String key;
    protected ConfigNode parent;

    protected List<String> nodeOrder;
    protected final Map<String, ConfigNode> childNodes;
    protected List<String> comment;

    protected String value;

    public ConfigNode(String key, ConfigNode parent, String value) {
        this.key = key;
        this.parent = parent;
        this.value = value;

        nodeOrder = new CopyOnWriteArrayList<>();
        childNodes = new HashMap<>();
        comment = new ArrayList<>();
    }

    protected void updateParent(ConfigNode newParent) {
        parent = newParent;
    }

    public Optional<ConfigNode> getNode(String path) {
        if (path == null) {
            return Optional.empty();
        }
        String[] parts = splitPathInTwo(path);
        String lookingFor = parts[0];
        String leftover = parts[1];

        if (leftover.isEmpty()) {
            return Optional.ofNullable(childNodes.get(lookingFor));
        } else {
            return getNode(lookingFor).flatMap(child -> child.getNode(leftover));
        }
    }

    private String[] splitPathInTwo(String path) {
        String[] split = StringUtils.split(path, ".", 2);
        if (split.length <= 1) {
            return new String[]{split[0], ""};
        }
        return split;
    }

    public boolean contains(String path) {
        return getNode(path).isPresent();
    }

    public ConfigNode addNode(String path) {
        ConfigNode newParent = this;
        if (path != null && !path.isEmpty()) {
            String[] parts = splitPathInTwo(path);
            String lookingFor = parts[0];
            String leftover = parts[1];

            // Add a new child
            ConfigNode child;
            if (!childNodes.containsKey(lookingFor)) {
                child = addChild(new ConfigNode(lookingFor, newParent, null));
            } else {
                child = childNodes.get(lookingFor);
            }

            // If the path ends return the leaf node
            // Otherwise continue recursively.
            return leftover.isEmpty() ? child : child.addNode(leftover);
        }
        throw new IllegalArgumentException("Can not add a node with path '" + path + "'");
    }

    /**
     * Remove a node at a certain path.
     *
     * @param path Path to the node that is up for removal.
     * @return {@code true} if the node was present and is now removed. {@code false} if the path did not have a node.
     */
    public boolean removeNode(String path) {
        Optional<ConfigNode> node = getNode(path);
        node.ifPresent(ConfigNode::remove);
        return node.isPresent();
    }

    public void remove() {
        if (parent == null) {
            throw new IllegalStateException("Can not remove root node from a tree.");
        }
        nodeModificationLock.enter();
        parent.nodeOrder.remove(key);
        parent.childNodes.remove(key);
        nodeModificationLock.exit();

        updateParent(null);

        // Remove children recursively to avoid memory leaks
        nodeOrder.stream()
                .sorted() // will use internal state and prevent Concurrent modification of underlying list
                .map(childNodes::get)
                .filter(Objects::nonNull)
                .forEach(ConfigNode::remove);
    }

    /**
     * Add a new child ConfigNode.
     *
     * @param child ConfigNode to add.
     *              If from another config tree, the parent is 'cut', which breaks the old tree traversal.
     * @return Return the node given, now part of this tree.
     */
    protected ConfigNode addChild(ConfigNode child) {
        getNode(child.key).ifPresent(ConfigNode::remove);

        nodeModificationLock.enter();
        childNodes.put(child.key, child);
        nodeOrder.add(child.key);
        nodeModificationLock.exit();

        child.updateParent(this);
        return child;
    }

    protected void removeChild(ConfigNode child) {
        removeNode(child.key);
    }

    /**
     * Moves a node from old path to new path.
     *
     * @param oldPath Old path of the node.
     * @param newPath New path of the node.
     * @return {@code true} if the move was successful. {@code false} if the new node is not present
     */
    public boolean moveChild(String oldPath, String newPath) {
        Optional<ConfigNode> found = getNode(oldPath);
        if (found.isEmpty()) {
            return false;
        }

        ConfigNode moveFrom = found.get();
        ConfigNode moveTo = addNode(newPath);

        moveTo.copyAll(moveFrom);
        removeNode(oldPath);

        return getNode(newPath).isPresent();
    }

    public String getKey(boolean deep) {
        if (deep) {
            String deepKey = parent != null ? parent.getKey(true) + "." + key : "";
            if (deepKey.startsWith(".")) {
                return deepKey.substring(1);
            }
            return deepKey;
        }
        return key;
    }

    private String getEnvironmentVariableKey() {
        String deepKey = parent != null ? parent.getKey(true) + "." + key : "";
        if (deepKey.startsWith(".")) {
            deepKey = deepKey.substring(1);
        }
        return "PLAN_" + StringUtils.replaceChars(StringUtils.upperCase(deepKey), '.', '_');
    }

    public void sort() {
        Collections.sort(nodeOrder);
    }

    public void reorder(List<String> newOrder) {
        nodeModificationLock.enter();
        List<String> oldOrder = nodeOrder;
        nodeOrder = new ArrayList<>();
        for (String childKey : newOrder) {
            if (childNodes.containsKey(childKey)) {
                nodeOrder.add(childKey);
            }
        }
        // Add those that were not in the new order, but are in the old order.
        oldOrder.removeAll(nodeOrder);
        nodeOrder.addAll(oldOrder);
        nodeModificationLock.exit();
    }

    /**
     * Find the root node and save.
     *
     * @throws IOException If the save can not be performed.
     */
    public void save() throws IOException {
        ConfigNode root = this.parent;
        while (root.parent != null) {
            root = root.parent;
        }
        root.save();
    }

    public <T> void set(String path, T value) {
        addNode(path).set(value);
    }

    public <T> void set(T value) {
        if (value == null) {
            this.value = null;
        } else if (value instanceof ConfigNode) {
            copyAll((ConfigNode) value);
        } else {
            ConfigValueParser<T> parser = ConfigValueParser.getParserFor(value.getClass());
            this.value = parser.decompose(value);
        }
    }

    public List<String> getComment() {
        return comment;
    }

    public void setComment(List<String> comment) {
        this.comment = comment;
    }

    private String getEnvironmentVariable() {
        String key = getEnvironmentVariableKey();
        String variable = System.getenv(key);
        Map<String, String> env = System.getenv();
        return variable;
    }

    public List<String> getStringList() {
        String environmentVariable = getEnvironmentVariable();
        if (environmentVariable != null) return new ConfigValueParser.StringListParser().compose(environmentVariable);
        return value == null ? Collections.emptyList()
                : new ConfigValueParser.StringListParser().compose(value);
    }

    public Integer getInteger() {
        String environmentVariable = getEnvironmentVariable();
        if (environmentVariable != null) return new ConfigValueParser.IntegerParser().compose(environmentVariable);
        return value == null ? null
                : new ConfigValueParser.IntegerParser().compose(value);
    }

    public Long getLong() {
        String environmentVariable = getEnvironmentVariable();
        if (environmentVariable != null) return new ConfigValueParser.LongParser().compose(environmentVariable);
        return value == null ? null
                : new ConfigValueParser.LongParser().compose(value);
    }

    public String getString() {
        String environmentVariable = getEnvironmentVariable();
        if (environmentVariable != null) return new ConfigValueParser.StringParser().compose(environmentVariable);
        return value == null ? null
                : new ConfigValueParser.StringParser().compose(value);
    }

    public Double getDouble() {
        String environmentVariable = getEnvironmentVariable();
        if (environmentVariable != null) return new ConfigValueParser.DoubleParser().compose(environmentVariable);
        return value == null ? null
                : new ConfigValueParser.DoubleParser().compose(value);
    }

    public boolean getBoolean() {
        String environmentVariable = getEnvironmentVariable();
        if (environmentVariable != null) return new ConfigValueParser.BooleanParser().compose(environmentVariable);
        return new ConfigValueParser.BooleanParser().compose(value);
    }

    public List<String> getStringList(String path) {
        return getNode(path).map(ConfigNode::getStringList).orElse(Collections.emptyList());
    }

    /**
     * Return values in a Map.
     *
     * @param fullKeys Should the key be full keys of the Config node.
     * @return Map with Config key - ConfigNode#getString.
     */
    public Map<String, String> getStringMap(boolean fullKeys) {
        return childNodes.values().stream()
                .collect(Collectors.toMap(node -> node.getKey(fullKeys), ConfigNode::getString));
    }

    /**
     * @return List of config keys
     */
    public List<String> getConfigPaths() {
        ArrayDeque<ConfigNode> dfs = new ArrayDeque<>();
        dfs.push(this);

        List<String> configPaths = new ArrayList<>();
        while (!dfs.isEmpty()) {
            ConfigNode next = dfs.pop();
            if (next.isLeafNode()) {
                configPaths.add(next.getKey(true));
            } else {
                dfs.addAll(next.getChildren());
            }
        }
        return configPaths;
    }

    public <T> List<T> dfs(BiConsumer<ConfigNode, List<T>> accessVisitor) {
        ArrayDeque<ConfigNode> dfs = new ArrayDeque<>();
        dfs.push(this);

        List<T> result = new ArrayList<>();
        while (!dfs.isEmpty()) {
            ConfigNode next = dfs.pop();
            accessVisitor.accept(next, result);
            dfs.addAll(next.getChildren());
        }
        return result;
    }

    public Integer getInteger(String path) {
        return getNode(path).map(ConfigNode::getInteger).orElse(null);
    }

    public Long getLong(String path) {
        return getNode(path).map(ConfigNode::getLong).orElse(null);
    }

    public String getString(String path) {
        return getNode(path).map(ConfigNode::getString).orElse(null);
    }

    public boolean getBoolean(String path) {
        return getNode(path).map(ConfigNode::getBoolean).orElse(false);
    }

    public Double getDouble(String path) {
        return getNode(path).map(ConfigNode::getDouble).orElse(null);
    }

    public void copyMissing(ConfigNode from) {
        // Override comment conditionally
        if (comment.size() < from.comment.size()) {
            comment = from.comment;
        }

        // Override value conditionally
        boolean currentValueIsMissing = value == null || value.isEmpty();
        boolean otherNodeHasValue = from.value != null && !from.value.isEmpty();
        if (currentValueIsMissing && otherNodeHasValue) {
            value = from.value;
        }

        // Copy all nodes from 'from'
        for (String childKey : from.nodeOrder) {
            ConfigNode newChild = from.childNodes.get(childKey);

            // Copy values recursively to children
            ConfigNode created = addNode(childKey);
            created.copyMissing(newChild);
        }
    }

    public void copyAll(ConfigNode from) {
        // Override comment and value unconditionally.
        comment = from.comment;
        value = from.value;

        // Copy all nodes from 'from'
        for (String childKey : from.nodeOrder) {
            ConfigNode newChild = from.childNodes.get(childKey);

            // Copy values recursively to children
            ConfigNode created = addNode(childKey);
            created.copyAll(newChild);
        }
    }

    public void copyValue(ConfigNode from) {
        comment = from.comment;
        value = from.value;
    }

    protected int getNodeDepth() {
        return parent != null ? parent.getNodeDepth() + 1 : -1; // Root node is -1
    }

    public ConfigNode getParent() {
        return parent;
    }

    public boolean isLeafNode() {
        return nodeOrder.isEmpty();
    }

    protected List<String> getNodeOrder() {
        return nodeOrder;
    }

    public Collection<ConfigNode> getChildren() {
        return childNodes.values();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConfigNode)) return false;
        ConfigNode that = (ConfigNode) o;
        return Objects.equals(key, that.key) &&
                nodeOrder.equals(that.nodeOrder) &&
                childNodes.equals(that.childNodes) &&
                comment.equals(that.comment) &&
                Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, childNodes, comment, value);
    }

    @Override
    public String toString() {
        return "{'" + value + "' " + (!childNodes.isEmpty() ? childNodes : "") + '}';
    }
}
