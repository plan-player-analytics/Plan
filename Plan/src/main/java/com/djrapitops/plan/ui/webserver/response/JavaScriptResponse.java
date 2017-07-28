package main.java.com.djrapitops.plan.ui.webserver.response;

import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.utilities.HtmlUtils;

import java.io.FileNotFoundException;
import java.io.OutputStream;

/**
 * @author Rsl1122
 * @since 3.5.2
 */
public class JavaScriptResponse extends Response {

    public JavaScriptResponse(OutputStream output, String resource) {
        super(output);
        super.setHeader("HTTP/1.1 200 OK");
        try {
            super.setContent(HtmlUtils.getStringFromResource(resource));
        } catch (FileNotFoundException e) {
            Log.toLog(this.getClass().getName(), e);
            super.setContent("");
        }
    }
}
