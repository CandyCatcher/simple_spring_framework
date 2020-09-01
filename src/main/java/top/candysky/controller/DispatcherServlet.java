package top.candysky.controller;

import lombok.extern.slf4j.Slf4j;
import top.candysky.controller.frontend.MainPageController;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 1.拦截所有的请求
 * 2.解析所有的请求
 * 3.派发给对应的Controller里面的方法进行处理
 */
//拦截所有的请求
@WebServlet("/")
@Slf4j
public class DispatcherServlet extends HttpServlet {
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //获取解析相关的请求，并派发给对应的controller
        //获取请求路径
        System.out.println("request path is " + req.getServletPath());
        //获取请求方法
        System.out.println("request method is " + req.getMethod());

        /*
        首先考虑工厂模式，但是抽象工厂模式也没办法解决controller增加所带来的问题
        加入反射
         */
        if (req.getServletPath() == "/fronted/getmainpageinfo" && req.getMethod() == "GET") {
            new MainPageController().getMainPageInfo(req, resp);
        } else if (req.getServletPath() == "/superadmin/addheadline" && req.getMethod() == "POST") {
            new MainPageController().getMainPageInfo(req, resp);
        }

    }
}
