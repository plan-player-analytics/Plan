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
package com.djrapitops.plan.settings.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class ConfigNodeTest {

    private static final String SIMPLE_STRING_NODE = "Simple_string_node";
    private static final String STRING_NODE_WITH_QUOTES = "String_node_with_quotes";
    private static final String STRING_NODE_WITH_DOUBLE_QUOTES = "String_node_with_double_quotes";
    private static final String FIRST_LEVEL = "1st_level";
    private static final String SECOND_LEVEL = "2nd_level";
    private static final String THIRD_LEVEL = "3rd_level";

    private ConfigNode testTree;

    @BeforeEach
    void prepareTree() {
        testTree = new ConfigNode(null, null, null);
        testTree.addChild(new ConfigNode(SIMPLE_STRING_NODE, testTree, "String"));
        testTree.addChild(new ConfigNode(STRING_NODE_WITH_QUOTES, testTree, "\"'String'\""));
        testTree.addChild(new ConfigNode(STRING_NODE_WITH_DOUBLE_QUOTES, testTree, "'\"String\"'"));
        ConfigNode emptyNode = new ConfigNode(FIRST_LEVEL, testTree, null);
        ConfigNode secondLevel = new ConfigNode(SECOND_LEVEL, emptyNode, "String");
        testTree.addChild(emptyNode);
        emptyNode.addChild(secondLevel);
        secondLevel.addChild(new ConfigNode(THIRD_LEVEL, secondLevel, "3"));

        assertTrue(testTree.childNodes.containsKey(SIMPLE_STRING_NODE), "Tree construction failed, addChild does not work.");
    }

    @Test
    void rootNodeReturnsDepthZero() {
        assertEquals(-1, testTree.getNodeDepth());
    }

    @Test
    void depthOneIsReturnedCorrectly() {
        assertEquals(0, testTree.childNodes.get(SIMPLE_STRING_NODE).getNodeDepth());
    }

    @Test
    void depthTwoIsReturnedCorrectly() {
        assertEquals(1, testTree.childNodes.get(FIRST_LEVEL).childNodes.get(SECOND_LEVEL).getNodeDepth());
    }

    @Test
    void firstLevelNodeIsFound() {
        assertTrue(testTree.getNode(SIMPLE_STRING_NODE).isPresent(), "Node was not found");
    }

    @Test
    void secondLevelNodeIsFound() {
        assertTrue(testTree.getNode(FIRST_LEVEL + "." + SECOND_LEVEL).isPresent(), "Node was not found");
    }

    @Test
    void normalStringValueIsParsedCorrectly() {
        String expected = "String";
        String result = testTree.childNodes.get(SIMPLE_STRING_NODE).getString();
        assertEquals(expected, result);
    }

    @Test
    void normalStringValueIsParsedCorrectlyViaPath() {
        String expected = "String";
        String result = testTree.getString(SIMPLE_STRING_NODE);
        assertEquals(expected, result);
    }

    @Test
    void quotedStringValueIsParsedCorrectly() {
        String expected = "'String'";
        String result = testTree.childNodes.get(STRING_NODE_WITH_QUOTES).getString();
        assertEquals(expected, result);
    }

    @Test
    void quotedStringValueIsParsedCorrectlyViaPath() {
        String expected = "'String'";
        String result = testTree.getString(STRING_NODE_WITH_QUOTES);
        assertEquals(expected, result);
    }

    @Test
    void doubleQuotedStringValueIsParsedCorrectly() {
        String expected = "\"String\"";
        String result = testTree.childNodes.get(STRING_NODE_WITH_DOUBLE_QUOTES).getString();
        assertEquals(expected, result);
    }

    @Test
    void doubleQuotedStringValueIsParsedCorrectlyViaPath() {
        String expected = "\"String\"";
        String result = testTree.getString(STRING_NODE_WITH_DOUBLE_QUOTES);
        assertEquals(expected, result);
    }

    @Test
    void removeFirstLevelChildRemovesFirstAndSecondLevelChildren() {
        assertTrue(testTree.childNodes.containsKey(FIRST_LEVEL));

        ConfigNode removedNode = testTree.childNodes.get(FIRST_LEVEL);

        testTree.removeNode(FIRST_LEVEL);

        assertFalse(testTree.getNode(FIRST_LEVEL).isPresent());
        assertFalse(testTree.getNode(FIRST_LEVEL + "." + SECOND_LEVEL).isPresent());
        assertNull(removedNode.parent);
    }

    @Test
    void removeSecondLevelChildRemovesOnlySecondLevelChild() {
        assertTrue(testTree.childNodes.get(FIRST_LEVEL).childNodes.containsKey(SECOND_LEVEL));

        testTree.removeNode(FIRST_LEVEL + "." + SECOND_LEVEL);

        assertTrue(testTree.getNode(FIRST_LEVEL).isPresent());
        assertFalse(testTree.getNode(FIRST_LEVEL + "." + SECOND_LEVEL).isPresent());
    }

    @Test
    void getNodeDoesNotAddNodes() {
        assertFalse(testTree.getNode("NonexistentNode").isPresent());
    }

    @Test
    void childNodesAreSorted() {
        List<String> nodeOrder = new ArrayList<>(testTree.nodeOrder);

        testTree.sort();

        assertNotEquals(nodeOrder, testTree.nodeOrder);
        Collections.sort(nodeOrder);
        assertEquals(nodeOrder, testTree.nodeOrder);
    }

    @Test
    void addChildUpdatesParent() {
        ConfigNode adding = new ConfigNode("Path", /*null parent*/null, null);
        testTree.addChild(adding);
        assertSame(testTree, adding.parent);
    }

    @Test
    void addFirstLevelNodeAddsSingleChild() {
        testTree.addNode("Path");
        assertTrue(testTree.getNode("Path").isPresent());
    }

    @Test
    void addSecondLevelNodeAddsAllNodesInPath() {
        testTree.addNode("Path.Path");
        assertTrue(testTree.getNode("Path").isPresent());
        assertTrue(testTree.getNode("Path.Path").isPresent());
    }

    @Test
    void moveMovesNodeToNewPath() {
        String oldPath = SIMPLE_STRING_NODE;
        String newPath = "New_path";

        ConfigNode movedNode = testTree.childNodes.get(oldPath);
        ConfigNode oldParent = movedNode.parent;
        assertSame(testTree, oldParent);

        System.out.println("Root: " + testTree);
        testTree.moveChild(oldPath, newPath);

        // New has proper values
        ConfigNode newNode = testTree.getNode(newPath).orElseThrow(AssertionError::new);
        assertEquals(movedNode.value, newNode.value);
        assertEquals(movedNode.comment, newNode.comment);
        assertEquals(movedNode.nodeOrder, newNode.nodeOrder);
        assertSame(oldParent, newNode.parent);

        // Old has been removed
        assertFalse(testTree.getNode(oldPath).isPresent());
        assertNull(movedNode.parent);
    }

    @Test
    void movePreservesParentNodes() {
        String oldPath = FIRST_LEVEL + "." + SECOND_LEVEL;
        String newPath = "New_Path";

        String oldValue = "TestValue";
        ConfigNode node = testTree.getNode(FIRST_LEVEL).orElseThrow(AssertionError::new);
        node.set(oldValue);

        testTree.moveChild(oldPath, newPath);

        ConfigNode parentNode = testTree.getNode(FIRST_LEVEL).orElseThrow(AssertionError::new);
        assertEquals(oldValue, parentNode.getString());
    }

    @Test
    void moveMovesChildNodes() {
        String oldPath = FIRST_LEVEL + "." + SECOND_LEVEL;
        String newPath = "New_Path";

        String oldValue = testTree.getNode(oldPath + "." + THIRD_LEVEL)
                .map(node -> node.value).orElseThrow(AssertionError::new);

        testTree.moveChild(oldPath, newPath);

        ConfigNode childNode = testTree.getNode(newPath + "." + THIRD_LEVEL).orElseThrow(AssertionError::new);
        assertEquals(oldValue, childNode.value);
    }

    @Test
    void moveWithChildNodesCopiesRootValue() {
        String oldPath = FIRST_LEVEL + "." + SECOND_LEVEL;
        String newPath = "New_Path";

        String oldValue = testTree.getNode(oldPath)
                .map(node -> node.value).orElseThrow(AssertionError::new);

        testTree.moveChild(oldPath, newPath);

        ConfigNode node = testTree.getNode(newPath).orElseThrow(AssertionError::new);
        assertEquals(oldValue, node.value);
    }

    @Test
    void nonExistingNodeIsNotMoved() {
        assertFalse(testTree.moveChild("non-existing", "to"));
        assertFalse(testTree.getNode("to").isPresent());
    }

    @Test
    void deepKeyIsFullPath() {
        String expected = FIRST_LEVEL + "." + SECOND_LEVEL;
        String result = testTree.getNode(expected).map(node -> node.getKey(true)).orElse("FAIL");
        assertEquals(expected, result);
    }

    @Test
    void shallowKeyIsOnlyLastPart() {
        assertEquals(
                SECOND_LEVEL,
                testTree.getNode(FIRST_LEVEL + "." + SECOND_LEVEL)
                        .map(node -> node.getKey(false))
                        .orElse("FAIL")
        );
    }

    @Test
    void settingValueAddsMissingNodes() {
        String expectedValue = "Value";
        testTree.set("Path.Path", expectedValue);

        Optional<ConfigNode> addedNode = testTree.getNode("Path.Path");
        assertTrue(addedNode.isPresent());
        assertEquals(expectedValue, addedNode.get().getString());
    }

    @Test
    void settingValueIsSet() {
        String expectedValue = "NewValue";
        testTree.set(SIMPLE_STRING_NODE, expectedValue);

        assertEquals(expectedValue, testTree.getString(SIMPLE_STRING_NODE));
    }

    @Test
    void getStringReturnsValue() {
        String expected = "Value";
        ConfigNode adding = new ConfigNode("Path", null, expected);
        String result = adding.getString();
        assertEquals(expected, result);
    }

    @Test
    void nonExistentStringValueIsNotParsed() {
        assertNull(testTree.getString("Non-existent"));
    }

    @Test
    void nullValueIsReturnedAsNull() {
        assertNull(testTree.getString(FIRST_LEVEL));
    }

    @Test
    void settingConfigNodeOverridesValues() {
        ConfigNode adding = new ConfigNode(null, null, "NewValue");
        testTree.set(SIMPLE_STRING_NODE, adding);
        assertEquals(
                adding.getString(),
                testTree.getString(SIMPLE_STRING_NODE)
        );
    }

    @Test
    void settingConfigNodeAddsNewNodes() {
        ConfigNode adding = new ConfigNode(null, null, "NewValue");
        testTree.set("Path", adding);
        assertEquals(
                adding.getString(),
                testTree.getString("Path")
        );
    }

    @Test
    void settingConfigNodeCopiesChildren() {
        ConfigNode adding = testTree.getNode(FIRST_LEVEL).orElseThrow(() -> new AssertionError("Fail"));
        testTree.set(SIMPLE_STRING_NODE, adding);
        assertTrue(testTree.getNode(SIMPLE_STRING_NODE + "." + SECOND_LEVEL).isPresent());
    }

    @Test
    void copyAllOverridesAllValues() {
        ConfigNode overridingWith = testTree.getNode(FIRST_LEVEL).orElseThrow(() -> new AssertionError("Fail"));
        ConfigNode added = testTree.addNode("Test." + SECOND_LEVEL + "." + THIRD_LEVEL);
        added.set("ORIGINAL");
        ConfigNode node = testTree.getNode("Test").orElseThrow(() -> new AssertionError("Fail"));
        node.copyAll(overridingWith);

        String original = testTree.getString(FIRST_LEVEL);
        String copied = testTree.getString("Test");
        assertEquals(original, copied);

        original = testTree.getString(FIRST_LEVEL + "." + SECOND_LEVEL);
        copied = testTree.getString("Test." + SECOND_LEVEL);
        assertEquals(original, copied);

        original = testTree.getString(FIRST_LEVEL + "." + SECOND_LEVEL + "." + THIRD_LEVEL);
        copied = testTree.getString("Test." + SECOND_LEVEL + "." + THIRD_LEVEL);
        assertEquals(original, copied);
    }

    @Test
    void copyDefaultAddsProperValues() {
        ConfigNode overridingWith = testTree.getNode(FIRST_LEVEL).orElseThrow(() -> new AssertionError("Fail"));
        ConfigNode added = testTree.addNode("Test." + SECOND_LEVEL + "." + THIRD_LEVEL);
        added.set("ORIGINAL");
        ConfigNode node = testTree.getNode("Test").orElseThrow(() -> new AssertionError("Fail"));
        node.copyMissing(overridingWith);

        String original = testTree.getString(FIRST_LEVEL);
        String copied = testTree.getString("Test");
        assertEquals(original, copied);

        original = testTree.getString(FIRST_LEVEL + "." + SECOND_LEVEL);
        copied = testTree.getString("Test." + SECOND_LEVEL);
        assertEquals(original, copied);

        copied = testTree.getString("Test." + SECOND_LEVEL + "." + THIRD_LEVEL);
        assertEquals("ORIGINAL", copied);
    }

    @TestFactory
    Collection<DynamicTest> copyMissingCorrectnessTests() {
        return Arrays.stream(new String[][]{
                new String[]{"", "Value"},
                new String[]{null, "Value"},
                new String[]{"Value", ""},
                new String[]{"Value", null}
        }).map(valuePair -> {
            String previousValue = valuePair[0];
            String overridingValue = valuePair[1];
            return DynamicTest.dynamicTest("ConfigNode#copyMissing sets 'Value' correctly '" + previousValue + "', '" + overridingValue + "'",
                    () -> {
                        ConfigNode underTest = new ConfigNode("Test", null, previousValue);
                        ConfigNode copyFrom = new ConfigNode("Test", null, overridingValue);

                        underTest.copyMissing(copyFrom);

                        assertEquals("Value", underTest.getString());
                    });
        }).collect(Collectors.toList());
    }

    @Test
    void environmentVariableString() {
        ConfigNode node = new ConfigNode("Test.Node.String", new ConfigNode("", null, null), null);
        String expected = "String";
        String result = node.getString();
        assertEquals(expected, result);
    }

    @Test
    void environmentVariableBoolean() {
        ConfigNode node = new ConfigNode("Test.Node.Boolean", new ConfigNode("", null, null), null);
        Boolean expected = true;
        Boolean result = node.getBoolean();
        assertEquals(expected, result);
    }

    @Test
    void environmentVariableInteger() {
        ConfigNode node = new ConfigNode("Test.Node.Integer", new ConfigNode("", null, null), null);
        Integer expected = 5;
        Integer result = node.getInteger();
        assertEquals(expected, result);
    }

    @Test
    void environmentVariableDouble() {
        ConfigNode node = new ConfigNode("Test.Node.Double", new ConfigNode("", null, null), null);
        Double expected = 0.5;
        Double result = node.getDouble();
        assertEquals(expected, result);
    }

    @Test
    void environmentVariableLong() {
        ConfigNode node = new ConfigNode("Test.Node.Long", new ConfigNode("", null, null), null);
        Long expected = Long.MAX_VALUE;
        Long result = node.getLong();
        assertEquals(expected, result);
    }

    @Test
    void environmentVariableStringList() {
        ConfigNode node = new ConfigNode("Test.Node.StringList", new ConfigNode("", null, null), null);
        List<String> expected = List.of("Test", "Another");
        List<String> result = node.getStringList();
        assertEquals(expected, result);
    }
}