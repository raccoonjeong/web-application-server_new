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
            HttpResponse httpResponse = new HttpResponse(out);


            String url = httpRequest.getPath();
            Map<String, String> cookieMap = HttpRequestUtils.parseCookies(httpRequest.getHeader("Cookie"));


            if ("/user/create".equals(url)) {
                User user = new User(httpRequest.getParameter("userId"),
                        httpRequest.getParameter("password"),
                        httpRequest.getParameter("name"),
                        httpRequest.getParameter("email"));

                DataBase.addUser(user);
                log.debug("User: {}", user);

                httpResponse.sendRedirect("/index.html");

            } else if ("/user/login".equals(url)) {
                User user = DataBase.findUserById(httpRequest.getParameter("userId"));
                if (user != null && user.getUserId().equals(httpRequest.getParameter("userId"))
                        && user.getPassword().equals(httpRequest.getParameter("password"))) {
                    // 로그인 성공
                    log.debug("로그인 성공");
                    httpResponse.addHeader("Set-Cookie", "logined=true");
                    httpResponse.sendRedirect("/index.html");
                } else {
                    log.debug("로그인 실패");
                    httpResponse.addHeader("Set-Cookie", "logined=fail");
                    httpResponse.sendRedirect("/user/login_failed.html");
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
                    httpResponse.forwardBody(sb.toString());
                } else {
                    httpResponse.sendRedirect("/user/login.html");
                }
            } else {
                httpResponse.forward(url);
            }

        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

}
