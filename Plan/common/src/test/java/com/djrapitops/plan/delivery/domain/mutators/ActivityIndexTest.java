package com.djrapitops.plan.delivery.domain.mutators;

import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.locale.Message;
import com.djrapitops.plan.settings.locale.lang.HtmlLang;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ActivityIndexTest {

    @Test
    void localeIsUsedWhenGivenForGetGroups() {
        Locale locale = createTestLocale();

        String[] expected = new String[]{"A", "B", "C", "D", "E"};
        String[] result = ActivityIndex.getGroups(locale);
        assertArrayEquals(expected, result);
    }

    private Locale createTestLocale() {
        Locale locale = new Locale();
        locale.put(HtmlLang.INDEX_VERY_ACTIVE, new Message("A"));
        locale.put(HtmlLang.INDEX_ACTIVE, new Message("B"));
        locale.put(HtmlLang.INDEX_REGULAR, new Message("C"));
        locale.put(HtmlLang.INDEX_IRREGULAR, new Message("D"));
        locale.put(HtmlLang.INDEX_INACTIVE, new Message("E"));
        return locale;
    }

    @Test
    void localeDefaultGroups() {
        String[] expected = new String[]{"Very Active", "Active", "Regular", "Irregular", "Inactive"};
        String[] result = ActivityIndex.getDefaultGroups();
        assertArrayEquals(expected, result);
    }

    @Test
    void veryActiveGroup() {
        String expected = HtmlLang.INDEX_VERY_ACTIVE.getDefault();
        String result = new ActivityIndex(ActivityIndex.VERY_ACTIVE, 0).getGroup();
        assertEquals(expected, result);
    }

    @Test
    void activeGroup() {
        String expected = HtmlLang.INDEX_ACTIVE.getDefault();
        String result = new ActivityIndex(ActivityIndex.ACTIVE, 0).getGroup();
        assertEquals(expected, result);
    }

    @Test
    void regularGroup() {
        String expected = HtmlLang.INDEX_REGULAR.getDefault();
        String result = new ActivityIndex(ActivityIndex.REGULAR, 0).getGroup();
        assertEquals(expected, result);
    }

    @Test
    void irregularGroup() {
        String expected = HtmlLang.INDEX_IRREGULAR.getDefault();
        String result = new ActivityIndex(ActivityIndex.IRREGULAR, 0).getGroup();
        assertEquals(expected, result);
    }

    @Test
    void inactiveGroup() {
        String expected = HtmlLang.INDEX_INACTIVE.getDefault();
        String result = new ActivityIndex(0.0, 0).getGroup();
        assertEquals(expected, result);
    }

    @Test
    void veryActiveGroupWithLocale() {
        String expected = "A";
        String result = new ActivityIndex(ActivityIndex.VERY_ACTIVE, 0).getGroup(createTestLocale());
        assertEquals(expected, result);
    }

    @Test
    void activeGroupWithLocale() {
        String expected = "B";
        String result = new ActivityIndex(ActivityIndex.ACTIVE, 0).getGroup(createTestLocale());
        assertEquals(expected, result);
    }

    @Test
    void regularGroupWithLocale() {
        String expected = "C";
        String result = new ActivityIndex(ActivityIndex.REGULAR, 0).getGroup(createTestLocale());
        assertEquals(expected, result);
    }

    @Test
    void irregularGroupWithLocale() {
        String expected = "D";
        String result = new ActivityIndex(ActivityIndex.IRREGULAR, 0).getGroup(createTestLocale());
        assertEquals(expected, result);
    }

    @Test
    void inactiveGroupWithLocale() {
        String expected = "E";
        String result = new ActivityIndex(0.0, 0).getGroup(createTestLocale());
        assertEquals(expected, result);
    }

}