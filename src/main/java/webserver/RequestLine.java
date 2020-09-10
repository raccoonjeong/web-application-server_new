package webserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;

import java.util.Map;


public class RequestLine {

    private final static Logger log = LoggerFactory.getLogger(RequestHandler.class);
    private HttpMethod method;
    private String path;
    private Map<String, String> params;


    public RequestLine(String requestLine) {
        log.debug("request line : {}", requestLine);
        String[] tokens = requestLine.split(" ");
        method = HttpMethod.valueOf(tokens[0]);

        if (method == HttpMethod.POST) {
            path = tokens[1];
            return;
        }

        int index = tokens[1].indexOf("?");

        if(index == -1) { // 쿼리스트링이 없다
            path = tokens[1];
        } else { // 쿼리스트링이 있다
            path = tokens[1].substring(0, index);
            params = HttpRequestUtils.parseQueryString(tokens[1].substring(index+1));
        }
    }

    public HttpMethod getMethod() {
        return this.method;
    }

    public String getPath() {
        return this.path;
    }

    public Map<String, String> getParams() {
        return this.params;
    }
}
