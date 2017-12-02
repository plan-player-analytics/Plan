package main.java.com.djrapitops.plan.systems.webserver.response;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.zip.GZIPOutputStream;

/**
 * @author Rsl1122
 * @since 3.5.2
 */
public abstract class Response {

    private String type;
    private String header;
    private String content;

    private Headers responseHeaders;

    /**
     * Class Constructor.
     */
    public Response(ResponseType type) {
        this.type = type.get();
    }

    public Response() {
        this.type = ResponseType.HTML.get();
    }

    protected String getHeader() {
        return header;
    }

    public String getResponse() {
        return header + "\r\n"
                + "Content-Type: " + type + ";\r\n"
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

    protected void setType(ResponseType type) {
        this.type = type.get();
    }

    public void setResponseHeaders(Headers responseHeaders) {
        this.responseHeaders = responseHeaders;
    }

    public void send(HttpExchange exchange) throws IOException {
        responseHeaders.set("Content-Type", type);
        responseHeaders.set("Content-Encoding", "gzip");
        exchange.sendResponseHeaders(getCode(), 0);

        try (GZIPOutputStream out = new GZIPOutputStream(exchange.getResponseBody());
             ByteArrayInputStream bis = new ByteArrayInputStream(content.getBytes())) {
            byte[] buffer = new byte[2048];
            int count;
            while ((count = bis.read(buffer)) != -1) {
                out.write(buffer, 0, count);
            }
        }
    }

    @Override
    public String toString() {
        return header + " | " + getResponse();
    }
}
