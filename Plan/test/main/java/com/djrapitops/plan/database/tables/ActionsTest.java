package main.java.com.djrapitops.plan.database.tables;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ActionsTest {

    @Test
    public void getUnknownActionsEnum() {
        Actions action = Actions.getById(Integer.MIN_VALUE);
        assertEquals(Actions.UNKNOWN, action);
    }

}