package controller;

import webserver.HttpRequest;
import webserver.HttpResponse;

public abstract class AbstractController implements Controller{
    public void service(HttpRequest request, HttpResponse response) {

        doGet();
    };

    public abstract void doGet();
}
