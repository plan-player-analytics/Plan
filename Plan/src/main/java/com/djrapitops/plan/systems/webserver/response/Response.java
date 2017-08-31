package main.java.com.djrapitops.plan.systems.webserver.response;

import java.util.Objects;

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

    public void setContent(String content) {
        this.content = content;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public int getCode() {
        return header == null ? 500 : Integer.parseInt(header.split(" ")[1]);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Response response = (Response) o;
        return Objects.equals(header, response.header) &&
                Objects.equals(content, response.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(header, content);
    }
}
