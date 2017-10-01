package main.java.com.djrapitops.plan.systems.webserver.response;

/**
 * @author Rsl1122
 * @since 3.5.2
 */
public class JavaScriptResponse extends FileResponse {

    public JavaScriptResponse(String fileName) {
        super(fileName);
        super.setType(ResponseType.JAVASCRIPT);
    }
}
