package controller;

import db.DataBase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;
import webserver.HttpRequest;
import webserver.HttpResponse;

import java.util.Collection;
import java.util.Map;

public class ListUserController implements Controller{
    private static final Logger log = LoggerFactory.getLogger(CreateUserController.class);
    @Override
    public void service(HttpRequest request, HttpResponse response) {
        Map<String, String> cookieMap = HttpRequestUtils.parseCookies(request.getHeader("Cookie"));

        log.debug("cookieMap 로그:" + cookieMap);

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
}
