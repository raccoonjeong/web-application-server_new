package webserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class HttpResponse {
    private final static Logger log = LoggerFactory.getLogger(HttpResponse.class);
    private DataOutputStream dos;
    private Map<String, String> headers = new HashMap<>();
    private byte[] bodyOfResponse;

    public HttpResponse(OutputStream out) {
        dos = new DataOutputStream(out);
    }


    public void addHeader(String key, String value) {
        headers.put(key, value);
    }
    public void forward(String url) {
        try {
            byte[] body = Files.readAllBytes(new File("./webapp"+url).toPath());
            if (url.endsWith(".css")) {
                headers.put("Content-Type", "text/css");
            } else if (url.endsWith(".js")) {
                headers.put("Content-Type", "application/javascript");
            } else {
                headers.put("Content-Type", "text/html;charset=utf=8");
            }
            headers.put("Content-Length", body.length + "");
//            dos.writeBytes("HTTP/1.1 200 OK \r\n");
//            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
//            processHeaders();
//            dos.writeBytes("\r\n");
//            dos.writeBytes(url); // body
//            dos.writeBytes("\r\n");
            response200Header(body.length);
            responseBody(body);
        }catch(Exception e) {

        }
    }

    public void forwardBody(String body) {
        byte[] contents = body.getBytes();
        headers.put("Content-Type", "text/html;charset=utf-8");
        headers.put("Content-Length", contents.length + "");
        response200Header(contents.length);
        responseBody(contents);
    }

    public void response200Header(int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            processHeaders();
            dos.writeBytes("\r\n");
        } catch(IOException e) {
            log.error(e.getMessage());
        }
    }

    public void sendRedirect(String path) {
        try {
            dos.writeBytes("HTTP/1.1 302 Redirect \r\n");
            processHeaders();
            dos.writeBytes("location: " + path + "\r\n");
            dos.writeBytes("\r\n");
        }catch(IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseBody(byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.writeBytes("\r\n");
            dos.flush();
        }catch(IOException e) {
            log.error(e.getMessage());
        }
    }


    private void processHeaders() {
        try {
            for (String key : headers.keySet()) {
                dos.writeBytes(key+": "+ headers.get(key)+"\r\n");
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }


}
