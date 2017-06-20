/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.java.com.djrapitops.plan.command;

/**
 *
 * @author Rsl1122
 */
public class Condition {
    final private String failMsg;
    final private boolean pass;

    public Condition(boolean pass, String failMsg) {
        this.failMsg = failMsg;
        this.pass = pass;
    }

    public String getFailMsg() {
        return failMsg;
    }
    
    public boolean pass() {
        return pass;
    }
}
