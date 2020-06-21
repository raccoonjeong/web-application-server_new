package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import db.DataBase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;
import util.IOUtils;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            // TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.

            BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            String line = br.readLine();
            log.debug("request line: {}", line);

            if (line == null) {
                return;
            }
            String[] tokens = line.split(" ");

            int contentLength = 0;
            Map<String, String> cookieMap = new HashMap<>();
            while (!line.equals("")) {
                line = br.readLine();
                log.debug("header: {}", line);
                if (line.contains("Content-Length")) {
                    contentLength = getContentLength(line);
                }

                if (line.contains("Cookie")) {
                    cookieMap = HttpRequestUtils.parseCookies(line);
                }
            }

            String url = tokens[1];
            if ("/user/create".equals(url)) {

                String body = IOUtils.readData(br, contentLength);
                Map<String, String> params = HttpRequestUtils.parseQueryString(body);
                User user = new User(params.get("userId"), params.get("password"), params.get("name"), params.get("email"));
                DataBase.addUser(user);
                log.debug("User: {}", user);

                DataOutputStream dos = new DataOutputStream(out);

                byte[] bodyData = Files.readAllBytes(new File("./webapp/index.html").toPath());
                response302Header(dos, bodyData.length, "/index.html");
                responseBody(dos, bodyData);

            } else if ("/user/login".equals(url)) {

                String body = IOUtils.readData(br, contentLength);
                Map<String, String> params = HttpRequestUtils.parseQueryString(body);

                DataOutputStream dos = new DataOutputStream(out);

                User user = DataBase.findUserById(params.get("userId"));
                if (user != null && user.getUserId().equals(params.get("userId")) && user.getUserId().equals(params.get("password"))) {
                    // 로그인 성공
                    log.debug("로그인 성공");
                    byte[] bodyData = Files.readAllBytes(new File("./webapp/index.html").toPath());
                    response302HeaderWithCookie(dos, bodyData.length, "/index.html", "logined=true");
                    responseBody(dos, bodyData);

                } else {
                    log.debug("로그인 실패");
                    byte[] bodyData = Files.readAllBytes(new File("./webapp/user/login_failed.html").toPath());
                    response302HeaderWithCookie(dos, bodyData.length, "/user/login_failed.html", "logined=fail");
                    responseBody(dos, bodyData);

                }
            } else if ("/user/list".equals(url)) {

                DataOutputStream dos = new DataOutputStream(out);
                if (cookieMap.containsKey("Cookie: logined") && Boolean.parseBoolean(cookieMap.get("Cookie: logined"))) {

                    byte[] bodyData = Files.readAllBytes(new File("./webapp/user/list.html").toPath());
                    response302Header(dos, bodyData.length, "/user/list.html");
                    responseBody(dos, bodyData);

                } else {
                    byte[] bodyData = Files.readAllBytes(new File("./webapp/user/login.html").toPath());
                    response302Header(dos, bodyData.length, "/user/login.html");
                    responseBody(dos, bodyData);

                }

            } else if (url.contains(".css")) {
                DataOutputStream dos = new DataOutputStream(out);
                byte[] bodyData = Files.readAllBytes(new File("./webapp" + url).toPath());
                response200HeaderForCSS(dos, bodyData.length);
                responseBody(dos, bodyData);
            } else {

                DataOutputStream dos = new DataOutputStream(out);

                byte[] body = Files.readAllBytes(new File("./webapp"+tokens[1]).toPath());
                response200Header(dos, body.length);
                responseBody(dos, body);

            }

        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
    private int getContentLength(String line) {
        String[] headerTokens = line.split(":");
        return Integer.parseInt(headerTokens[1].trim());
    }

    private void response302HeaderWithCookie(DataOutputStream dos, int lengthOfBodyContent, String location, String cookie) {
        try {
            dos.writeBytes("HTTP/1.1 302 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("Set-Cookie: " + cookie + "\r\n");
            dos.writeBytes("location: " + location + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response200HeaderForCSS(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/css;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response302Header(DataOutputStream dos, int lengthOfBodyContent, String location) {
        try {
            dos.writeBytes("HTTP/1.1 302 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("location: " + location + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
