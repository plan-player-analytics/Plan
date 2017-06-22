package main.java.com.djrapitops.plan.command;

/**
 * Container class for boolean and a string.
 *
 * @author Rsl1122
 * @since 3.4.3
 * @deprecated Stupid idea.
 */
@Deprecated
public class Condition {

    final private String failMsg;
    final private boolean pass;

    /**
     * Constructor. 
     * @param pass Did the condition pass?
     * @param failMsg Message to send if the condition failed.
     */
    @Deprecated
    public Condition(boolean pass, String failMsg) {
        this.failMsg = failMsg;
        this.pass = pass;
    }

    /**
     *
     * @return
     */
    @Deprecated
    public String getFailMsg() {
        return failMsg;
    }

    /**
     *
     * @return
     */
    @Deprecated
    public boolean pass() {
        return pass;
    }
}
