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

            HttpRequest request = new HttpRequest(in);
            HttpResponse response = new HttpResponse(out);


            String url = request.getPath();

            if ("/user/create".equals(url)) {
                createUser(request, response);

            } else if ("/user/login".equals(url)) {
                login(request, response);

            } else if ("/user/list".equals(url)) {
                listUser(request, response);

            } else {
                response.forward(url);
            }

        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void listUser(HttpRequest request, HttpResponse response) {
        Map<String, String> cookieMap = HttpRequestUtils.parseCookies(request.getHeader("Cookie"));

        System.out.println("cookieMap 로그:" + cookieMap);

        if (Boolean.parseBoolean(cookieMap.get("logined"))) {
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
            response.forwardBody(sb.toString());
        } else {
            response.sendRedirect("/user/login.html");
        }
    }

    private void createUser(HttpRequest request, HttpResponse response) {
        User user = new User(request.getParameter("userId"),
                request.getParameter("password"),
                request.getParameter("name"),
                request.getParameter("email"));

        DataBase.addUser(user);
        log.debug("User: {}", user);

        response.sendRedirect("/index.html");
    }
    private void login(HttpRequest request, HttpResponse response) {
        User user = DataBase.findUserById(request.getParameter("userId"));
        if (user != null && user.getUserId().equals(request.getParameter("userId"))
                && user.getPassword().equals(request.getParameter("password"))) {
            // 로그인 성공
            log.debug("로그인 성공");
            response.addHeader("Set-Cookie", "logined=true");
            response.sendRedirect("/index.html");
        } else {
            log.debug("로그인 실패");
            response.addHeader("Set-Cookie", "logined=fail");
            response.sendRedirect("/user/login_failed.html");
        }
    }

}
