/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2018 Risto Lahtela
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
package com.djrapitops.plan.system.settings.config;

import java.io.IOException;
import java.util.*;

/**
 * Represents a single node in a configuration file
 * <p>
 * Based on
 * https://github.com/Rsl1122/Abstract-Plugin-Framework/blob/72e221d3571ef200727713d10d3684c51e9f469d/AbstractPluginFramework/api/src/main/java/com/djrapitops/plugin/config/ConfigNode.java
 *
 * @author Rsl1122
 */
public class ConfigNode {

    protected final String key;
    protected ConfigNode parent;

    protected List<String> nodeOrder;
    protected Map<String, ConfigNode> childNodes;
    protected List<String> comment;

    protected String value;

    public ConfigNode(String key, ConfigNode parent, String value) {
        this.key = key;
        this.parent = parent;
        this.value = value;

        nodeOrder = new ArrayList<>();
        childNodes = new HashMap<>();
        comment = new ArrayList<>();
    }

    protected void updateParent(ConfigNode newParent) {
        parent = newParent;
    }

    public Optional<ConfigNode> getNode(String path) {
        String[] parts = path.split("\\.", 2);
        String key = parts[0];
        String leftover = parts[1];

        if (leftover.isEmpty()) {
            return Optional.ofNullable(childNodes.get(key));
        } else {
            return getNode(key).flatMap(child -> child.getNode(leftover));
        }
    }

    protected void addNode(String path) {
        ConfigNode newParent = this;
        if (!path.isEmpty()) {
            String[] parts = path.split("\\.", 2);
            String key = parts[0];
            String leftover = parts[1];

            if (!childNodes.containsKey(key)) {
                addChild(new ConfigNode(key, newParent, null));
            }
            ConfigNode child = childNodes.get(key);
            child.addNode(leftover);
        }
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
        parent.childNodes.remove(key);
        parent.nodeOrder.remove(key);
        updateParent(null);
    }

    protected void addChild(ConfigNode child) {
        getNode(child.key).ifPresent(ConfigNode::remove);
        childNodes.put(child.key, child);
        nodeOrder.add(child.key);
        child.updateParent(this);
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
        if (!found.isPresent()) {
            return false;
        }

        addNode(newPath);

        ConfigNode moveFrom = found.get();
        ConfigNode moveTo = getNode(newPath).orElseThrow(() -> new IllegalStateException("Config node was not added properly: " + newPath));
        ConfigNode oldParent = moveFrom.parent;
        ConfigNode newParent = moveTo.parent;
        oldParent.removeChild(moveFrom);
        newParent.addChild(moveTo);

        return getNode(newPath).isPresent();
    }

    public String getKey(boolean deep) {
        if (deep && parent != null) {
            String deepKey = parent.getKey(true) + "." + key;
            if (deepKey.startsWith(".")) {
                return deepKey.substring(1);
            }
            return deepKey;
        }
        return key;
    }

    public void sort() {
        Collections.sort(nodeOrder);
    }

    public boolean reorder(List<String> newOrder) {
        if (nodeOrder.containsAll(newOrder)) {
            nodeOrder = newOrder;
            return true;
        }
        return false;
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
        addNode(path);
        ConfigNode node = getNode(path).orElseThrow(() -> new IllegalStateException("Config node was not added properly: " + path));
        node.set(value);
    }

    public <T> void set(T value) {
        if (value instanceof ConfigNode) {
            addChild(((ConfigNode) value));
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

    public List<String> getStringList() {
        return new ConfigValueParser.StringListParser().compose(value);
    }

    public Integer getInteger() {
        return new ConfigValueParser.IntegerParser().compose(value);
    }

    public Long getLong() {
        return new ConfigValueParser.LongParser().compose(value);
    }

    public String getString() {
        return new ConfigValueParser.StringParser().compose(value);
    }

    public boolean isTrue() {
        return new ConfigValueParser.BooleanParser().compose(value);
    }

    public List<String> getStringList(String path) {
        return getNode(path).map(ConfigNode::getStringList).orElse(new ArrayList<>());
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

    public boolean isTrue(String path) {
        return getNode(path).map(ConfigNode::isTrue).orElse(false);
    }

    public void copyMissing(ConfigNode from) {
        if (comment.size() < from.comment.size()) {
            comment = from.comment;
        }

        if (value == null && from.value != null) {
            value = from.value;
        }

        for (String key : from.nodeOrder) {
            ConfigNode newChild = from.childNodes.get(key);

            if (childNodes.containsKey(key)) {
                ConfigNode oldChild = childNodes.get(key);
                oldChild.copyMissing(newChild);
            } else {
                addChild(newChild);
            }
        }
    }

    protected int getNodeDepth() {
        return parent != null ? parent.getNodeDepth() + 1 : 0;
    }
}
