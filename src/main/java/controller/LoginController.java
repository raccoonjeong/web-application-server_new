package controller;

import db.DataBase;
import http.HttpSession;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import http.HttpRequest;
import http.HttpResponse;

public class LoginController implements Controller{
    private static final Logger log = LoggerFactory.getLogger(CreateUserController.class);
    @Override
    public void service(HttpRequest request, HttpResponse response) {
        User user = DataBase.findUserById(request.getParameter("userId"));
        if (user != null && user.getUserId().equals(request.getParameter("userId"))
                && user.getPassword().equals(request.getParameter("password"))) {
            // 로그인 성공
            log.debug("로그인 성공");
            HttpSession session = request.getSession();
            session.setAttribute("user", user);

            response.sendRedirect("/index.html");
        } else {
            log.debug("로그인 실패");
            // response.addHeader("Set-Cookie", "logined=fail");
            response.sendRedirect("/user/login_failed.html");
        }
    }
}
