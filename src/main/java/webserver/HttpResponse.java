package webserver;

import java.io.DataOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class HttpResponse {
    private DataOutputStream dos;
    private Map<String, String> headers = new HashMap<>();
    private byte[] bodyOfResponse;
    public HttpResponse(OutputStream out) {
        dos = new DataOutputStream(out);

        try {
            // bodyOfResponse = Files.readAllBytes(new File("./webapp/index.html").toPath());
        }catch(Exception e) {

        }
    }

    public void forward(String path) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            for (String key : headers.keySet()) {
                dos.writeBytes(key+": "+ headers.get(key)+"\r\n");
            }
            dos.writeBytes("\r\n");
            dos.writeBytes(path); // body
            dos.writeBytes("\r\n");
        }catch(Exception e) {

        }
    }

    public void sendRedirect(String path) {
        try {
            dos.writeBytes("HTTP/1.1 302 Redirect \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            for (String key : headers.keySet()) {
                dos.writeBytes(key+": "+ headers.get(key)+"\r\n");
            }
            dos.writeBytes("location: " + path + "\r\n");
            dos.writeBytes("\r\n");
        }catch(Exception e) {

        }
    }

    public void addHeader(String key, String value) {
        headers.put(key, value);
    }
}
