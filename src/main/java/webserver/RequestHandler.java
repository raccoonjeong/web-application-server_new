package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Collection;
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

            HttpRequest httpRequest = new HttpRequest(in);

            // int contentLength = 0;


            String url = httpRequest.getPath();


            // contentLength = Integer.valueOf(httpRequest.getHeader("Content-Length"));
            Map<String, String> cookieMap = HttpRequestUtils.parseCookies(httpRequest.getHeader("Cookie"));

            DataOutputStream dos = new DataOutputStream(out);
            byte[] bodyOfResponse = null;


            if ("/user/create".equals(url)) {

//                String bodyofRequest = IOUtils.readData(br, contentLength);
//                Map<String, String> params = HttpRequestUtils.parseQueryString(bodyofRequest);
                // User user = new User(params.get("userId"), params.get("password"), params.get("name"), params.get("email"));

                User user = new User(httpRequest.getParameter("userId"),
                        httpRequest.getParameter("password"),
                        httpRequest.getParameter("name"),
                        httpRequest.getParameter("email"));

                DataBase.addUser(user);
                log.debug("User: {}", user);

                bodyOfResponse = Files.readAllBytes(new File("./webapp/index.html").toPath());
                response302Header(dos, bodyOfResponse.length, "/index.html");

            } else if ("/user/login".equals(url)) {

//                String bodyofRequest = IOUtils.readData(br, contentLength);
//                Map<String, String> params = HttpRequestUtils.parseQueryString(bodyofRequest);

                User user = DataBase.findUserById(httpRequest.getParameter("userId"));
                if (user != null && user.getUserId().equals(httpRequest.getParameter("userId"))
                        && user.getPassword().equals(httpRequest.getParameter("password"))) {
                    // 로그인 성공
                    log.debug("로그인 성공");
                    bodyOfResponse = Files.readAllBytes(new File("./webapp/index.html").toPath());
                    response302HeaderWithCookie(dos, bodyOfResponse.length, "/index.html", "logined=true");

                } else {
                    log.debug("로그인 실패");
                    bodyOfResponse = Files.readAllBytes(new File("./webapp/user/login_failed.html").toPath());
                    response302HeaderWithCookie(dos, bodyOfResponse.length, "/user/login_failed.html", "logined=fail");

                }
            } else if ("/user/list".equals(url)) {

                if (cookieMap.containsKey("Cookie: logined") && Boolean.parseBoolean(cookieMap.get("Cookie: logined"))) {

                    Collection<User> users = DataBase.findAll();
                    StringBuilder sb = new StringBuilder();
                    sb.append("<table border='1'>");
                    for(User user : users) {
                        sb.append("<tr>");
                        sb.append("<td>" + user.getUserId() + "</td>");
                        sb.append("<td>" + user.getName() + "</td>");
                        sb.append("<td>" + user.getEmail() + "</td>");
                        sb.append("</tr>");
                    }
                    sb.append("</table>");
                    // bodyOfResponse = Files.readAllBytes(new File("./webapp/user/list.html").toPath());
                    bodyOfResponse = sb.toString().getBytes();
                    response200Header(dos, bodyOfResponse.length);

                } else {
                    bodyOfResponse = Files.readAllBytes(new File("./webapp/user/login.html").toPath());
                    response302Header(dos, bodyOfResponse.length, "/user/login.html");

                }

            } else if (url.contains(".css")) {
                bodyOfResponse = Files.readAllBytes(new File("./webapp" + url).toPath());
                response200HeaderForCSS(dos, bodyOfResponse.length);
            } else {

                bodyOfResponse = Files.readAllBytes(new File("./webapp" + url).toPath());
                response200Header(dos, bodyOfResponse.length);

            }

            responseBody(dos, bodyOfResponse);

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
            dos.writeBytes("HTTP/1.1 302 Redirect \r\n");
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
            dos.writeBytes("HTTP/1.1 302 Redirect \r\n");
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
