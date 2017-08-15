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

        return (header != null ? header.equals(response.header) : response.header == null)
                && (content != null ? content.equals(response.content) : response.content == null);
    }

    @Override
    public int hashCode() {
        int result = header != null ? header.hashCode() : 0;
        result = 31 * result + (content != null ? content.hashCode() : 0);
        return result;
    }
}
