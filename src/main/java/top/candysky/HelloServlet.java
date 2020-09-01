package top.candysky;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@WebServlet(urlPatterns = "/index")
public class HelloServlet extends HttpServlet {
    //想在这个方法下显示日志，需要在这里有log方法
    //Logger logger = LoggerFactory.getLogger(HelloServlet.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String name = "my framework";
        log.debug("name is" + name);
        System.out.println(name);
        req.setAttribute("name", name);
        req.getRequestDispatcher("index.jsp").forward(req,resp);
    }
}

