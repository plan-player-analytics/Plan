package main.java.com.djrapitops.plan.ui.webserver.response;

/**
 * @author Rsl1122
 * @since 3.5.2
 */
public abstract class Response {

    private String header;
    private String content;

    /**
     * Class Constructor.
     */
    public Response() {
    }

    public String getResponse() {
        return header + "\r\n"
                + "Content-Type: text/html;\r\n"
                + "Content-Length: " + content.length() + "\r\n"
                + "\r\n"
                + content;
    }

    public String getContent() {
        return content;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getCode() {
        if (header == null) {
            return 500;
        } else {
            return Integer.parseInt(header.split(" ")[1]);
        }
    }
}
