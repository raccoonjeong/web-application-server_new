package webserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;
import util.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class HttpRequest {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private String method;
    private String path;
    private Map<String,String> headers = new HashMap<String, String>();
    private Map<String,String> params = new HashMap<String, String>();

    public HttpRequest(InputStream in) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            String line = br.readLine();
            if (line == null) return;

            processRequestLine(line);

            line = br.readLine();
            while(!line.equals("")) {
                log.debug("header : {}", line);
                String[] tokens = line.split(":");
                headers.put(tokens[0].trim(), tokens[1].trim());
                line = br.readLine();
            }

            if ("POST".equals(method)) {
                String body = IOUtils.readData(br, Integer.parseInt(headers.get("Content-Length")));
                params = HttpRequestUtils.parseQueryString(body);
            }

        } catch(IOException io) {
            log.error(io.getMessage());
        }
    }

    private void processRequestLine(String requestLine) {
        log.debug("request line : {}", requestLine);
        String[] tokens = requestLine.split(" ");
        method = tokens[0];

        if ("POST".equals(method)) {
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

    public String getMethod() {
        return this.method;
    }

    public String getPath() {
        return this.path;
    }

    public String getHeader(String key) {
        return this.headers.get(key);
    }

    public String getParameter(String key) {
        return this.params.get(key);
    }

//    public HttpRequest(InputStream in) {
//        try {
//            BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
//            String line = br.readLine();
//            log.debug("request line: {}", line);
//
//            if (line == null)  return;
//
//            String[] tokens = line.split(" ");
//            String method = tokens[0];
//            String url = tokens[1];
//
//            String[] pathAndParams = HttpRequestUtils.parsePathAndParams(url);
//            String path = pathAndParams[0];
//
//            Map<String, String> paramMap = HttpRequestUtils.parseQueryString(pathAndParams[1]);
////            int contentLength = 0;
//
//            Map<String, String> headerMap = new HashMap<>();
//            while (!line.equals("")) {
//                line = br.readLine();
//                if (line != null && line.contains(":")) {
//                    log.debug("header: {}", line);
//                    HttpRequestUtils.Pair pair = HttpRequestUtils.parseHeader(line);
//                    headerMap.put(pair.getKey(), pair.getValue());
//                } else {
//                    break;
//                }
//            }
//
////            setMethod(method);
////            setPath(path);
////            setParameter(paramMap);
////            setHeader(headerMap);
//            this.method = method;
//            this.path = path;
//            this.parameterMap = paramMap;
//            this.headerMap = headerMap;
//
//        }catch(Exception e) {
//            e.printStackTrace();
//        }
//    }
//    private int getContentLength(String line) {
//        String[] headerTokens = line.split(":");
//        return Integer.parseInt(headerTokens[1].trim());
//    }
//
//    public void setMethod(String method) {
//        this.method = method;
//    }
//
//    public void setPath(String path) {
//        this.path = path;
//    }
//
//    public void setHeader(Map headerMap) {
//        this.headerMap = headerMap;
//    }
//
//    public void setParameter(Map parameterMap) {
//        this.parameterMap = parameterMap;
//    }
//

}
