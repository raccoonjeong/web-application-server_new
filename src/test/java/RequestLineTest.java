import org.junit.Test;
import http.HttpMethod;
import http.RequestLine;

import static org.junit.Assert.assertEquals;

public class RequestLineTest {
    @Test
    public void create_method() {
        RequestLine line = new RequestLine("GET /index.html HTTP/1.1");
        assertEquals(HttpMethod.GET, line.getMethod());
        assertEquals("/index.html", line.getPath());

        line = new RequestLine("POST /index.html HTTP/1.1");
        assertEquals("/index.html", line.getPath());
    }
}
